package com.example.diploma

import android.app.Activity
import android.os.Build
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private enum class AssembleRound { CAT, PARROT }

private data class AnimalPuzzleAssets(
    val silhouetteRes: Int,
    val piece1Res: Int,
    val piece2Res: Int,
    val piece3Res: Int,
    val title: String,
    val background: Color,
    val silW: Float,
    val silH: Float,
    val p1W: Float,
    val p1H: Float,
    val p2W: Float,
    val p2H: Float,
    val p3W: Float,
    val p3H: Float
)

private fun assetsForRound(round: AssembleRound): AnimalPuzzleAssets = when (round) {
    AssembleRound.CAT -> AnimalPuzzleAssets(
        silhouetteRes = R.drawable.game_cat_silhouette,
        piece1Res = R.drawable.game_cat_piece_head,
        piece2Res = R.drawable.game_cat_piece_leg,
        piece3Res = R.drawable.game_cat_piece_body,
        title = "Собери животное!",
        background = Color(0xFFF9EFAE),
        silW = 270f, silH = 285f,
        p1W = 131f, p1H = 176f,
        p2W = 121f, p2H = 163f,
        p3W = 229f, p3H = 155f
    )
    AssembleRound.PARROT -> AnimalPuzzleAssets(
        silhouetteRes = R.drawable.game_parrot_silhouette,
        piece1Res = R.drawable.game_parrot_piece_head,
        piece2Res = R.drawable.game_parrot_piece_wing,
        piece3Res = R.drawable.game_parrot_piece_tail,
        title = "Собери попугая!",
        background = Color(0xFFD8E4A8),
        silW = 293f, silH = 299f,
        p1W = 148f, p1H = 188f,
        p2W = 224f, p2H = 283f,
        p3W = 208f, p3H = 283f
    )
}

private data class MaskData(
    val w: Int,
    val h: Int,
    val alphaMask: BooleanArray,
    val xs: IntArray,
    val ys: IntArray,
    val count: Int
)

private data class Candidate(val x: Int, val y: Int, val score: Int)

data class PuzzlePieceCalibrated(
    val id: Int,
    val imageRes: Int,
    val widthDp: androidx.compose.ui.unit.Dp,
    val heightDp: androidx.compose.ui.unit.Dp,
    val startOffset: DpOffset,
    val targetOffset: DpOffset,
    val currentOffset: DpOffset,
    val isPlaced: Boolean
)

/**
 * Compose [painterResource] does not support XML &lt;bitmap&gt; wrappers — only vectors and raster files.
 * Each puzzle instance needs its own [cacheKey] and a **software** bitmap copy — otherwise several Images can
 * share one GPU-backed bitmap and you get “holes” / wrong pixels after drag-end.
 */
