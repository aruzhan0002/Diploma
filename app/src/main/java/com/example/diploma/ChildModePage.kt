package kz.aruzhan.care_steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.Canvas

@Composable
fun ChildModePage(navController: NavController) {
    val context = LocalContext.current
    var stats by remember { mutableStateOf(GameStatsRepository.todayStats(context)) }

    LaunchedEffect(Unit) {
        stats = GameStatsRepository.todayStats(context)
    }

    Scaffold(
        bottomBar = {
            BottomBar(navController, selectedIndex = 2)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Верхняя панель — заголовок и иконка настроек
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Детский режим",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Здесь вы можете перейти в детский режим чтобы воспользоваться большим функционалом нашего приложения",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2F6FED)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clickable {
                        navController.navigate("ChildGamesPage")
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "▶ Начать игру",
                        fontSize = 28.sp / 2,
                        fontWeight = FontWeight.SemiBold
                        ,
                        color = Color.White
                    )
                }
            }

            Text(
                text = "Последняя активность",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TodayActivityCard(
                    playedCount = stats.playedCount,
                    wonCount = stats.wonCount,
                    modifier = Modifier
                        .width(169.dp)
                        .height(200.dp)
                )

                Column(
                    modifier = Modifier.width(168.74.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SmallActivityCard(
                        title = "Время",
                        value = "${stats.totalMinutes} мин",
                        modifier = Modifier.height(97.75.dp)
                    )
                    SmallActivityCard(
                        title = "Последняя игра",
                        value = stats.lastGameTitle,
                        modifier = Modifier.height(97.75.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayActivityCard(
    playedCount: Int,
    wonCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4F6FA)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Сегодня выполнено",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = "$playedCount игр",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Побед: $wonCount",
                    fontSize = 12.sp,
                    color = Color(0xFF5A6475)
                )
            }

            Spacer(modifier = Modifier.padding(15.dp))

            // Круговой индикатор как в дизайне: 55×55, толщина 7dp, снизу карточки
            Canvas(modifier = Modifier.size(56.dp)) {
                val strokeWidth = 7.dp.toPx()
                val side = size.minDimension - strokeWidth
                val insetX = (size.width - side) / 2f
                val insetY = (size.height - side) / 2f
                val arcSize = Size(side, side)
                val topLeftSquare = Offset(insetX + strokeWidth / 2f, insetY + strokeWidth / 2f)

                // Серое кольцо
                drawArc(
                    color = Color(0xFFE3E7F0),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeftSquare,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                val progressSweep = if (playedCount <= 0) 0f else (360f * wonCount.toFloat() / playedCount.toFloat())
                // Синяя дуга прогресса побед
                drawArc(
                    color = Color(0xFF006FFD),
                    startAngle = -90f,
                    sweepAngle = progressSweep,
                    useCenter = false,
                    topLeft = topLeftSquare,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun SmallActivityCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4F6FA)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChildModePagePreview() {
    ChildModePage(navController = rememberNavController())
}

