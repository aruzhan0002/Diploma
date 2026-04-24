package com.example.diploma

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class EmotionCharacter(
    val id: Int,
    val imageRes: Int,
    val correctEmotionId: Int
)

data class EmotionOption(
    val id: Int,
    val label: String,
    val imageRes: Int
)

data class ConnectionLine(
    val characterId: Int,
    val emotionId: Int,
    val points: List<Offset> = emptyList()
)

@Composable
fun EmotionGamePage(navController: NavController) {

    val context = LocalContext.current
    val startedAtMs = remember { System.currentTimeMillis() }
    var sessionSaved by remember { mutableStateOf(false) }
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.game_music).apply {
            isLooping = true
            seekTo(2000)
        }
    }

    DisposableEffect(Unit) {
        mediaPlayer.start()
        onDispose {
            if (!sessionSaved) {
                val durationSec = ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt()
                GameStatsRepository.recordSession(
                    context = context,
                    gameTitle = "Мои эмоции",
                    durationSeconds = durationSec,
                    won = false
                )
                sessionSaved = true
            }
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    val characters = remember {
        listOf(
            EmotionCharacter(1, R.drawable.emotion_girl_happy, correctEmotionId = 2),
            EmotionCharacter(2, R.drawable.emotion_boy_angry, correctEmotionId = 1)
        )
    }

    val emotions = remember {
        listOf(
            EmotionOption(1, "Злость", R.drawable.emoji_angry),
            EmotionOption(2, "Счастье", R.drawable.emoji_happy)
        )
    }

    var connections by remember { mutableStateOf(listOf<ConnectionLine>()) }
    var dragFrom by remember { mutableStateOf<Int?>(null) }
    var dragPoints by remember { mutableStateOf(listOf<Offset>()) }
    var isDragging by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf(RoutineResult.NONE) }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    val characterCenters = remember { mutableStateMapOf<Int, Offset>() }
    val emotionCenters = remember { mutableStateMapOf<Int, Offset>() }

    val themeColor = Color(0xFF7BC144)
    val bgColor = Color(0xFFE8F5D4)

    fun checkResult() {
        if (connections.size == characters.size) {
            val allCorrect = connections.all { conn ->
                characters.find { it.id == conn.characterId }?.correctEmotionId == conn.emotionId
            }
            result = if (allCorrect) RoutineResult.SUCCESS else RoutineResult.FAIL
        }
    }

    fun resetGame() {
        connections = emptyList()
        result = RoutineResult.NONE
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .pointerInput(connections) {
                detectDragGestures(
                    onDragStart = { startPos ->
                        var closestChar: Int? = null
                        var closestDist = Float.MAX_VALUE
                        characterCenters.forEach { (charId, center) ->
                            val alreadyConnected = connections.any { it.characterId == charId }
                            if (!alreadyConnected) {
                                val dist = (startPos - center).getDistance()
                                if (dist < 200f && dist < closestDist) {
                                    closestDist = dist
                                    closestChar = charId
                                }
                            }
                        }
                        if (closestChar != null) {
                            dragFrom = closestChar
                            isDragging = true
                            dragPoints = listOf(startPos)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        if (isDragging) {
                            change.consume()
                            val last = dragPoints.lastOrNull() ?: Offset.Zero
                            val newPoint = Offset(
                                last.x + dragAmount.x,
                                last.y + dragAmount.y
                            )
                            dragPoints = dragPoints + newPoint
                        }
                    },
                    onDragEnd = {
                        val fromId = dragFrom
                        if (fromId != null && isDragging) {
                            val lastPoint = dragPoints.lastOrNull() ?: Offset.Zero
                            var matched: Int? = null
                            emotionCenters.forEach { (eId, center) ->
                                val dist = (lastPoint - center).getDistance()
                                if (dist < 120f) {
                                    val alreadyTaken = connections.any { it.emotionId == eId }
                                    if (!alreadyTaken) {
                                        matched = eId
                                    }
                                }
                            }
                            if (matched != null) {
                                connections = connections + ConnectionLine(
                                    fromId, matched!!, dragPoints.toList()
                                )
                                checkResult()
                            }
                        }
                        isDragging = false
                        dragFrom = null
                        dragPoints = emptyList()
                    },
                    onDragCancel = {
                        isDragging = false
                        dragFrom = null
                        dragPoints = emptyList()
                    }
                )
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 24.dp, top = 24.dp, end = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(themeColor)
                            .clickable {
                                isPaused = true
                                mediaPlayer.pause()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.pause),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Text(
                        text = "Соедини эмоции",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A7A1A)
                    )

                    Spacer(modifier = Modifier.width(40.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        characters.forEach { character ->
                            val alreadyConnected = connections.any { it.characterId == character.id }

                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .onGloballyPositioned { coords ->
                                        val bounds = coords.boundsInRoot()
                                        characterCenters[character.id] = Offset(
                                            bounds.right,
                                            bounds.center.y
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = character.imageRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(20.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                if (alreadyConnected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(themeColor.copy(alpha = 0.15f))
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        emotions.forEach { emotion ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .onGloballyPositioned { coords ->
                                        val bounds = coords.boundsInRoot()
                                        emotionCenters[emotion.id] = Offset(
                                            bounds.left,
                                            bounds.center.y
                                        )
                                    }
                            ) {
                                Image(
                                    painter = painterResource(id = emotion.imageRes),
                                    contentDescription = emotion.label,
                                    modifier = Modifier.size(80.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineColor = Color(0xFF7BC144)
            val strokeWidth = 6.dp.toPx()

            fun drawFreeformLine(pts: List<Offset>, color: Color) {
                if (pts.size < 2) return
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    for (i in 1 until pts.size) {
                        val prev = pts[i - 1]
                        val cur = pts[i]
                        val midX = (prev.x + cur.x) / 2f
                        val midY = (prev.y + cur.y) / 2f
                        quadraticBezierTo(prev.x, prev.y, midX, midY)
                    }
                    lineTo(pts.last().x, pts.last().y)
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }

            connections.forEach { conn ->
                if (conn.points.size >= 2) {
                    drawFreeformLine(conn.points, lineColor)
                }
            }

            if (isDragging && dragPoints.size >= 2) {
                drawFreeformLine(dragPoints, lineColor.copy(alpha = 0.6f))
            }
        }

        if (result == RoutineResult.FAIL) {
            ResultOverlayGreen(
                text = "Попробуй еще раз!",
                onDismiss = { resetGame() }
            )
        }

        if (result == RoutineResult.SUCCESS) {
            SuccessOverlayGreen(
                onDismiss = {
                    if (!sessionSaved) {
                        val durationSec = ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt()
                        GameStatsRepository.recordSession(
                            context = context,
                            gameTitle = "Мои эмоции",
                            durationSeconds = durationSec,
                            won = true
                        )
                        sessionSaved = true
                    }
                    navController.popBackStack()
                }
            )
        }

        if (isPaused) {
            PauseOverlayGreen(
                isMuted = isMuted,
                onResume = {
                    isPaused = false
                    if (!isMuted) mediaPlayer.start()
                },
                onHome = {
                    navController.navigate("ModuleOneGamesPage") {
                        popUpTo("ModuleOneGamesPage") { inclusive = true }
                    }
                },
                onToggleSound = {
                    isMuted = !isMuted
                    if (isMuted) {
                        mediaPlayer.setVolume(0f, 0f)
                    } else {
                        mediaPlayer.setVolume(1f, 1f)
                    }
                }
            )
        }
    }
}

@Composable
private fun PauseOverlayGreen(
    isMuted: Boolean,
    onResume: () -> Unit,
    onHome: () -> Unit,
    onToggleSound: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_home_green),
                contentDescription = "Домой",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable { onHome() },
                contentScale = ContentScale.Fit
            )
            Image(
                painter = painterResource(id = R.drawable.ic_play_green),
                contentDescription = "Продолжить",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .clickable { onResume() },
                contentScale = ContentScale.Fit
            )
            Image(
                painter = painterResource(
                    id = if (isMuted) R.drawable.ic_sound_off_green else R.drawable.ic_sound_on_green
                ),
                contentDescription = "Звук",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable { onToggleSound() },
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun ResultOverlayGreen(
    text: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .background(Color(0xFF7BC144), RoundedCornerShape(24.dp))
                .padding(horizontal = 32.dp, vertical = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuccessOverlayGreen(
    onDismiss: () -> Unit
) {
    val balloonColors = listOf(
        Color(0xFFFF4444), Color(0xFF44BB44), Color(0xFF4488FF),
        Color(0xFFFFBB33), Color(0xFFFF66AA), Color(0xFF44DDDD),
        Color(0xFFAA66FF), Color(0xFFFF8844), Color(0xFF66DD44),
        Color(0xFFFF5577), Color(0xFF55BBFF), Color(0xFFFFDD44)
    )

    val balloons = remember {
        List(80) {
            BalloonData(
                xFraction = kotlin.random.Random.nextFloat(),
                size = kotlin.random.Random.nextFloat() * 30f + 40f,
                color = balloonColors[it % balloonColors.size],
                speed = kotlin.random.Random.nextFloat() * 0.5f + 0.5f,
                swayAmplitude = kotlin.random.Random.nextFloat() * 25f + 10f,
                swayFrequency = kotlin.random.Random.nextFloat() * 3f + 1f
            )
        }
    }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 4000, easing = LinearEasing)
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
                        androidx.compose.ui.geometry.Rect(
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

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color(0xFF7BC144), RoundedCornerShape(24.dp))
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Молодец!",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 820, heightDp = 380)
@Composable
private fun EmotionGamePreview() {
    MaterialTheme {
        EmotionGamePage(navController = rememberNavController())
    }
}
