package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.data.HabitType
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.PurplePrimary
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun DataManagementDialog(
    habits: List<Habit>,
    onDismiss: () -> Unit,
    onImportHabits: (List<Habit>) -> Unit,
    onClearAll: () -> Unit
) {
    val context = LocalContext.current
    var jsonText by remember {
        mutableStateOf(
            // Pre-fill with exported habits JSON
            exportToJson(habits)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Backup & Data Management",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Export your habit data to JSON or paste a JSON backup below to import.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = CharcoalBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Habits JSON", jsonText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied JSON to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Copy JSON", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            try {
                                val imported = parseFromJson(jsonText)
                                if (imported.isNotEmpty()) {
                                    onImportHabits(imported)
                                    Toast.makeText(context, "Imported ${imported.size} habits", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "No habits found in JSON", Toast.LENGTH_SHORT).show()
                                }
                            } catch (_: Exception) {
                                Toast.makeText(context, "Invalid JSON format", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Import", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = {
                        onClearAll()
                        Toast.makeText(context, "Reset all habits", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All Habits & Data", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)) {
                Text("Close", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

private fun exportToJson(habits: List<Habit>): String {
    val array = JSONArray()
    habits.forEach { habit ->
        val obj = JSONObject().apply {
            put("title", habit.title)
            put("emoji", habit.emoji)
            put("category", habit.category)
            put("type", habit.type.name)
            put("targetValue", habit.targetValue)
            put("streak", habit.streak)
            put("bestStreak", habit.bestStreak)
            put("reminderTime", habit.reminderTime ?: JSONObject.NULL)
            put("reminderEnabled", habit.reminderEnabled)
        }
        array.put(obj)
    }
    return array.toString(2)
}

private fun parseFromJson(jsonStr: String): List<Habit> {
    val list = mutableListOf<Habit>()
    val array = JSONArray(jsonStr)
    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val typeStr = obj.optString("type", "CHECKBOX")
        val habitType = try { HabitType.valueOf(typeStr) } catch (_: Exception) { HabitType.CHECKBOX }

        list.add(
            Habit(
                title = obj.getString("title"),
                emoji = obj.optString("emoji", "✨"),
                category = obj.optString("category", "General"),
                type = habitType,
                targetValue = obj.optInt("targetValue", 1),
                streak = obj.optInt("streak", 0),
                bestStreak = obj.optInt("bestStreak", 0),
                reminderTime = if (obj.isNull("reminderTime")) null else obj.optString("reminderTime"),
                reminderEnabled = obj.optBoolean("reminderEnabled", false)
            )
        )
    }
    return list
}
