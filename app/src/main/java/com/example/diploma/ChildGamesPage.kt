package com.example.diploma

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class GameItem(
    val title: String,
    val route: String,
    val imageRes: Int,
    val variant: String = "default"
)

@Composable
fun ChildGamesPage(navController: NavController) {
    val games = listOf(
        GameItem("\u0415\u0436\u0435\u0434\u043D\u0435\u0432\u043D\u044B\u0435 \u0434\u0435\u043B\u0430", "DailyRoutineGamePage", imageRes = R.drawable.game_card_daily),
        GameItem("\u041C\u043E\u0438 \u044D\u043C\u043E\u0446\u0438\u0438", "EmotionGamePage", imageRes = R.drawable.game_card_emotions),
        GameItem("\u0412\u0435\u0441\u0435\u043B\u044B\u0435 \u0436\u0438\u0432\u043E\u0442\u043D\u044B\u0435", "AssembleAnimalGamePage", imageRes = R.drawable.game_card_animals),
        GameItem("\u041D\u0430\u0440\u0438\u0441\u0443\u0439 \u0424\u0438\u0433\u0443\u0440\u044B", "DrawTriangleSquareGamePage", imageRes = R.drawable.game_card_shapes),
        GameItem("\u041D\u0430\u0439\u0434\u0438 \u0446\u0432\u0435\u0442\u0430", "ColorFinderGamePage", imageRes = R.drawable.game_card_colors, variant = "colors"),
        GameItem("\u0421\u0447\u0438\u0442\u0430\u043B\u043A\u0430", "CountingGamePage", imageRes = R.drawable.game_card_counting, variant = "counting"),
        GameItem("\u0423\u0433\u0430\u0434\u0430\u0439 \u0437\u0432\u0443\u043A", "GuessSoundGamePage", imageRes = R.drawable.game_card_guess_sound, variant = "sound"),
        GameItem("\u0412\u043D\u0438\u0437 \u0438\u043B\u0438 \u0412\u0432\u0435\u0440\u0445", "UpDownGamePage", imageRes = R.drawable.game_card_updown, variant = "updown")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.child_games_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CareSteps",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFFF5A623)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.95f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF667085)
                        )
                    }
                    Text(text = "\uD83C\uDF1E", fontSize = 28.sp)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    games.forEach { game ->
                        GameCardItem(game = game) { navController.navigate(game.route) }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameCardItem(game: GameItem, onClick: () -> Unit) {
    if (game.variant == "colors" || game.variant == "counting" || game.variant == "sound" || game.variant == "updown") {
        val cardBg = when (game.variant) {
            "colors" -> Color(0xFF3E73E9)
            "counting" -> Color(0xFFE05AB7)
            "sound" -> Color(0xFFEA875B)
            "updown" -> Color(0xFF88D72E)
            else -> Color(0xFF3E73E9)
        }
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(196.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(cardBg)
                .border(4.dp, Color.White, RoundedCornerShape(22.dp))
                .clickable { onClick() }
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(136.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF3F3F3))
                        .border(2.dp, cardBg, RoundedCornerShape(18.dp))
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = game.imageRes),
                        contentDescription = game.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = game.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    Image(
        painter = painterResource(id = game.imageRes),
        contentDescription = game.title,
        modifier = Modifier
            .width(160.dp)
            .height(196.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentScale = ContentScale.FillBounds
    )
}

private data class CountingRoundState(
    val targetCount: Int,
    val options: List<Int>
)

private fun buildCountingOptions(correct: Int): List<Int> {
    val pool = (1..8).filter { it != correct }.shuffled()
    return listOf(correct, pool[0], pool[1]).shuffled()
}

private fun generateCountingRounds(): List<CountingRoundState> =
    (1..8).shuffled().take(3).map { number ->
        CountingRoundState(targetCount = number, options = buildCountingOptions(number))
    }

@Composable
fun CountingGamePage(navController: NavController) {
    val context = LocalContext.current
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.game_music).apply {
            isLooping = true
        }
    }
    var rounds by remember { mutableStateOf(generateCountingRounds()) }
    var roundIndex by remember { mutableStateOf(0) }
    var wrongAnswers by remember { mutableStateOf(setOf<Int>()) }
    var score by remember { mutableStateOf(0) }
    var isLocked by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        mediaPlayer.start()
        onDispose {
            runCatching {
                mediaPlayer.stop()
                mediaPlayer.release()
            }
        }
    }

    fun restartGame() {
        rounds = generateCountingRounds()
        roundIndex = 0
        wrongAnswers = emptySet()
        score = 0
        isLocked = false
        isPaused = false
        showCelebration = false
        showResult = false
        if (!isMuted) mediaPlayer.start()
    }

    val round = rounds[roundIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFAD7EE), Color(0xFFF7C4E5), Color(0xFFF3B7DE))
                )
            )
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        if (showCelebration) {
            GameBalloonsSuccessOverlay(
                title = "Молодец!",
                onDismiss = {
                    showCelebration = false
                    showResult = true
                }
            )
            return@Box
        }

        if (showResult) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (score == 3) "\u0423\u043C\u043D\u0438\u0447\u043A\u0430!" else "\u0425\u043E\u0440\u043E\u0448\u0430\u044F \u043F\u043E\u043F\u044B\u0442\u043A\u0430!",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE340A4)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "\u0412\u0435\u0440\u043D\u044B\u0445 \u043E\u0442\u0432\u0435\u0442\u043E\u0432: $score \u0438\u0437 3",
                    fontSize = 22.sp,
                    color = Color(0xFF7A2D69),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { idx ->
                        Text(
                            text = if (idx < score) "\u2605" else "\u2606",
                            fontSize = 36.sp,
                            color = if (idx < score) Color(0xFFFFC933) else Color(0xFFD4AFC9)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .clickable { navController.popBackStack() }
                            .padding(horizontal = 22.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("\u041D\u0430\u0437\u0430\u0434", color = Color(0xFF9E4B82), fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFE340A4))
                            .clickable { restartGame() }
                            .padding(horizontal = 22.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("\u0418\u0433\u0440\u0430\u0442\u044C \u0441\u043D\u043E\u0432\u0430", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            return@Box
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        isPaused = true
                        mediaPlayer.pause()
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE94BAE))
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White
                    )
                }
                Text(
                    text = "\u0421\u043A\u043E\u043B\u044C\u043A\u043E \u0443\u0442\u043E\u0447\u0435\u043A?",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFDF5FA),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFFD73D9E),
                            blurRadius = 7f
                        )
                    )
                )
                Box(modifier = Modifier.width(98.dp)) {
                    // Пустое место справа, чтобы заголовок оставался визуально по центру.
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { idx ->
                    Text(
                        text = if (idx < score) "\u2605" else "\u2606",
                        fontSize = 28.sp,
                        color = if (idx < score) Color(0xFFFFCA3A) else Color(0xFFE9C9DD)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEFA1D2))
                    .padding(vertical = 18.dp, horizontal = 16.dp)
            ) {
                val ducks = List(round.targetCount) { "\uD83D\uDC25" }
                val rows = ducks.chunked(4)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rows.forEach { duckRow ->
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            duckRow.forEach { duck ->
                                Text(text = duck, fontSize = 42.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                round.options.forEach { option ->
                    val isWrong = option in wrongAnswers
                    val bubbleColor = if (isWrong) Color(0xFFCFCFD6) else Color.White
                    val textColor = if (isWrong) Color(0xFF8A8A95) else Color(0xFFE340A4)

                    Box(
                        modifier = Modifier
                            .width(108.dp)
                            .height(72.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(bubbleColor)
                            .border(4.dp, Color(0xFFFFF2FA), RoundedCornerShape(28.dp))
                            .clickable(enabled = !isLocked && !isPaused) {
                                if (option == round.targetCount) {
                                    if (!isLocked) {
                                        score += 1
                                        isLocked = true
                                        scope.launch {
                                            delay(400)
                                            if (roundIndex == rounds.lastIndex) {
                                                mediaPlayer.pause()
                                                showCelebration = true
                                            } else {
                                                roundIndex += 1
                                                wrongAnswers = emptySet()
                                                isLocked = false
                                            }
                                        }
                                    }
                                } else {
                                    wrongAnswers = wrongAnswers + option
                                }
                            }
                            .heightIn(min = 72.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.toString(),
                            color = textColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 42.sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0xFFEEAFD8),
                                    blurRadius = 4f
                                )
                            )
                        )
                    }
                }
            }
        }

        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    RoundIconButton(size = 66.dp, onClick = {
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
                    RoundIconButton(size = 74.dp, onClick = {
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
                    RoundIconButton(size = 66.dp, onClick = {
                        isMuted = !isMuted
                        val volume = if (isMuted) 0f else 1f
                        mediaPlayer.setVolume(volume, volume)
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
private fun RoundIconButton(
    size: androidx.compose.ui.unit.Dp = 46.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFE94BAE))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun EmptyComingSoonGamePage(navController: NavController, title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFF6FF))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("\uD83D\uDEE0", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF2D3A55))
            Spacer(modifier = Modifier.height(8.dp))
            Text("\u0418\u0433\u0440\u0430 \u0441\u043A\u043E\u0440\u043E \u043F\u043E\u044F\u0432\u0438\u0442\u0441\u044F", color = Color(0xFF6F7C91))
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2F6FED))
                    .clickable { navController.popBackStack() }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text("\u041D\u0430\u0437\u0430\u0434", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChildGamesPagePreview() {
    ChildGamesPage(navController = rememberNavController())
}
