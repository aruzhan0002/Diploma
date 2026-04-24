package com.example.diploma

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun GameBalloonsSuccessOverlay(
    onDismiss: () -> Unit,
    title: String = "Молодец!",
    balloonCount: Int = 80,
    minBalloonSize: Float = 40f,
    maxBalloonSize: Float = 70f,
    durationMillis: Int = 4000
) {
    data class BalloonData(
        val xFraction: Float,
        val size: Float,
        val color: Color,
        val speed: Float,
        val swayAmplitude: Float,
        val swayFrequency: Float
    )

    val balloonColors = listOf(
        Color(0xFFFF4444), Color(0xFF44BB44), Color(0xFF4488FF),
        Color(0xFFFFBB33), Color(0xFFFF66AA), Color(0xFF44DDDD),
        Color(0xFFAA66FF), Color(0xFFFF8844), Color(0xFF66DD44),
        Color(0xFFFF5577), Color(0xFF55BBFF), Color(0xFFFFDD44)
    )

    val balloons = remember(balloonCount, minBalloonSize, maxBalloonSize) {
        List(balloonCount) {
            BalloonData(
                xFraction = Random.nextFloat(),
                size = Random.nextFloat() * (maxBalloonSize - minBalloonSize) + minBalloonSize,
                color = balloonColors[it % balloonColors.size],
                speed = Random.nextFloat() * 0.5f + 0.5f,
                swayAmplitude = Random.nextFloat() * 25f + 10f,
                swayFrequency = Random.nextFloat() * 3f + 1f
            )
        }
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing)
        )
    }
    val progress = animProgress.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onDismiss() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            balloons.forEach { b ->
                val balloonProgress = (progress * (1f / b.speed)).coerceIn(0f, 1f)
                val yPos = -120f + (h + 240f) * balloonProgress
                val sway = kotlin.math.sin(balloonProgress * b.swayFrequency * Math.PI * 2).toFloat() * b.swayAmplitude
                val xPos = b.xFraction * w + sway

                val rx = b.size
                val ry = b.size * 1.3f

                val balloonBody = Path().apply {
                    moveTo(xPos, yPos - ry)
                    cubicTo(
                        xPos + rx * 1.1f, yPos - ry,
                        xPos + rx * 1.1f, yPos + ry * 0.4f,
                        xPos, yPos + ry
                    )
                    cubicTo(
                        xPos - rx * 1.1f, yPos + ry * 0.4f,
                        xPos - rx * 1.1f, yPos - ry,
                        xPos, yPos - ry
                    )
                    close()
                }
                drawPath(path = balloonBody, color = b.color)

                val knot = Path().apply {
                    moveTo(xPos - 5f, yPos + ry)
                    lineTo(xPos, yPos + ry + 10f)
                    lineTo(xPos + 5f, yPos + ry)
                    close()
                }
                drawPath(path = knot, color = b.color)

                val highlight = Path().apply {
                    val hx = xPos - rx * 0.3f
                    val hy = yPos - ry * 0.3f
                    val hr = rx * 0.3f
                    addOval(
                        Rect(
                            hx - hr, hy - hr * 1.2f,
                            hx + hr, hy + hr * 0.6f
                        )
                    )
                }
                drawPath(path = highlight, color = Color.White.copy(alpha = 0.35f))

                val stringPath = Path().apply {
                    moveTo(xPos, yPos + ry + 10f)
                    cubicTo(
                        xPos - 6f, yPos + ry + 40f,
                        xPos + 6f, yPos + ry + 65f,
                        xPos - 3f, yPos + ry + 90f
                    )
                }
                drawPath(
                    path = stringPath,
                    color = Color.White.copy(alpha = 0.5f),
                    style = Stroke(width = 1.5f)
                )
            }
        }

        Text(
            text = title,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color(0xFF7BC144), RoundedCornerShape(24.dp))
                .padding(horizontal = 32.dp, vertical = 16.dp)
        )
    }
}
