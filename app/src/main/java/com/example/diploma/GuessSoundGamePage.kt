package kz.aruzhan.care_steps

import android.media.MediaPlayer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private data class GuessSoundOption(
    val id: Int,
    val imageName: String
)

private data class GuessSoundRound(
    val soundResName: String,
    val speakerImageName: String,
    val correctOptionId: Int,
    val options: List<GuessSoundOption>
)

@Composable
fun GuessSoundGamePage(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Если у тебя другие названия файлов, просто поменяй имена здесь.
    val rounds = remember {
        listOf(
            GuessSoundRound(
                soundResName = "guess_sound_car",
                speakerImageName = "guess_sound_speaker",
                correctOptionId = 1,
                options = listOf(
                    GuessSoundOption(1, "guess_sound_car"),
                    GuessSoundOption(2, "guess_sound_alarm"),
                    GuessSoundOption(3, "guess_sound_elephant")
                )
            ),
            GuessSoundRound(
                soundResName = "guess_sound_elephant",
                speakerImageName = "guess_sound_speaker",
                correctOptionId = 3,
                options = listOf(
                    GuessSoundOption(1, "guess_sound_car"),
                    GuessSoundOption(2, "guess_sound_alarm"),
                    GuessSoundOption(3, "guess_sound_elephant")
                )
            )
        )
    }

    var roundIndex by remember { mutableStateOf(0) }
    var starsFilled by remember { mutableStateOf(0) }
    var disabledOptionIds by remember { mutableStateOf(setOf<Int>()) }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var locked by remember { mutableStateOf(false) }
    var showRoundSuccess by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val currentRound = rounds[roundIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1CC93))
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
                RoundControlButton(
                    onClick = { isPaused = true },
                    bg = Color(0xFFFA8A56)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "Чей это звук?",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFA8A56)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(2) { idx ->
                        Image(
                            painter = painterResource(
                                id = if (idx < starsFilled) R.drawable.game_star_filled else R.drawable.game_star_outline
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Image(
                    painter = painterResource(id = resolveDrawableResId(context, currentRound.speakerImageName) ?: R.drawable.game_card_guess_sound),
                    contentDescription = "Колонка",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(enabled = !isPaused && !showSuccess && !showRoundSuccess && !locked) {
                            if (!isMuted) {
                                val soundResId = resolveRawResId(context, currentRound.soundResName)
                                    ?: R.raw.game_music
                                scope.launch { playSoundOnce(context, soundResId) }
                            }
                        },
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "Нажми на меня!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFF46D40),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentRound.options.forEach { option ->
                    val disabled = option.id in disabledOptionIds
                    val imageId = resolveDrawableResId(context, option.imageName) ?: fallbackOptionImage(option.id)
                    Image(
                        painter = painterResource(id = imageId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(170.dp)
                            .alpha(if (disabled) 0.55f else 1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = !locked && !disabled && !isPaused && !showSuccess && !showRoundSuccess) {
                                if (option.id == currentRound.correctOptionId) {
                                    locked = true
                                    starsFilled = (starsFilled + 1).coerceAtMost(2)
                                    scope.launch {
                                        delay(350)
                                        if (roundIndex == rounds.lastIndex) {
                                            showSuccess = true
                                        } else {
                                            showRoundSuccess = true
                                        }
                                    }
                                } else {
                                    disabledOptionIds = disabledOptionIds + option.id
                                }
                            },
                        contentScale = ContentScale.Fit,
                        colorFilter = if (disabled) {
                            ColorFilter.colorMatrix(
                                ColorMatrix().apply { setToSaturation(0f) }
                            )
                        } else {
                            null
                        }
                    )
                }
            }
        }

        if (showRoundSuccess) {
            GameBalloonsSuccessOverlay(
                title = "Молодец!",
                onDismiss = {
                    showRoundSuccess = false
                    roundIndex += 1
                    disabledOptionIds = emptySet()
                    locked = false
                }
            )
        }

        if (showSuccess) {
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
                    RoundControlButton(onClick = {
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
                    RoundControlButton(size = 74.dp, onClick = { isPaused = false }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    RoundControlButton(onClick = { isMuted = !isMuted }) {
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
private fun RoundControlButton(
    size: androidx.compose.ui.unit.Dp = 66.dp,
    bg: Color = Color(0xFFFA8A56),
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private fun resolveDrawableResId(context: android.content.Context, name: String): Int? {
    val id = context.resources.getIdentifier(name, "drawable", context.packageName)
    return id.takeIf { it != 0 }
}

private fun resolveRawResId(context: android.content.Context, name: String): Int? {
    val id = context.resources.getIdentifier(name, "raw", context.packageName)
    return id.takeIf { it != 0 }
}

private fun fallbackOptionImage(id: Int): Int = when (id) {
    1 -> R.drawable.routine_wake
    2 -> R.drawable.routine_brush
    else -> R.drawable.routine_breakfast
}

private suspend fun playSoundOnce(context: android.content.Context, rawResId: Int) {
    suspendCoroutine { cont ->
        runCatching {
            val player = MediaPlayer.create(context, rawResId)
            if (player == null) {
                cont.resume(Unit)
                return@suspendCoroutine
            }
            player.setOnCompletionListener {
                runCatching { it.release() }
                cont.resume(Unit)
            }
            player.setOnErrorListener { mp, _, _ ->
                runCatching { mp.release() }
                cont.resume(Unit)
                true
            }
            player.start()
        }.onFailure {
            cont.resume(Unit)
        }
    }
}

