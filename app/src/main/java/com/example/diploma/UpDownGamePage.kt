package kz.aruzhan.care_steps

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.roundToInt

private enum class UpDownTarget { TOP, BOTTOM }

private data class UpDownRound(
    val title: String,
    val target: UpDownTarget,
    val sceneRes: Int,
    val objectRes: Int,
    val sceneWidth: androidx.compose.ui.unit.Dp,
    val sceneHeight: androidx.compose.ui.unit.Dp,
    val objectSize: androidx.compose.ui.unit.Dp,
    val topOffsetX: androidx.compose.ui.unit.Dp,
    val topOffsetY: androidx.compose.ui.unit.Dp,
    val bottomOffsetX: androidx.compose.ui.unit.Dp,
    val bottomOffsetY: androidx.compose.ui.unit.Dp,
    val zoneSize: androidx.compose.ui.unit.Dp
)

@Composable
fun UpDownGamePage(navController: NavController) {
    val context = LocalContext.current
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.game_music).apply { isLooping = true }
    }

    val rounds = remember {
        listOf(
            UpDownRound(
                title = "Положи кубик ПОД стол",
                target = UpDownTarget.BOTTOM,
                sceneRes = R.drawable.updown_table,
                objectRes = R.drawable.updown_cube,
                sceneWidth = 340.dp,
                sceneHeight = 220.dp,
                objectSize = 96.dp,
                topOffsetX = 0.dp,
                topOffsetY = (-80).dp,
                bottomOffsetX = 0.dp,
                bottomOffsetY = 86.dp,
                zoneSize = 130.dp
            ),
            UpDownRound(
                title = "Положи мяч НА дерево",
                target = UpDownTarget.TOP,
                sceneRes = R.drawable.guess_sound_updown_tree,
                objectRes = R.drawable.guess_sound_updown_ball,
                sceneWidth = 260.dp,
                sceneHeight = 300.dp,
                objectSize = 92.dp,
                topOffsetX = (-74).dp,
                topOffsetY = (-84).dp,
                bottomOffsetX = 108.dp,
                bottomOffsetY = 88.dp,
                zoneSize = 128.dp
            )
        )
    }

    var roundIndex by remember { mutableStateOf(0) }
    var starsFilled by remember { mutableStateOf(0) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var cubeRect by remember { mutableStateOf<Rect?>(null) }
    var topRect by remember { mutableStateOf<Rect?>(null) }
    var bottomRect by remember { mutableStateOf<Rect?>(null) }
    var result by remember { mutableStateOf(RoutineResult.NONE) }
    var showRoundSuccess by remember { mutableStateOf(false) }
    var showFinalSuccess by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        mediaPlayer.start()
        onDispose {
            runCatching {
                mediaPlayer.stop()
                mediaPlayer.release()
            }
        }
    }

    fun resetCube() {
        dragOffset = Offset.Zero
    }

    fun checkDrop() {
        val center = cubeRect?.center ?: return
        val inTop = topRect?.contains(center) == true
        val inBottom = bottomRect?.contains(center) == true
        val expected = rounds[roundIndex].target
        val correct = when (expected) {
            UpDownTarget.TOP -> inTop
            UpDownTarget.BOTTOM -> inBottom
        }
        if (correct) {
            starsFilled = (starsFilled + 1).coerceAtMost(2)
            if (roundIndex == rounds.lastIndex) {
                showFinalSuccess = true
            } else {
                showRoundSuccess = true
            }
        } else {
            result = RoutineResult.FAIL
            resetCube()
        }
    }

    LaunchedEffect(roundIndex) {
        result = RoutineResult.NONE
        resetCube()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD9EDA8))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8DD121))
                        .clickable {
                            isPaused = true
                            mediaPlayer.pause()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.updown_pause),
                        contentDescription = null,
                        modifier = Modifier.size(46.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Text(
                    text = rounds[roundIndex].title,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF72BD1C)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(2) { idx ->
                        Image(
                            painter = painterResource(
                                if (idx < starsFilled) R.drawable.game_star_filled else R.drawable.game_star_outline
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(rounds[roundIndex].sceneRes),
                    contentDescription = null,
                    modifier = Modifier.size(
                        width = rounds[roundIndex].sceneWidth,
                        height = rounds[roundIndex].sceneHeight
                    ),
                    contentScale = ContentScale.Fit
                )

                Box(
                    modifier = Modifier
                        .size(rounds[roundIndex].zoneSize)
                        .offset(
                            x = rounds[roundIndex].topOffsetX,
                            y = rounds[roundIndex].topOffsetY
                        )
                        .upDownDropZone()
                        .onGloballyPositioned { topRect = it.boundsInRoot() }
                )

                Box(
                    modifier = Modifier
                        .size(rounds[roundIndex].zoneSize)
                        .offset(
                            x = rounds[roundIndex].bottomOffsetX,
                            y = rounds[roundIndex].bottomOffsetY
                        )
                        .upDownDropZone()
                        .onGloballyPositioned { bottomRect = it.boundsInRoot() }
                )
            }
        }

        Image(
            painter = painterResource(rounds[roundIndex].objectRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 44.dp, top = 54.dp)
                .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
                .size(rounds[roundIndex].objectSize)
                .pointerInput(isPaused, showRoundSuccess, showFinalSuccess) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            if (isPaused || showRoundSuccess || showFinalSuccess) return@detectDragGestures
                            change.consume()
                            dragOffset += dragAmount
                        },
                        onDragEnd = {
                            if (!isPaused && !showRoundSuccess && !showFinalSuccess) {
                                checkDrop()
                            }
                        }
                    )
                }
                .onGloballyPositioned { cubeRect = it.boundsInRoot() },
            contentScale = ContentScale.Fit
        )

        if (result == RoutineResult.FAIL) {
            UpDownTextOverlay("Попробуй еще раз!") { result = RoutineResult.NONE }
        }

        if (showRoundSuccess) {
            GameBalloonsSuccessOverlay(
                title = "Молодец!",
                onDismiss = {
                    showRoundSuccess = false
                    roundIndex += 1
                    resetCube()
                }
            )
        }

        if (showFinalSuccess) {
            GameBalloonsSuccessOverlay(
                title = "Ты справился!",
                onDismiss = { navController.popBackStack() }
            )
        }

        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    UpDownControlButton(onClick = {
                        mediaPlayer.pause()
                        navController.navigate("ChildGamesPage") {
                            popUpTo("ChildGamesPage") { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    UpDownControlButton(size = 74.dp, onClick = {
                        isPaused = false
                        if (!isMuted) mediaPlayer.start()
                    }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    UpDownControlButton(onClick = {
                        isMuted = !isMuted
                        val v = if (isMuted) 0f else 1f
                        mediaPlayer.setVolume(v, v)
                    }) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Sound",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpDownControlButton(
    size: androidx.compose.ui.unit.Dp = 66.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(width = 4.dp, color = Color.White, shape = CircleShape)
            .background(Color(0xFF72BD1C))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun UpDownTextOverlay(text: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF5AAF1A),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(horizontal = 28.dp, vertical = 14.dp)
        )
    }
}

private fun Modifier.upDownDropZone(): Modifier = this
    .clip(RoundedCornerShape(20.dp))
    .background(Color(0xFFDDE9CC).copy(alpha = 0.32f))
    .drawBehind {
        drawRoundRect(
            color = Color(0xFF72BD1C).copy(alpha = 0.72f),
            cornerRadius = CornerRadius(24f, 24f),
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
            )
        )
    }

