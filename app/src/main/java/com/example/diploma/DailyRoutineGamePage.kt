package kz.aruzhan.care_steps

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlin.math.roundToInt
import kotlin.random.Random


val ChildGameFont = FontFamily.Default

enum class RoutineResult {
    NONE, SUCCESS, FAIL
}

data class RoutineCard(
    val id: Int,
    val title: String,
    val imageRes: Int
)

@Composable
fun DailyRoutineGamePage(navController: NavController) {

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
                    gameTitle = "Ежедневные дела",
                    durationSeconds = durationSec,
                    won = false
                )
                sessionSaved = true
            }
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    val cards = listOf(
        RoutineCard(1, "Проснуться", R.drawable.routine_wake),
        RoutineCard(2, "Чистить зубы", R.drawable.routine_brush),
        RoutineCard(3, "Завтрак", R.drawable.routine_breakfast)
    )

    val correctOrder = listOf(1, 2, 3)

    var availableCards by remember { mutableStateOf(cards) }
    val slots = remember { mutableStateListOf<RoutineCard?>(null, null, null) }
    var result by remember { mutableStateOf(RoutineResult.NONE) }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    fun resetGame() {
        availableCards = cards
        for (i in slots.indices) slots[i] = null
        result = RoutineResult.NONE
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE2B5))
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // Левая часть: пауза + заголовок + слоты
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 24.dp, top = 24.dp, end = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Верхняя панель: пауза слева + заголовок по центру
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9150))
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
                        text = "Расставь по порядку",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEE7B45)
                    )

                    Spacer(modifier = Modifier.width(40.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // Слоты 1-2-3
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (0..2).forEach { index ->
                        DropSlot(
                            index = index,
                            card = slots[index],
                            onClick = {
                                val card = slots[index] ?: return@DropSlot
                                slots[index] = null
                                availableCards = availableCards + card
                                result = RoutineResult.NONE
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            // Вертикальная линия-разделитель
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFFF7E53).copy(alpha = 0.4f))
            )

            // Правая часть: карточки для перетаскивания
            Column(
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .padding(vertical = 20.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                availableCards.forEach { card ->
                    DraggableRoutineCard(
                        card = card,
                        enabled = true,
                        onDropToSlot = {
                            val firstEmpty = slots.indexOfFirst { it == null }
                            if (firstEmpty != -1) {
                                val picked =
                                    availableCards.firstOrNull { it.id == card.id }
                                        ?: return@DraggableRoutineCard
                                slots[firstEmpty] = picked
                                availableCards = availableCards.filter { it.id != picked.id }

                                if (slots.all { it != null }) {
                                    val placedOrder = slots.map { it!!.id }
                                    result = if (placedOrder == correctOrder) {
                                        RoutineResult.SUCCESS
                                    } else {
                                        RoutineResult.FAIL
                                    }
                                } else {
                                    result = RoutineResult.NONE
                                }
                            }
                        }
                    )
                }
            }
        }

        if (result == RoutineResult.FAIL) {
            ResultOverlay(
                text = "Попробуй еще раз!",
                backgroundAlpha = 0.75f,
                onDismiss = { resetGame() }
            )
        }

        if (result == RoutineResult.SUCCESS) {
            SuccessOverlay(onDismiss = {
                if (!sessionSaved) {
                    val durationSec = ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt()
                    GameStatsRepository.recordSession(
                        context = context,
                        gameTitle = "Ежедневные дела",
                        durationSeconds = durationSec,
                        won = true
                    )
                    sessionSaved = true
                }
                navController.popBackStack()
            })
        }

        if (isPaused) {
            PauseOverlay(
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
private fun DraggableRoutineCard(
    card: RoutineCard,
    enabled: Boolean,
    onDropToSlot: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .offset {
                androidx.compose.ui.unit.IntOffset(
                    offsetX.roundToInt(),
                    offsetY.roundToInt()
                )
            }
            .pointerInput(card.id, enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragEnd = {
                        if (offsetX < -120f) {
                            onDropToSlot()
                        }
                        offsetX = 0f
                        offsetY = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        SmallRoutineCard(card = card)
    }
}

@Composable
private fun SmallRoutineCard(card: RoutineCard) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(90.dp)
            .clip(RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = card.imageRes),
            contentDescription = card.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun DropSlot(
    index: Int,
    card: RoutineCard?,
    onClick: () -> Unit
) {
    val dashColor = Color(0xFFFF7E53)
    val slotBg = Color(0xFFFFE0C9)

    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(slotBg)
            .drawBehind {
                val strokeWidth = 4.dp.toPx()
                val dashLength = 14.dp.toPx()
                val gapLength = 10.dp.toPx()
                val cornerPx = 28.dp.toPx()
                drawRoundRect(
                    color = dashColor,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(dashLength, gapLength), 0f
                        )
                    )
                )
            }
            .clickable(enabled = card != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (card == null) {
            Text(
                text = (index + 1).toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF7E53).copy(alpha = 0.5f)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .clickable { onClick() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = card.imageRes),
                    contentDescription = card.title,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp)),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = card.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ChildGameFont,
                    color = Color(0xFFBF5C2B),
                    modifier = Modifier.padding(bottom = 4.dp, top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PauseOverlay(
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
            // Домой
            Image(
                painter = painterResource(id = R.drawable.ic_home_game),
                contentDescription = "Домой",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable { onHome() },
                contentScale = ContentScale.Fit
            )

            // Продолжить
            Image(
                painter = painterResource(id = R.drawable.ic_play_game),
                contentDescription = "Продолжить",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .clickable { onResume() },
                contentScale = ContentScale.Fit
            )

            // Звук
            Image(
                painter = painterResource(
                    id = if (isMuted) R.drawable.ic_sound_off else R.drawable.ic_sound_on
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
private fun ResultOverlay(
    text: String,
    backgroundAlpha: Float,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = backgroundAlpha))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .background(Color(0xFFEE7B45), RoundedCornerShape(24.dp))
                .padding(horizontal = 32.dp, vertical = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}

data class BalloonData(
    val xFraction: Float,
    val size: Float,
    val color: Color,
    val speed: Float,
    val swayAmplitude: Float,
    val swayFrequency: Float
)

@Composable
private fun SuccessOverlay(
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
                xFraction = Random.nextFloat(),
                size = Random.nextFloat() * 30f + 40f,
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
                .background(Color(0xFFEE7B45), RoundedCornerShape(24.dp))
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Ты справился!",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ChildGameFont,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 820, heightDp = 380)
@Composable
private fun DailyRoutineGamePreview() {
    MaterialTheme {
        DailyRoutineGamePage(navController = rememberNavController())
    }
}
