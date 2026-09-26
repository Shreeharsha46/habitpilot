package com.example.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DayTrend(
    val dayLabel: String, // "MON", "TUE", etc.
    val dateString: String,
    val completionPercentage: Float // 0f .. 1f
)

@Composable
fun TrendChartView(
    habits: List<Habit>,
    logs: List<HabitLog>,
    modifier: Modifier = Modifier
) {
    val totalActive = habits.count { !it.isArchived }

    val trendData = remember(habits, logs) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val list = mutableListOf<DayTrend>()

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6) // Last 7 days including today

        val completionsByDate = logs.filter { it.completed }.groupBy { it.date }.mapValues { it.value.size }

        for (i in 0..6) {
            val dateStr = sdf.format(cal.time)
            val dayLabel = dayFormat.format(cal.time).uppercase(Locale.getDefault())

            val completedCount = completionsByDate[dateStr] ?: 0
            val pct = if (totalActive > 0) {
                (completedCount.toFloat() / totalActive.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }

            list.add(DayTrend(dayLabel, dateStr, pct))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val primaryColor = PurplePrimary
    val glowColor = PurpleSecondary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder.copy(alpha = 0.5f)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "7-Day Completion Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Rolling consistency & momentum",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val currentAvg = if (trendData.isNotEmpty()) {
                    (trendData.map { it.completionPercentage }.average() * 100).toInt()
                } else 0
                Text(
                    text = "$currentAvg% avg",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val width = size.width
                val height = size.height
                val paddingBottom = 20.dp.toPx()
                val effectiveHeight = height - paddingBottom

                if (trendData.isEmpty()) return@Canvas

                val stepX = width / (trendData.size - 1).coerceAtLeast(1)

                val points = trendData.mapIndexed { index, data ->
                    val x = index * stepX
                    val y = effectiveHeight - (data.completionPercentage * (effectiveHeight - 20.dp.toPx()))
                    Offset(x, y)
                }

                // Build smooth Bezier path
                val strokePath = Path()
                val fillPath = Path()

                if (points.isNotEmpty()) {
                    strokePath.moveTo(points.first().x, points.first().y)
                    fillPath.moveTo(points.first().x, effectiveHeight)
                    fillPath.lineTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlX = (p0.x + p1.x) / 2
                        strokePath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                        fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                    }

                    fillPath.lineTo(points.last().x, effectiveHeight)
                    fillPath.close()

                    // Draw gradient fill under curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.35f),
                                primaryColor.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = effectiveHeight
                        )
                    )

                    // Draw line
                    drawPath(
                        path = strokePath,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw data points
                    points.forEachIndexed { idx, point ->
                        val isToday = idx == points.size - 1
                        val pointRadius = if (isToday) 6.dp.toPx() else 4.dp.toPx()

                        // Outer ring glow
                        drawCircle(
                            color = primaryColor.copy(alpha = if (isToday) 0.4f else 0.2f),
                            radius = pointRadius + 4.dp.toPx(),
                            center = point
                        )
                        // Inner circle
                        drawCircle(
                            color = if (isToday) Color.White else primaryColor,
                            radius = pointRadius,
                            center = point
                        )
                    }
                }
            }

            // Labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trendData.forEachIndexed { idx, day ->
                    val isToday = idx == trendData.size - 1
                    Text(
                        text = if (isToday) "TODAY" else day.dayLabel,
                        fontSize = 10.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) PurplePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