@Composable
private fun PuzzleRasterImage(
    resId: Int,
    cacheKey: Any,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val imageBitmap = remember(cacheKey) {
        decodeBitmap(context, resId).toSoftwareArgbCopy().asImageBitmap()
    }
    Image(
        bitmap = imageBitmap,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

/** Independent ARGB_8888 bitmap safe for Compose [Image] (avoids HARDWARE / shared drawable buffer issues). */
private fun Bitmap.toSoftwareArgbCopy(): Bitmap {
    val base = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && config == Bitmap.Config.HARDWARE) {
        copy(Bitmap.Config.ARGB_8888, true)
    } else {
        this
    }
    return base.copy(Bitmap.Config.ARGB_8888, true)
}

@Composable
fun AssembleAnimalGameCalibratedPage(navController: NavController) {
    val context = LocalContext.current
    val startedAtMs = remember { System.currentTimeMillis() }
    var sessionSaved by remember { mutableStateOf(false) }
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
    var round by remember { mutableStateOf(AssembleRound.CAT) }
    var showSuccessOverlay by remember { mutableStateOf(false) }
    var catRoundDone by remember { mutableStateOf(false) }
    var parrotRoundDone by remember { mutableStateOf(false) }

    val puzzleAssets = remember(round) { assetsForRound(round) }

    DisposableEffect(Unit) {
        val previousOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        mediaPlayer.start()
        onDispose {
            if (!sessionSaved) {
                val durationSec = ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt()
                GameStatsRepository.recordSession(
                    context = context,
                    gameTitle = "Веселые животные",
                    durationSeconds = durationSec,
                    won = false
                )
                sessionSaved = true
            }
            mediaPlayer.stop()
            mediaPlayer.release()
            if (previousOrientation != null) {
                activity.requestedOrientation = previousOrientation
            }
        }
    }

    var calibratedTargets by remember { mutableStateOf<Map<Int, Pair<Int, Int>>?>(null) }

    LaunchedEffect(round) {
        calibratedTargets = null
        calibratedTargets = withContext(Dispatchers.Default) {
            calibrateTargetsFor(
                context,
                puzzleAssets.silhouetteRes,
                puzzleAssets.piece1Res,
                puzzleAssets.piece2Res,
                puzzleAssets.piece3Res
            )
        }
    }

    var pieces by remember(round) { mutableStateOf(emptyList<PuzzlePieceCalibrated>()) }
    var draggingId by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(puzzleAssets.background)
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
                    text = puzzleAssets.title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF0B300)
                )
                Box(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val boxWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                val boxHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

                val designSilW = puzzleAssets.silW
                val designSilH = puzzleAssets.silH
                val headW = puzzleAssets.p1W
                val headH = puzzleAssets.p1H
                val midW = puzzleAssets.p2W
                val midH = puzzleAssets.p2H
                val legsW = puzzleAssets.p3W
                val legsH = puzzleAssets.p3H

                val gap = 16f
                val totalDesignW = maxOf(headW, legsW) + gap + designSilW + gap + maxOf(midW, legsW)
                val totalDesignH = maxOf(designSilH, headH, midH, legsH)
                val scale = min(boxWidthPx / totalDesignW, boxHeightPx / totalDesignH) * 0.95f

                val silWpx = designSilW * scale
                val silHpx = designSilH * scale
                val silhouetteMask = remember(round) {
                    buildMaskData(decodeBitmap(context, puzzleAssets.silhouetteRes), alphaThreshold = 10)
                }

                val silLeftPx = (boxWidthPx - silWpx) / 2f
                val silTopPx = (boxHeightPx - silHpx) / 2f

                fun pxToDp(px: Float) = with(density) { px.toDp() }
                val silLeftDp = pxToDp(silLeftPx)
                val silTopDp = pxToDp(silTopPx)

                val headPieceWdp = pxToDp(headW * scale)
                val headPieceHdp = pxToDp(headH * scale)
                val midPieceWdp = pxToDp(midW * scale)
                val midPieceHdp = pxToDp(midH * scale)
                val legsPieceWdp = pxToDp(legsW * scale)
                val legsPieceHdp = pxToDp(legsH * scale)

                // Piece 1 (head): top-left corner
                val headStart = DpOffset(
                    x = pxToDp(4f),
                    y = pxToDp(4f)
                )
                // Piece 2 (wing): top-right corner
                val midStart = DpOffset(
                    x = pxToDp(boxWidthPx - midW * scale - 4f),
                    y = pxToDp(4f)
                )
                // Piece 3 (tail): bottom-right corner
                val legsStart = DpOffset(
                    x = pxToDp(boxWidthPx - legsW * scale - 4f),
                    y = pxToDp(boxHeightPx - legsH * scale - 4f)
                )

                val targets = calibratedTargets

                fun targetOrStart(pieceId: Int, startOffset: DpOffset): DpOffset {
                    if (targets == null) return startOffset
                    val v = targets[pieceId] ?: (0 to 0)
                    return DpOffset(
                        x = silLeftDp + pxToDp(v.first.toFloat() * scale),
                        y = silTopDp + pxToDp(v.second.toFloat() * scale)
                    )
                }

                if (pieces.isEmpty()) {
                    // Initial render: even without calibration we can show pieces.
                    pieces = listOf(
                        PuzzlePieceCalibrated(
                            id = 1,
                            imageRes = puzzleAssets.piece1Res,
                            widthDp = headPieceWdp,
                            heightDp = headPieceHdp,
                            startOffset = headStart,
                            targetOffset = targetOrStart(1, headStart),
                            currentOffset = headStart,
                            isPlaced = false
                        ),
                        PuzzlePieceCalibrated(
                            id = 2,
                            imageRes = puzzleAssets.piece2Res,
                            widthDp = midPieceWdp,
                            heightDp = midPieceHdp,
                            startOffset = midStart,
                            targetOffset = targetOrStart(2, midStart),
                            currentOffset = midStart,
                            isPlaced = false
                        ),
                        PuzzlePieceCalibrated(
                            id = 3,
                            imageRes = puzzleAssets.piece3Res,
                            widthDp = legsPieceWdp,
                            heightDp = legsPieceHdp,
                            startOffset = legsStart,
                            targetOffset = targetOrStart(3, legsStart),
                            currentOffset = legsStart,
                            isPlaced = false
                        )
                    )
                } else if (targets != null) {
                    val needsUpdate = pieces.any { p ->
                        val newTarget = targetOrStart(p.id, p.startOffset)
                        p.targetOffset != newTarget
                    }
                    if (needsUpdate) {
                        pieces = pieces.map { p ->
                            p.copy(targetOffset = targetOrStart(p.id, p.startOffset))
                        }
                    }
                }

                // Silhouette: fixed in the center, never draggable. zIndex 0 so pieces (zIndex 1) get touches first.
                PuzzleRasterImage(
                    resId = puzzleAssets.silhouetteRes,
                    cacheKey = round to puzzleAssets.silhouetteRes,
                    contentDescription = null,
                    modifier = Modifier
                        .zIndex(0f)
                        .offset(x = silLeftDp, y = silTopDp)
                        .size(width = pxToDp(silWpx), height = pxToDp(silHpx)),
                    contentScale = ContentScale.Fit
                )

                if (pieces.isNotEmpty()) {
                    pieces.forEach { p ->
                        val isLocked = p.isPlaced
                        val isOtherDragging = draggingId != null && draggingId != p.id
                        val canDrag = !isPaused && !showSuccessOverlay && !isLocked && !isOtherDragging

                        PuzzleRasterImage(
                            resId = p.imageRes,
                            cacheKey = Triple(round, p.id, p.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .zIndex(1f)
                                .offset(x = p.currentOffset.x, y = p.currentOffset.y)
                                .size(width = p.widthDp, height = p.heightDp)
                                .then(
                                    if (!canDrag) Modifier
                                    else Modifier.pointerInput(p.id) {
                                        detectDragGestures(
                                            onDragStart = { draggingId = p.id },
                                            onDragEnd = {
                                                val current = pieces.firstOrNull { it.id == p.id } ?: return@detectDragGestures
                                                val currentCenterX = with(density) {
                                                    current.currentOffset.x.toPx() + current.widthDp.toPx() / 2f
                                                }
                                                val currentCenterY = with(density) {
                                                    current.currentOffset.y.toPx() + current.heightDp.toPx() / 2f
                                                }

                                                // Child can place a piece anywhere inside the silhouette area.
                                                // Map screen position to bitmap pixels (mask.w/h = real PNG size, not design 270×285).
                                                val nx = ((currentCenterX - silLeftPx) / silWpx.coerceAtLeast(1f) * silhouetteMask.w)
                                                    .roundToInt()
                                                    .coerceIn(0, silhouetteMask.w - 1)
                                                val ny = ((currentCenterY - silTopPx) / silHpx.coerceAtLeast(1f) * silhouetteMask.h)
                                                    .roundToInt()
                                                    .coerceIn(0, silhouetteMask.h - 1)
                                                val searchRadius = max(3, min(silhouetteMask.w, silhouetteMask.h) / 60)
                                                val insideSilhouette = hasOpaquePixelNear(silhouetteMask, nx, ny, searchRadius)

                                                val newPieces = pieces.map { pp ->
                                                    if (pp.id != p.id) pp
                                                    else {
                                                        if (insideSilhouette) {
                                                            pp.copy(
                                                                currentOffset = current.currentOffset,
                                                                isPlaced = true
                                                            )
                                                        } else {
                                                            pp.copy(
                                                                currentOffset = pp.startOffset,
                                                                isPlaced = false
                                                            )
                                                        }
                                                    }
                                                }

                                                pieces = newPieces
                                                if (newPieces.all { it.isPlaced }) {
                                                    showSuccessOverlay = true
                                                    when (round) {
                                                        AssembleRound.CAT -> catRoundDone = true
                                                        AssembleRound.PARROT -> parrotRoundDone = true
                                                    }
                                                }
                                                draggingId = null
                                            },
                                            onDragCancel = { draggingId = null }
                                        ) { _, dragAmount ->
                                            if (isPaused || showSuccessOverlay) return@detectDragGestures
                                            val dx = with(density) { dragAmount.x.toDp() }
                                            val dy = with(density) { dragAmount.y.toDp() }
                                            pieces = pieces.map { pp ->
                                                if (pp.id != p.id) pp
                                                else if (pp.isPlaced) pp
                                                else pp.copy(currentOffset = DpOffset(pp.currentOffset.x + dx, pp.currentOffset.y + dy))
                                            }
                                        }
                                    }
                                )
                        )
                    }
                }
            }

            // Stars bottom-left
            Row(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 18.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(if (catRoundDone) R.drawable.game_star_filled else R.drawable.game_star_outline),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit
                )
                Image(
                    painter = painterResource(if (parrotRoundDone) R.drawable.game_star_filled else R.drawable.game_star_outline),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (showSuccessOverlay) {
            GameBalloonsSuccessOverlay(
                title = "Молодец!",
                onDismiss = {
                    showSuccessOverlay = false
                    when (round) {
                        AssembleRound.CAT -> round = AssembleRound.PARROT
                        AssembleRound.PARROT -> {
                            if (!sessionSaved) {
                                val durationSec = ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt()
                                GameStatsRepository.recordSession(
                                    context = context,
                                    gameTitle = "Веселые животные",
                                    durationSeconds = durationSec,
                                    won = true
                                )
                                sessionSaved = true
                            }
                            navController.popBackStack()
                        }
                    }
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

private fun drawableIntrinsicSize(context: Context, resId: Int): Pair<Float, Float> {
    val d = ContextCompat.getDrawable(context, resId) ?: return 270f to 285f
    val w = d.intrinsicWidth.takeIf { it > 0 } ?: 270
    val h = d.intrinsicHeight.takeIf { it > 0 } ?: 285
    return w.toFloat() to h.toFloat()
}

private fun calibrateTargetsFor(
    context: Context,
    silhouetteRes: Int,
    piece1Res: Int,
    piece2Res: Int,
    piece3Res: Int
): Map<Int, Pair<Int, Int>> {
    val silBitmap = decodeBitmap(context, silhouetteRes)
    val headBitmap = decodeBitmap(context, piece1Res)
    val midBitmap = decodeBitmap(context, piece2Res)
    val legsBitmap = decodeBitmap(context, piece3Res)

    val silMask = buildMaskData(silBitmap, alphaThreshold = 10)
    val silColorMask = buildColorMaskData(silBitmap, alphaThreshold = 10)
    val headMask = buildMaskData(headBitmap, alphaThreshold = 10)
    val midMask = buildMaskData(midBitmap, alphaThreshold = 10)
    val legsMask = buildMaskData(legsBitmap, alphaThreshold = 10)

    val headColorMask = buildColorMaskData(headBitmap, alphaThreshold = 10)
    val midColorMask = buildColorMaskData(midBitmap, alphaThreshold = 10)
    val legsColorMask = buildColorMaskData(legsBitmap, alphaThreshold = 10)

    val headCandidates = findTopCandidates(
        sil = silMask,
        piece = headMask,
        silColor = silColorMask,
        pieceColor = headColorMask,
        topK = 25
    )
    val midCandidates = findTopCandidates(
        sil = silMask,
        piece = midMask,
        silColor = silColorMask,
        pieceColor = midColorMask,
        topK = 25
    )
    val legsCandidates = findTopCandidates(
        sil = silMask,
        piece = legsMask,
        silColor = silColorMask,
        pieceColor = legsColorMask,
        topK = 25
    )

    val overlapPenalty = 350 // penalize piece-pixel overlaps strongly
    var bestTotal = Int.MIN_VALUE
    var best: Triple<Pair<Int, Int>, Pair<Int, Int>, Pair<Int, Int>>? = null

    for (hc in headCandidates) {
        for (mc in midCandidates) {
            val overlapHM = overlapCount(headMask, hc.x, hc.y, midMask, mc.x, mc.y)
            // prune early
            if (overlapHM > 0 && hc.score < headMask.count * 0.9) continue

            for (lc in legsCandidates) {
                val overlapHL = overlapCount(headMask, hc.x, hc.y, legsMask, lc.x, lc.y)
                val overlapML = overlapCount(midMask, mc.x, mc.y, legsMask, lc.x, lc.y)

                val total =
                    hc.score + mc.score + lc.score -
                        overlapPenalty * (overlapHM + overlapHL + overlapML)

                if (total > bestTotal) {
                    bestTotal = total
                    best = Triple(
                        hc.x to hc.y,
                        mc.x to mc.y,
                        lc.x to lc.y
                    )
                }
            }
        }
    }

    val result = best ?: Triple(0 to 0, 0 to 0, 0 to 0)
    return mapOf(
        1 to result.first,
        2 to result.second,
        3 to result.third
    )
}

/**
 * Works for PNG and for XML drawables (e.g. &lt;bitmap&gt; wrappers). [BitmapFactory.decodeResource] returns null for many XML resources.
 */
private fun decodeBitmap(context: Context, resId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, resId)
        ?: error("Drawable not found for resId=$resId")
    if (drawable is BitmapDrawable) {
        val bmp = drawable.bitmap
        if (bmp != null && !bmp.isRecycled) {
            return bmp.copy(Bitmap.Config.ARGB_8888, false)
        }
    }
    val w = drawable.intrinsicWidth.takeIf { it > 0 } ?: 270
    val h = drawable.intrinsicHeight.takeIf { it > 0 } ?: 285
    return drawable.toBitmap(w, h, Bitmap.Config.ARGB_8888)
}

private fun buildMaskData(bitmap: Bitmap, alphaThreshold: Int): MaskData {
    val w = bitmap.width
    val h = bitmap.height
    val alphaMask = BooleanArray(w * h)
    val xsList = ArrayList<Int>()
    val ysList = ArrayList<Int>()

    val pixels = IntArray(w * h)
    bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

    for (y in 0 until h) {
        for (x in 0 until w) {
            val idx = y * w + x
            val a = (pixels[idx] ushr 24) and 0xFF
            if (a > alphaThreshold) {
                alphaMask[idx] = true
                xsList.add(x)
                ysList.add(y)
            }
        }
    }

    val xs = IntArray(xsList.size)
    val ys = IntArray(ysList.size)
    for (i in xs.indices) {
        xs[i] = xsList[i]
        ys[i] = ysList[i]
    }
    return MaskData(w, h, alphaMask, xs, ys, xs.size)
}

private fun buildColorMaskData(bitmap: Bitmap, alphaThreshold: Int): MaskData {
    val w = bitmap.width
    val h = bitmap.height
    val colorMask = BooleanArray(w * h)
    val xsList = ArrayList<Int>()
    val ysList = ArrayList<Int>()

    val pixels = IntArray(w * h)
    bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

    for (y in 0 until h) {
        for (x in 0 until w) {
            val idx = y * w + x
            val p = pixels[idx]
            val a = (p ushr 24) and 0xFF
            if (a <= alphaThreshold) continue

            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val b = p and 0xFF

            // Keep only "colored" pixels: ignore near-white/gray pixels.
            val maxC = maxOf(r, g, b)
            val minC = minOf(r, g, b)
            val chroma = maxC - minC
            val isNearWhite = maxC >= 220 && minC >= 200 && chroma <= 22
            val isColored = chroma >= 28 && !isNearWhite

            if (isColored) {
                colorMask[idx] = true
                xsList.add(x)
                ysList.add(y)
            }
        }
    }

    val xs = IntArray(xsList.size)
    val ys = IntArray(ysList.size)
    for (i in xs.indices) {
        xs[i] = xsList[i]
        ys[i] = ysList[i]
    }
    return MaskData(w, h, colorMask, xs, ys, xs.size)
}

private fun findTopCandidates(
    sil: MaskData,
    piece: MaskData,
    silColor: MaskData,
    pieceColor: MaskData,
    topK: Int
): List<Candidate> {
    val xMax = sil.w - piece.w
    val yMax = sil.h - piece.h
    if (xMax < 0 || yMax < 0) return emptyList()

    val candidates = ArrayList<Candidate>()
    fun addCandidate(x: Int, y: Int, score: Int) {
        if (candidates.size < topK) {
            candidates.add(Candidate(x, y, score))
        } else {
            var minIdx = 0
            var minScore = candidates[0].score
            for (i in 1 until candidates.size) {
                val s = candidates[i].score
                if (s < minScore) {
                    minScore = s
                    minIdx = i
                }
            }
            if (score > minScore) {
                candidates[minIdx] = Candidate(x, y, score)
            }
        }
    }

    for (y in 0..yMax) {
        for (x in 0..xMax) {
            var alphaScore = 0
            for (i in 0 until piece.count) {
                val px = x + piece.xs[i]
                val py = y + piece.ys[i]
                if (sil.alphaMask[py * sil.w + px]) alphaScore++
            }

            var colorScore = 0
            for (i in 0 until pieceColor.count) {
                val px = x + pieceColor.xs[i]
                val py = y + pieceColor.ys[i]
                if (silColor.alphaMask[py * silColor.w + px]) colorScore++
            }

            // Color matches must dominate for accurate piece placement on highlighted regions.
            val score = alphaScore + colorScore * 8
            addCandidate(x, y, score)
        }
    }

    return candidates.sortedByDescending { it.score }
}

/** True if silhouette mask has an opaque pixel near (bx, by) — forgiving for finger / scaling. */
private fun hasOpaquePixelNear(mask: MaskData, bx: Int, by: Int, radius: Int): Boolean {
    val w = mask.w
    val h = mask.h
    val r = radius.coerceAtLeast(1)
    for (dy in -r..r) {
        for (dx in -r..r) {
            val x = bx + dx
            val y = by + dy
            if (x in 0 until w && y in 0 until h && mask.alphaMask[y * w + x]) return true
        }
    }
    return false
}

private fun overlapCount(a: MaskData, aX: Int, aY: Int, b: MaskData, bX: Int, bY: Int): Int {
    // Count how many non-transparent pixels overlap when:
    // - piece A is placed at (aX, aY) in silhouette coordinate space
    // - piece B is placed at (bX, bY) in the same coordinate space
    var overlap = 0
    for (i in 0 until a.count) {
        val absX = aX + a.xs[i]
        val absY = aY + a.ys[i]

        val localBX = absX - bX
        val localBY = absY - bY

        if (localBX in 0 until b.w && localBY in 0 until b.h) {
            if (b.alphaMask[localBY * b.w + localBX]) overlap++
        }
    }
    return overlap
}

