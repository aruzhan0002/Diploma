package com.example.diploma

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun ModuleOneGamesPage(navController: NavController) {

    Scaffold { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF7ED6F9),
                            Color(0xFFE5F7FF)
                        )
                    )
                )
        ) {

             Image(
                 painter = painterResource(id = R.drawable.bg_module1_cloud),
                 contentDescription = null,
                 modifier = Modifier.fillMaxSize(),
                 contentScale = ContentScale.Crop
             )

            // Верхний логотип и солнышко
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 24.dp, end = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.caresteps_logo),
                    contentDescription = null,
                    // логотип 2x от макета: 116x29 -> 232x58
                    modifier = Modifier
                        .width(116.dp)
                        .height(29.dp),
                    contentScale = ContentScale.Fit
                )

                Image(
                    painter = painterResource(id = R.drawable.sun_happy),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Две карточки игр в центре
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 64.dp)
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                GameCategoryCard(
                    borderColor = Color(0xFFFF7A45),
                    title = "Ежедневные дела",
                    imagePainter = painterResource(id = R.drawable.daily_routine_image),
                    onClick = {
                        navController.navigate("DailyRoutineGamePage")
                    }
                )

                GameCategoryCard(
                    borderColor = Color(0xFF52C41A),
                    title = "Мои эмоции",
                    imagePainter = painterResource(id = R.drawable.my_emotions_image),
                    onClick = {
                        navController.navigate("EmotionGamePage")
                    }
                )
            }
        }
    }
}

@Composable
private fun GameCategoryCard(
    borderColor: Color,
    title: String,
    imagePainter: androidx.compose.ui.graphics.painter.Painter,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .width(260.dp)
            .height(320.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White, RoundedCornerShape(28.dp))
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(borderColor, RoundedCornerShape(24.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = imagePainter,
                            contentDescription = null,
                            modifier = Modifier.size(180.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2933)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 820, heightDp = 380)
@Composable
private fun ModuleOneGamesPagePreview() {
    ModuleOneGamesPage(navController = rememberNavController())
}

