package kz.aruzhan.care_steps

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.hypot
import kotlin.math.min

data class PuzzlePieceStable(
    val id: Int,
    val imageRes: Int,
    val pieceWidth: Dp,
    val pieceHeight: Dp,
    val startOffset: DpOffset,
    val targetOffset: DpOffset,
    val currentOffset: DpOffset,
    val isPlaced: Boolean
)

@Composable
fun AssembleAnimalGameStablePage(navController: NavController) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val density = LocalDensity.current

    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.game_music).apply {
            isLooping = true
            seekTo(2000)
        }
    }

    var isPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val previousOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        mediaPlayer.start()

        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
            if (previousOrientation != null) {
                activity.requestedOrientation = previousOrientation
            }
        }
    }

    var pieces by remember { mutableStateOf<List<PuzzlePieceStable>>(emptyList()) }
    var gameWon by remember { mutableStateOf(false) }
    var draggingId by remember { mutableStateOf<Int?>(null) }

    val designSilW = 270f
    val designSilH = 285f
    // Piece PNG pixel sizes (matches your values)
    val designHeadW = 131f
    val designHeadH = 176f
    val designMidW = 121f
    val designMidH = 163f
    val designLegsW = 229f
    val designLegsH = 155f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9EFAE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause button (top-left)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5D547))
                        .clickable {
                            isPaused = true
                            mediaPlayer.pause()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.pause),
                        contentDescription = "Пауза",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Text(
                    text = "Собери животное!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF0B300)
                )

                // Right placeholder to keep title centered
                Box(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val boxWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                    val boxHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
                    val scale = min(boxWidthPx / designSilW, boxHeightPx / designSilH)

                    // Silhouette dimensions in pixels
                    val silWpx = designSilW * scale
                    val silHpx = designSilH * scale
                    val silLeftPx = (boxWidthPx - silWpx) / 2f
                    val silTopPx = (boxHeightPx - silHpx) / 2f

                    fun pxToDp(px: Float): Dp = with(density) { px.toDp() }

                    val silLeft = pxToDp(silLeftPx)
                    val silTop = pxToDp(silTopPx)

                    // ---- IMPORTANT: targets are predefined coordinates inside the puzzle area ----
                    // Adjust these X/Y values if you want perfect pixel alignment with your Figma.
                    // Coordinates are in the same design space as the silhouette (270x285), before scaling.
                    val headTargetInSilX = (designSilW - designHeadW) / 2f
                    val headTargetInSilY = 20f

                    val midTargetInSilX = (designSilW - designMidW) / 2f
                    val midTargetInSilY = 95f

                    val legsTargetInSilX = (designSilW - designLegsW) / 2f
                    val legsTargetInSilY = 130f

                    val headTarget = DpOffset(
                        x = pxToDp(silLeftPx + headTargetInSilX * scale),
                        y = pxToDp(silTopPx + headTargetInSilY * scale)
                    )
                    val midTarget = DpOffset(
                        x = pxToDp(silLeftPx + midTargetInSilX * scale),
                        y = pxToDp(silTopPx + midTargetInSilY * scale)
                    )
                    val legsTarget = DpOffset(
                        x = pxToDp(silLeftPx + legsTargetInSilX * scale),
                        y = pxToDp(silTopPx + legsTargetInSilY * scale)
                    )

                    // Start offsets: where pieces begin (outside the silhouette but still inside the same puzzle container).
                    val headStart = DpOffset(
                        x = pxToDp(silLeftPx - designHeadW * 0.9f * scale),
                        y = pxToDp(silTopPx + 10f * scale)
                    )
                    val midStart = DpOffset(
                        x = pxToDp(silLeftPx + silWpx + 10f * scale),
                        y = pxToDp(silTopPx + 30f * scale)
                    )
                    val legsStart = DpOffset(
                        x = pxToDp(silLeftPx + silWpx + 18f * scale),
                        y = pxToDp(silTopPx + 130f * scale)
                    )

                    // pieceWidth/Height are Dp in pixels already; keep separately for each piece
                    val headPieceWidth = pxToDp(designHeadW * scale)
                    val headPieceHeight = pxToDp(designHeadH * scale)
                    val midPieceWidth = pxToDp(designMidW * scale)
                    val midPieceHeight = pxToDp(designMidH * scale)
                    val legsPieceWidth = pxToDp(designLegsW * scale)
                    val legsPieceHeight = pxToDp(designLegsH * scale)

                    val initialPieces = listOf(
                        PuzzlePieceStable(
                            id = 1,
                            imageRes = R.drawable.game_cat_piece_head,
                            pieceWidth = headPieceWidth,
                            pieceHeight = headPieceHeight,
                            startOffset = headStart,
                            targetOffset = headTarget,
                            currentOffset = headStart,
                            isPlaced = false
                        ),
                        PuzzlePieceStable(
                            id = 2,
                            // IMPORTANT: middle piece is the 121x163 PNG -> game_cat_piece_leg
                            imageRes = R.drawable.game_cat_piece_leg,
                            pieceWidth = midPieceWidth,
                            pieceHeight = midPieceHeight,
                            startOffset = midStart,
                            targetOffset = midTarget,
                            currentOffset = midStart,
                            isPlaced = false
                        ),
                        PuzzlePieceStable(
                            id = 3,
                            // IMPORTANT: legs piece is the 229x155 PNG -> game_cat_piece_body
                            imageRes = R.drawable.game_cat_piece_body,
                            pieceWidth = legsPieceWidth,
                            pieceHeight = legsPieceHeight,
                            startOffset = legsStart,
                            targetOffset = legsTarget,
                            currentOffset = legsStart,
                            isPlaced = false
                        )
                    )

                    // Reset game whenever layout (scale) changes.
                    LaunchedEffect(scale) {
                        pieces = initialPieces
                        gameWon = false
                        draggingId = null
                    }

                    val snapThresholdPx = 58f * scale

                    // Silhouette (static)
                    Image(
                        painter = painterResource(R.drawable.game_cat_silhouette),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(x = silLeft, y = silTop)
                            .size(
                                width = pxToDp(silWpx),
                                height = pxToDp(silHpx)
                            ),
                        contentScale = ContentScale.Fit
                    )

                    // Draggable pieces
                    pieces.forEach { piece ->
                        val isLocked = piece.isPlaced
                        val isOtherDragging = draggingId != null && draggingId != piece.id
                        val canDrag = !isPaused && !gameWon && !isLocked && !isOtherDragging

                        Image(
                            painter = painterResource(piece.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .offset(x = piece.currentOffset.x, y = piece.currentOffset.y)
                                .size(width = piece.pieceWidth, height = piece.pieceHeight)
                                .then(
                                    if (!canDrag) Modifier else Modifier.pointerInput(piece.id) {
                                        detectDragGestures(
                                            onDragStart = { draggingId = piece.id },
                                            onDragEnd = {
                                                // Use latest piece state for correct snap calculation.
                                                val current = pieces.firstOrNull { it.id == piece.id } ?: return@detectDragGestures

                                                val currentCenterX = with(density) { current.currentOffset.x.toPx() + current.pieceWidth.toPx() / 2f }
                                                val currentCenterY = with(density) { current.currentOffset.y.toPx() + current.pieceHeight.toPx() / 2f }

                                                val targetCenterX = with(density) { current.targetOffset.x.toPx() + current.pieceWidth.toPx() / 2f }
                                                val targetCenterY = with(density) { current.targetOffset.y.toPx() + current.pieceHeight.toPx() / 2f }

                                                val dist = hypot(currentCenterX - targetCenterX, currentCenterY - targetCenterY)

                                                val shouldSnap = dist <= snapThresholdPx

                                                val newPieces = pieces.map { p ->
                                                    if (p.id != piece.id) return@map p
                                                    if (shouldSnap) {
                                                        p.copy(
                                                            currentOffset = p.targetOffset,
                                                            isPlaced = true
                                                        )
                                                    } else {
                                                        p.copy(
                                                            currentOffset = p.startOffset,
                                                            isPlaced = false
                                                        )
                                                    }
                                                }

                                                pieces = newPieces
                                                gameWon = newPieces.all { it.isPlaced }
                                                draggingId = null
                                            },
                                            onDragCancel = {
                                                draggingId = null
                                            }
                                        ) { _, dragAmount ->
                                            if (isPaused || gameWon) return@detectDragGestures
                                            val dx = with(density) { dragAmount.x.toDp() }
                                            val dy = with(density) { dragAmount.y.toDp() }

                                            pieces = pieces.map { p ->
                                                if (p.id != piece.id) return@map p
                                                if (p.isPlaced) return@map p
                                                p.copy(
                                                    currentOffset = DpOffset(
                                                        x = p.currentOffset.x + dx,
                                                        y = p.currentOffset.y + dy
                                                    )
                                                )
                                            }
                                        }
                                    }
                                )
                        )
                    }
                }
            }
        }

        // Stars bottom-left
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 18.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.game_star_filled),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                contentScale = ContentScale.Fit
            )
            Image(
                painter = painterResource(
                    if (gameWon) R.drawable.game_star_filled else R.drawable.game_star_outline
                ),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                contentScale = ContentScale.Fit
            )
        }

        // Success overlay
        if (gameWon) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Кошка",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Собрал(а)!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF7BC144), RoundedCornerShape(22.dp))
                            .clickable {
                                pieces = pieces.map { p ->
                                    p.copy(
                                        currentOffset = p.startOffset,
                                        isPlaced = false
                                    )
                                }
                                gameWon = false
                                draggingId = null
                            }
                            .padding(horizontal = 26.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Ещё раз",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Pause overlay
        if (isPaused) {
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
                        painter = painterResource(R.drawable.ic_home_game),
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
                        painter = painterResource(R.drawable.ic_play_game),
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
                        painter = painterResource(
                            id = if (isMuted) R.drawable.ic_sound_off else R.drawable.ic_sound_on
                        ),
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

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

