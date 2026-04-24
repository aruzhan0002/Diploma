package com.example.diploma

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ColorShape { CIRCLE, STAR, TRIANGLE, SQUARE }

private data class ColorOption(
    val id: Int,
    val shape: ColorShape,
    val color: Color,
    val isCorrect: Boolean,
    val active: Boolean = true
)

private data class ColorRound(
    val title: String,
    val options: List<ColorOption>
)

@Composable
fun ColorFinderGamePage(navController: NavController) {
    val context = LocalContext.current
    val activity = context.findActivity()

    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.game_music).apply {
            isLooping = true
            seekTo(2000)
        }
    }

    val rounds = remember {
        listOf(
            ColorRound(
                title = "Желтый",
                options = listOf(
                    ColorOption(1, ColorShape.CIRCLE, Color(0xFF44EA2F), isCorrect = false),
                    ColorOption(2, ColorShape.STAR, Color(0xFFF3D151), isCorrect = true),
                    ColorOption(3, ColorShape.TRIANGLE, Color(0xFFE245D7), isCorrect = false)
                )
            ),
            ColorRound(
                title = "Красный",
                options = listOf(
                    ColorOption(1, ColorShape.SQUARE, Color(0xFFB3B3B3), isCorrect = false),
                    ColorOption(2, ColorShape.CIRCLE, Color(0xFF22C8D9), isCorrect = false),
                    ColorOption(3, ColorShape.TRIANGLE, Color(0xFFFF4A4A), isCorrect = true)
                )
            )
        )
    }

    var roundIndex by remember { mutableStateOf(0) }
    var options by remember { mutableStateOf(rounds[0].options) }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var showRoundSuccess by remember { mutableStateOf(false) }
    var showFinalSuccess by remember { mutableStateOf(false) }
    var starsFilled by remember { mutableStateOf(0) }
    var sessionSaved by remember { mutableStateOf(false) }
    val startedAtMs = remember { System.currentTimeMillis() }
    var selectedCorrectId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val prevOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        mediaPlayer.start()
        onDispose {
            if (!sessionSaved) {
                val durationSec = ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt()
                GameStatsRepository.recordSession(context, "Найди цвета", durationSec, won = false)
                sessionSaved = true
            }
            mediaPlayer.stop()
            mediaPlayer.release()
            if (prevOrientation != null) activity.requestedOrientation = prevOrientation
        }
    }

    fun onPick(option: ColorOption) {
        if (!option.active || isPaused || showRoundSuccess || showFinalSuccess || selectedCorrectId != null) return
        if (option.isCorrect) {
            selectedCorrectId = option.id
            starsFilled = (starsFilled + 1).coerceAtMost(2)
            scope.launch {
                delay(320)
                if (roundIndex == rounds.lastIndex) {
                    showFinalSuccess = true
                } else {
                    showRoundSuccess = true
                }
            }
        } else {
            options = options.map {
                if (it.id == option.id) it.copy(active = false, color = Color(0xFFB9B9B9)) else it
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6EA3E2))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(0.dp))
                    .background(Color(0xFF6EA3E2))
                    .padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F76E6))
                        .clickable {
                            isPaused = true
                            mediaPlayer.pause()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.pause),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Text(
                    text = rounds[roundIndex].title,
                    modifier = Modifier.align(Alignment.TopCenter),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (roundIndex) {
                        0 -> Color(0xFFF6D14A)
                        else -> Color(0xFFFF5B4A)
                    }
                )

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(34.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    options.forEach { option ->
                        val targetSize = if (selectedCorrectId == option.id) 156.dp else 78.dp
                        ShapeOption(
                            option = option,
                            modifier = Modifier
                                .size(targetSize + 18.dp)
                                .clickable { onPick(option) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(2) { idx ->
                        Image(
                            painter = painterResource(
                                if (idx < starsFilled) R.drawable.game_star_filled else R.drawable.game_star_outline
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }

        if (showRoundSuccess) {
            GameBalloonsSuccessOverlay(
                title = "Молодец!",
                onDismiss = {
                    showRoundSuccess = false
                    roundIndex = 1
                    options = rounds[1].options
                    selectedCorrectId = null
                }
            )
        }

        if (showFinalSuccess) {
            GameBalloonsSuccessOverlay(
                title = "Ты молодец!",
                onDismiss = {
                    if (!sessionSaved) {
                        val durationSec = ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt()
                        GameStatsRepository.recordSession(context, "Найди цвета", durationSec, won = true)
                        sessionSaved = true
                    }
                    selectedCorrectId = null
                    navController.popBackStack()
                }
            )
        }

        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_home_blue),
                        contentDescription = "Домой",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .clickable {
                                navController.navigate("ChildGamesPage") {
                                    popUpTo("ChildGamesPage") { inclusive = true }
                                }
                            },
                        contentScale = ContentScale.Fit
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_play_blue),
                        contentDescription = "Продолжить",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .clickable {
                                isPaused = false
                                if (!isMuted) mediaPlayer.start()
                            },
                        contentScale = ContentScale.Fit
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_sound_blue),
                        contentDescription = "Звук",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .clickable {
                                isMuted = !isMuted
                                if (isMuted) mediaPlayer.setVolume(0f, 0f) else mediaPlayer.setVolume(1f, 1f)
                            },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun ShapeOption(option: ColorOption, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 6f
        when (option.shape) {
            ColorShape.CIRCLE -> {
                drawCircle(
                    color = option.color,
                    radius = size.minDimension * 0.38f,
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = size.minDimension * 0.38f,
                    center = center,
                    style = Stroke(width = stroke)
                )
            }
            ColorShape.SQUARE -> {
                val side = size.minDimension * 0.72f
                val topLeft = Offset((size.width - side) / 2f, (size.height - side) / 2f)
                drawRect(option.color, topLeft = topLeft, size = Size(side, side))
                drawRect(
                    color = Color.White,
                    topLeft = topLeft,
                    size = Size(side, side),
                    style = Stroke(width = stroke)
                )
            }
            ColorShape.TRIANGLE -> {
                val p = Path().apply {
                    moveTo(size.width / 2f, size.height * 0.17f)
                    lineTo(size.width * 0.16f, size.height * 0.82f)
                    lineTo(size.width * 0.84f, size.height * 0.82f)
                    close()
                }
                drawPath(p, color = option.color)
                drawPath(p, color = Color.White, style = Stroke(width = stroke, cap = StrokeCap.Round))
            }
            ColorShape.STAR -> {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val outer = size.minDimension * 0.36f
                val inner = outer * 0.46f
                val path = Path()
                for (i in 0 until 10) {
                    val angle = Math.toRadians((-90.0 + i * 36.0))
                    val r = if (i % 2 == 0) outer else inner
                    val x = cx + (r * kotlin.math.cos(angle)).toFloat()
                    val y = cy + (r * kotlin.math.sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, color = option.color)
                drawPath(path, color = Color.White, style = Stroke(width = stroke, cap = StrokeCap.Round))
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

