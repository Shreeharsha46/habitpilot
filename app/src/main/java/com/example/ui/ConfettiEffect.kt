package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class Particle(
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotationSpeed: Float,
    val size: Float,
    val color: Color
)

@Composable
fun ConfettiEffect(
    isActive: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val colors = listOf(
        Color(0xFFD0BCFF), // Lavender
        Color(0xFF81C784), // Mint Green
        Color(0xFFFFD54F), // Gold
        Color(0xFFFF80AB), // Pink
        Color(0xFF64B5F6), // Blue
        Color(0xFFB388FF)  // Deep Lavender
    )

    val particles = remember {
        List(65) {
            val angle = Random.nextFloat() * Math.PI.toFloat() * 2
            val speed = Random.nextFloat() * 600f + 300f
            Particle(
                initialX = 0.5f,
                initialY = 0.35f,
                velocityX = kotlin.math.cos(angle) * speed,
                velocityY = kotlin.math.sin(angle) * speed - 200f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                size = Random.nextFloat() * 10f + 8f,
                color = colors.random()
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(isActive) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
        onFinished()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        val gravity = 980f * t * t
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            val x = (p.initialX * width) + (p.velocityX * t * 0.8f)
            val y = (p.initialY * height) + (p.velocityY * t * 0.8f) + (gravity * 0.5f)
            val rotation = p.rotationSpeed * t
            val alpha = (1f - (t * 0.9f)).coerceIn(0f, 1f)

            rotate(degrees = rotation, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(x - p.size / 2, y - p.size / 2),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}
