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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import kotlin.math.sqrt

private enum class DrawStage { TRIANGLE, SQUARE }

private data class TargetShape(
    val stage: DrawStage,
    val dots: List<DrawDot>,
    val mainDotIndices: List<Int>,
    val edgeDotGroups: List<List<Int>>,
    val outerPath: Path,
    val innerPath: Path,
    val outerStrokeWidth: Float,
    val innerStrokeWidth: Float,
    val outerColor: Color,
    val innerColor: Color
)

private data class DrawDot(
    val center: Offset,
    val radius: Float,
    val color: Color
)

@Composable
fun DrawTriangleSquareGamePage(navController: NavController) {
    val context = LocalContext.current
    val startedAtMs = remember { System.currentTimeMillis() }
    var sessionSaved by remember { mutableStateOf(false) }
    val activity = context.findActivity()
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.game_music).apply {
            isLooping = true
            seekTo(2000)
        }
    }

    var isPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var currentStage by remember { mutableStateOf(DrawStage.TRIANGLE) }
    var triangleDone by remember { mutableStateOf(false) }
    var rectangleDone by remember { mutableStateOf(false) }
    var showTriangleSuccess by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val previousOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        mediaPlayer.start()
        onDispose {
            if (!sessionSaved) {
                val durationSec = ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt()
                GameStatsRepository.recordSession(
                    context = context,
                    gameTitle = "Нарисуй фигуры",
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

    val title = when (currentStage) {
        DrawStage.TRIANGLE -> "Нарисуй треугольник"
        DrawStage.SQUARE -> "Нарисуй прямоугольник"
    }

    val star1Filled = triangleDone
    val star2Filled = rectangleDone

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAD4FD))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Нарисуй фигуры",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.align(Alignment.Start)
            )

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
                        modifier = Modifier.size(22.dp),
                        colorFilter = null
                    )
                }

                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Box(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            DrawArea(
                navController = navController,
                stage = currentStage,
                isPaused = isPaused,
                onStageCompleted = {
                    if (currentStage == DrawStage.TRIANGLE) {
                        triangleDone = true
                        showTriangleSuccess = true
                    }
                },
                onAllCompleted = {
                    rectangleDone = true
                }
            )
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
                painter = painterResource(
                    id = if (star1Filled) R.drawable.game_star_filled else R.drawable.game_star_outline
                ),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                colorFilter = null
            )
            Image(
                painter = painterResource(
                    id = if (star2Filled) R.drawable.game_star_filled else R.drawable.game_star_outline
                ),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                colorFilter = null
            )
        }

        if (triangleDone && showTriangleSuccess) {
            // Show success after 1st part, then move to 2nd stage.
            GameBalloonsSuccessOverlay(onDismiss = {
                showTriangleSuccess = false
                currentStage = DrawStage.SQUARE
            })
        }

        if (rectangleDone) {
            GameBalloonsSuccessOverlay(onDismiss = {
                if (!sessionSaved) {
                    val durationSec = ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt()
                    GameStatsRepository.recordSession(
                        context = context,
                        gameTitle = "Нарисуй фигуры",
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
                    navController.navigate("ChildGamesPage") {
                        popUpTo("ChildGamesPage") { inclusive = true }
                    }
                },
                onToggleSound = {
                    isMuted = !isMuted
                    if (isMuted) mediaPlayer.setVolume(0f, 0f) else mediaPlayer.setVolume(1f, 1f)
                }
            )
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

@Composable
private fun DrawArea(
    navController: NavController,
    stage: DrawStage,
    isPaused: Boolean,
    onStageCompleted: () -> Unit,
    onAllCompleted: () -> Unit
) {
    var canvasSize by remember { mutableStateOf(IntSize(0, 0)) }
    val drawnPoints = remember { mutableStateListOf<Offset>() }

    var coveredCount by remember { mutableStateOf(0) }
    var totalSamples by remember { mutableStateOf(1) }
    var stepCompleted by remember { mutableStateOf(false) }

    // Rebuild target when stage or canvas size changes.
    val target = remember(stage, canvasSize) {
        buildTarget(stage, canvasSize)
    }

    // Boolean coverage array is recreated on each stage/size change.
    val covered = remember(stage, canvasSize) { BooleanArray(target.dots.size) }

    LaunchedEffect(stage, canvasSize) {
        drawnPoints.clear()
        for (i in covered.indices) covered[i] = false
        coveredCount = 0
        totalSamples = maxOf(1, target.dots.size)
        stepCompleted = false
    }

    val hitRadiusPx = 10f // дополнительная "погрешность" по касанию

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .onSizeChanged { canvasSize = it }
            .pointerInput(stage, isPaused) {
                detectDragGestures(
                    onDragStart = { start ->
                        if (isPaused) return@detectDragGestures
                        if (stepCompleted) return@detectDragGestures
                        drawnPoints.add(start)
                    },
                    onDrag = { change, _ ->
                        if (isPaused) return@detectDragGestures
                        if (stepCompleted) return@detectDragGestures
                        val p = change.position
                        drawnPoints.add(p)

                        // Update coverage based on hitting the design dots.
                        for (i in target.dots.indices) {
                            if (covered[i]) continue
                            val dot = target.dots[i]
                            val d = hypot(p.x - dot.center.x, p.y - dot.center.y)
                            val threshold = dot.radius * 0.95f + hitRadiusPx * 0.20f
                            if (d <= threshold) {
                                covered[i] = true
                                coveredCount++
                            }
                        }

                        val mainComplete = target.mainDotIndices.isNotEmpty() && target.mainDotIndices.all { covered[it] }

                        // Не допускаем "пустую" сторону возле вершин (проверяем концы),
                        // но допускаем небольшие пропуски в середине ребра.
                        val requiredEdgeRatio = 0.70f
                        var edgesOk = true
                        for (group in target.edgeDotGroups) {
                            if (group.isEmpty()) continue
                            val ratio = group.count { covered[it] }.toFloat() / group.size.toFloat()
                            val endsCovered = covered[group.first()] && covered[group.last()]
                            if (!(ratio >= requiredEdgeRatio && endsCovered)) {
                                edgesOk = false
                                break
                            }
                        }

                        if (mainComplete && edgesOk) {
                            stepCompleted = true
                            drawnPoints.clear()
                            if (stage == DrawStage.TRIANGLE) onStageCompleted() else onAllCompleted()
                        }
                    },
                    onDragEnd = {
                        // nothing
                    }
                )
            }
    ) {
        // Outer + inner triangle (double outline)
        val outerShadowAlpha = if (stage == DrawStage.SQUARE) 0.10f else 0.18f
        val outerShadow = target.outerColor.copy(alpha = outerShadowAlpha)
        drawPath(
            path = target.outerPath,
            color = outerShadow,
            style = Stroke(width = target.outerStrokeWidth * 1.45f, cap = StrokeCap.Round)
        )
        drawPath(
            path = target.outerPath,
            color = target.outerColor,
            style = Stroke(width = target.outerStrokeWidth, cap = StrokeCap.Round)
        )

        // Design: for the triangle we use double outline, for the rectangle/square only outer outline.
        if (stage == DrawStage.TRIANGLE) {
            drawPath(
                path = target.innerPath,
                color = target.innerColor,
                style = Stroke(width = target.innerStrokeWidth, cap = StrokeCap.Round)
            )
        }

        target.dots.forEach { dot ->
            // Dots in the design have a darker inner circle (big + small).
            val innerDotColor = if (stage == DrawStage.SQUARE) Color(0xFFE54857) else Color(0xFFD08B00)
            drawCircle(color = dot.color, radius = dot.radius, center = dot.center)
            val innerScale = if (dot.radius >= 8f) 0.45f else 0.38f
            drawCircle(
                color = innerDotColor,
                radius = dot.radius * innerScale,
                center = dot.center
            )
        }

        // Draw user drawn stroke on top.
        if (drawnPoints.size >= 2) {
            val path = Path().apply {
                moveTo(drawnPoints.first().x, drawnPoints.first().y)
                for (p in drawnPoints.drop(1)) lineTo(p.x, p.y)
            }
            drawPath(path, color = Color.Black, style = Stroke(width = 14f, cap = StrokeCap.Round))
        }
    }
}

private fun buildTarget(stage: DrawStage, size: IntSize): TargetShape {
    val w = size.width.toFloat()
    val h = size.height.toFloat()
    if (w <= 1f || h <= 1f) {
        return TargetShape(
            stage = stage,
            dots = emptyList(),
            mainDotIndices = emptyList(),
            edgeDotGroups = emptyList(),
            outerPath = Path(),
            innerPath = Path(),
            outerStrokeWidth = 0f,
            innerStrokeWidth = 0f,
            outerColor = Color.Transparent,
            innerColor = Color.Transparent
        )
    }

    return when (stage) {
        DrawStage.TRIANGLE -> {
            val minSize = min(w, h)

            // Use equilateral geometry so the triangle looks correct on different aspect ratios.
            val side = minSize * 0.75f
            val triHeight = side * (sqrt(3f) / 2f)

            // Slightly lower centroid matches the screenshot layout in the canvas.
            val centroid = Offset(w / 2f, h / 2f + minSize * 0.025f)

            val top = Offset(centroid.x, centroid.y - triHeight * (2f / 3f))
            val left = Offset(centroid.x - side / 2f, centroid.y + triHeight * (1f / 3f))
            val right = Offset(centroid.x + side / 2f, centroid.y + triHeight * (1f / 3f))

            val outerPath = Path().apply {
                moveTo(left.x, left.y)
                lineTo(top.x, top.y)
                lineTo(right.x, right.y)
                close()
            }

            // Design triangle border is noticeably thicker.
            val outerStrokeWidth = minSize * 0.065f
            val innerStrokeWidth = outerStrokeWidth * 0.68f

            // Inner outline is a scaled similar triangle to keep proportions correct.
            val centroidToVertex = triHeight * (2f / 3f)
            val insetFromVertex = outerStrokeWidth * 1.55f
            val k = ((centroidToVertex - insetFromVertex) / centroidToVertex).coerceIn(0.75f, 0.95f)

            val innerTop = Offset(
                centroid.x + (top.x - centroid.x) * k,
                centroid.y + (top.y - centroid.y) * k
            )
            val innerLeft = Offset(
                centroid.x + (left.x - centroid.x) * k,
                centroid.y + (left.y - centroid.y) * k
            )
            val innerRight = Offset(
                centroid.x + (right.x - centroid.x) * k,
                centroid.y + (right.y - centroid.y) * k
            )
            val innerPath = Path().apply {
                moveTo(innerLeft.x, innerLeft.y)
                lineTo(innerTop.x, innerTop.y)
                lineTo(innerRight.x, innerRight.y)
                close()
            }

            val outerColor = Color(0xFFF3B000)
            val innerColor = Color(0xFFFFC247)
            // Small dots use the same outer tone as the border in the design.
            val bigDotColor = Color(0xFFF0B300)
            val smallDotColor = bigDotColor

            val dots = ArrayList<DrawDot>(24)
            val bigDotRadius = minSize * 0.031f
            val smallDotRadius = minSize * 0.021f

            // Vertex (big) dots
            dots.add(DrawDot(top, radius = bigDotRadius, color = bigDotColor)) // index 0
            dots.add(DrawDot(left, radius = bigDotRadius, color = bigDotColor)) // index 1
            dots.add(DrawDot(right, radius = bigDotRadius, color = bigDotColor)) // index 2

            val mainDotIndices = listOf(0, 1, 2)

            // Edge dots excluding vertices.
            fun addEdgeDots(a: Offset, b: Offset, count: Int) {
                for (i in 1..count) {
                    val t = i.toFloat() / (count + 1).toFloat()
                    val p = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
                    dots.add(DrawDot(p, radius = smallDotRadius, color = smallDotColor))
                }
            }

            // Numbers tuned to match the screenshot distribution (big vertices + dots on edges).
            addEdgeDots(left, top, count = 4)
            addEdgeDots(top, right, count = 4)
            addEdgeDots(left, right, count = 7)

            // Edge dot groups (exclude vertex dots).
            // After 3 vertex dots: edge1(4), edge2(4), edge3(7).
            val edgeDotGroups = listOf(
                listOf(3, 4, 5, 6),        // left -> top
                listOf(7, 8, 9, 10),       // top -> right
                listOf(11, 12, 13, 14, 15, 16, 17) // left -> right
            )

            TargetShape(
                stage = stage,
                dots = dots,
                mainDotIndices = mainDotIndices,
                edgeDotGroups = edgeDotGroups,
                outerPath = outerPath,
                innerPath = innerPath,
                outerStrokeWidth = outerStrokeWidth,
                innerStrokeWidth = innerStrokeWidth,
                outerColor = outerColor,
                innerColor = innerColor
            )
        }

        DrawStage.SQUARE -> {
            val minSize = min(w, h)

            // Centered rectangle for the second stage.
            val rectWidth = minSize * 0.62f
            val rectHeight = minSize * 0.42f
            val rectLeft = (w - rectWidth) / 2f
            val rectRight = rectLeft + rectWidth

            val rectCenterY = h / 2f + minSize * 0.03f
            val rectTop = rectCenterY - rectHeight / 2f
            val rectBottom = rectTop + rectHeight

            val outerPath = Path().apply {
                addRect(Rect(rectLeft, rectTop, rectRight, rectBottom))
            }

            val outerStrokeWidth = minSize * 0.072f
            val innerStrokeWidth = outerStrokeWidth * 0.58f

            val inset = outerStrokeWidth * 1.35f
            val innerPath = Path().apply {
                addRect(Rect(rectLeft + inset, rectTop + inset, rectRight - inset, rectBottom - inset))
            }

            val outerColor = Color(0xFFE85663)
            val innerColor = Color(0xFFF8C8CF)

            val bigDotColor = Color(0xFFF07F8C)
            val smallDotColor = Color(0xFFE85663)

            val dots = ArrayList<DrawDot>(32)
            val bigDotRadius = minSize * 0.032f
            val smallDotRadius = minSize * 0.021f

            val tl = Offset(rectLeft, rectTop)
            val tr = Offset(rectRight, rectTop)
            val bl = Offset(rectLeft, rectBottom)
            val br = Offset(rectRight, rectBottom)

            // Corner (big) dots
            dots.add(DrawDot(tl, radius = bigDotRadius, color = bigDotColor)) // index 0
            dots.add(DrawDot(tr, radius = bigDotRadius, color = bigDotColor)) // index 1
            dots.add(DrawDot(bl, radius = bigDotRadius, color = bigDotColor)) // index 2
            dots.add(DrawDot(br, radius = bigDotRadius, color = bigDotColor)) // index 3

            val mainDotIndices = listOf(0, 1, 2, 3)

            fun addEdgeDots(a: Offset, b: Offset, count: Int) {
                for (i in 1..count) {
                    val t = i.toFloat() / (count + 1).toFloat()
                    val p = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
                    dots.add(DrawDot(p, radius = smallDotRadius, color = smallDotColor))
                }
            }

            // Side dots excluding corners.
            addEdgeDots(tl, tr, count = 4)
            addEdgeDots(tr, br, count = 4)
            addEdgeDots(bl, br, count = 4)
            addEdgeDots(tl, bl, count = 4)

            // Edge dot groups (each side has 4 dots, corners excluded).
            // Total dots: 4 corners + 4*4 edges = 20.
            val edgeDotGroups = listOf(
                listOf(4, 5, 6, 7),    // top edge
                listOf(8, 9, 10, 11),  // right edge
                listOf(12, 13, 14, 15),// bottom edge
                listOf(16, 17, 18, 19) // left edge
            )

            TargetShape(
                stage = stage,
                dots = dots,
                mainDotIndices = mainDotIndices,
                edgeDotGroups = edgeDotGroups,
                outerPath = outerPath,
                innerPath = innerPath,
                outerStrokeWidth = outerStrokeWidth,
                innerStrokeWidth = innerStrokeWidth,
                outerColor = outerColor,
                innerColor = innerColor
            )
        }
    }
}

private fun samplePolyline(points: List<Offset>, step: Float): List<Offset> {
    if (points.size < 2) return emptyList()
    val out = ArrayList<Offset>()
    for (seg in 0 until points.size - 1) {
        val a = points[seg]
        val b = points[seg + 1]
        val dx = b.x - a.x
        val dy = b.y - a.y
        val dist = hypot(dx, dy)
        if (dist == 0f) continue
        val count = (dist / step).toInt().coerceAtLeast(1)
        for (i in 0..count) {
            val t = i.toFloat() / count.toFloat()
            out.add(Offset(a.x + dx * t, a.y + dy * t))
        }
    }
    return out.distinctBy { Pair(it.x.roundToInt(), it.y.roundToInt()) }
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
            Image(
                painter = painterResource(id = R.drawable.ic_home_game),
                contentDescription = "Домой",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable { onHome() },
                contentScale = ContentScale.Fit
            )
            Image(
                painter = painterResource(id = R.drawable.ic_play_game),
                contentDescription = "Продолжить",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .clickable { onResume() },
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
                    .clickable { onToggleSound() },
                contentScale = ContentScale.Fit
            )
        }
    }
}

