package com.example.diploma

import android.media.MediaPlayer
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class PuzzlePieceData(
    val id: Int,
    val imageRes: Int,
    val correctSlot: Int
)

@Composable
fun AssembleAnimalGamePage(navController: NavController) {

    val context = LocalContext.current
    val activity = context.findActivity()
    val density = LocalDensity.current
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.game_music).apply {
            isLooping = true
            seekTo(2000)
        }
    }

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

    val pieces = remember {
        listOf(
            PuzzlePieceData(1, R.drawable.game_cat_piece_head, 0),
            PuzzlePieceData(2, R.drawable.game_cat_piece_leg, 1),
            PuzzlePieceData(3, R.drawable.game_cat_piece_body, 2)
        )
    }

    var slotContents by remember { mutableStateOf(listOf<Int?>(null, null, null)) } // head, leg, body
    var placedPieceIds by remember { mutableStateOf(setOf<Int>()) }
    val pieceBaseOffsets = remember { mutableStateMapOf<Int, Offset>() }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var gameWon by remember { mutableStateOf(false) }

    val slotBounds = remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
    val gameAreaTopLeft = remember { mutableStateOf(Pair(0f, 0f)) }

    fun checkWin() {
        if (placedPieceIds.size == 3 &&
            slotContents[0] == 1 &&
            slotContents[1] == 2 &&
            slotContents[2] == 3
        ) {
            gameWon = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF4B8))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF0B300)
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coords ->
                            val r = coords.boundsInRoot()
                            gameAreaTopLeft.value = Pair(r.left, r.top)
                        }
                ) {
                    val boxWidth = constraints.maxWidth.toFloat()
                    val boxHeight = constraints.maxHeight.toFloat()
                    val designScale = minOf(boxWidth / 828f, boxHeight / 378f)
                    val silhouW = 270f * designScale
                    val silhouH = 285f * designScale
                    fun pieceWidthPx(id: Int): Float = when (id) {
                        1 -> 131f * designScale // head width
                        2 -> 121f * designScale // middle width
                        else -> 229f * designScale // leg/body width
                    }
                    fun pieceHeightPx(id: Int): Float = when (id) {
                        1 -> 176f * designScale // head height
                        2 -> 163f * designScale // middle height
                        else -> 155f * designScale // leg/body height
                    }
                    fun pieceOffsetXPx(id: Int): Float = when (id) {
                        1 -> -silhouW * 0.24f
                        2 -> silhouW * 0.20f
                        else -> silhouW * 0.24f
                    }
                    fun pieceOffsetYPx(id: Int): Float = when (id) {
                        1 -> -silhouH * 0.25f
                        2 -> -silhouH * 0.06f
                        else -> silhouH * 0.20f
                    }

                    Box(
                        modifier = Modifier
                            .size(
                                with(density) { silhouW.toDp() },
                                with(density) { silhouH.toDp() }
                            )
                            .align(Alignment.Center)
                            .onGloballyPositioned { coords ->
                                val r = coords.boundsInRoot()
                                val cx = r.left + r.width / 2f
                                val cy = r.top + r.height / 2f
                                slotBounds.value = listOf(
                                    Pair(
                                        cx + pieceOffsetXPx(1) + pieceWidthPx(1) / 2f,
                                        cy + pieceOffsetYPx(1) + pieceHeightPx(1) / 2f
                                    ),
                                    Pair(
                                        cx + pieceOffsetXPx(2) + pieceWidthPx(2) / 2f,
                                        cy + pieceOffsetYPx(2) + pieceHeightPx(2) / 2f
                                    ),
                                    Pair(
                                        cx + pieceOffsetXPx(3) + pieceWidthPx(3) / 2f,
                                        cy + pieceOffsetYPx(3) + pieceHeightPx(3) / 2f
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.game_cat_silhouette),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            contentScale = ContentScale.Fit
                        )
                        // placed puzzle pieces on silhouette
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            slotContents[0]?.let {
                                Image(
                                    painter = painterResource(R.drawable.game_cat_piece_head),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(
                                            with(density) { pieceWidthPx(1).toDp() },
                                            with(density) { pieceHeightPx(1).toDp() }
                                        )
                                        .offset(
                                            x = with(density) { pieceOffsetXPx(1).toDp() },
                                            y = with(density) { pieceOffsetYPx(1).toDp() }
                                        ),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            slotContents[1]?.let {
                                Image(
                                    painter = painterResource(R.drawable.game_cat_piece_leg),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(
                                            with(density) { pieceWidthPx(2).toDp() },
                                            with(density) { pieceHeightPx(2).toDp() }
                                        )
                                        .offset(
                                            x = with(density) { pieceOffsetXPx(2).toDp() },
                                            y = with(density) { pieceOffsetYPx(2).toDp() }
                                        ),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            slotContents[2]?.let {
                                Image(
                                    painter = painterResource(R.drawable.game_cat_piece_body),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(
                                            with(density) { pieceWidthPx(3).toDp() },
                                            with(density) { pieceHeightPx(3).toDp() }
                                        )
                                        .offset(
                                            x = with(density) { pieceOffsetXPx(3).toDp() },
                                            y = with(density) { pieceOffsetYPx(3).toDp() }
                                        ),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    LaunchedEffect(boxWidth, boxHeight) {
                        if (pieceBaseOffsets.isEmpty()) {
                            pieceBaseOffsets[1] = Offset(boxWidth * 0.08f, boxHeight * 0.18f) // head left
                            pieceBaseOffsets[2] = Offset(boxWidth * 0.78f, boxHeight * 0.16f) // middle right-top
                            pieceBaseOffsets[3] = Offset(boxWidth * 0.70f, boxHeight * 0.56f) // bottom right-bottom
                        }
                    }

                    pieces.filter { it.id !in placedPieceIds }.forEach { piece ->
                        var offsetPx by remember { mutableStateOf(Offset.Zero) }
                        val base = pieceBaseOffsets[piece.id] ?: Offset.Zero
                        val startX = base.x
                        val startY = base.y
                        val dragPieceWidthPx = pieceWidthPx(piece.id)
                        val dragPieceHeightPx = pieceHeightPx(piece.id)

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (startX + offsetPx.x).roundToInt(),
                                        (startY + offsetPx.y).roundToInt()
                                    )
                                }
                                .size(
                                    with(density) { dragPieceWidthPx.toDp() },
                                    with(density) { dragPieceHeightPx.toDp() }
                                )
                                .pointerInput(piece.id) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            val bounds = slotBounds.value
                                            val (gx, gy) = gameAreaTopLeft.value
                                            if (bounds.size == 3) {
                                                val targetSlot = piece.correctSlot
                                                val (sx, sy) = bounds[targetSlot]
                                                val pieceCenterX = gx + startX + offsetPx.x + dragPieceWidthPx / 2f
                                                val pieceCenterY = gy + startY + offsetPx.y + dragPieceHeightPx / 2f
                                                val dist = sqrt((pieceCenterX - sx) * (pieceCenterX - sx) + (pieceCenterY - sy) * (pieceCenterY - sy))
                                                if (dist < maxOf(dragPieceWidthPx, dragPieceHeightPx) * 0.70f && slotContents[targetSlot] == null) {
                                                    slotContents = slotContents.toMutableList().apply {
                                                        set(targetSlot, piece.id)
                                                    }
                                                    placedPieceIds = placedPieceIds + piece.id
                                                    checkWin()
                                                }
                                                offsetPx = Offset.Zero
                                            }
                                        }
                                    ) { change, dragAmount ->
                                        offsetPx += dragAmount
                                    }
                                }
                        ) {
                            Image(
                                painter = painterResource(piece.imageRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp, start = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(
                        id = if (gameWon) R.drawable.game_star_filled else R.drawable.game_star_outline
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    contentScale = ContentScale.Fit
                )
                Image(
                    painter = painterResource(R.drawable.game_star_outline),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (gameWon) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Молодец!\nСоберёшь ещё?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color(0xFF7BC144), RoundedCornerShape(24.dp))
                        .padding(horizontal = 40.dp, vertical = 24.dp)
                )
            }
        }

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
                                if (isMuted) mediaPlayer.setVolume(0f, 0f)
                                else mediaPlayer.setVolume(1f, 1f)
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
