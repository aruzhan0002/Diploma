package com.example.diploma

import LoginScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
@Composable
fun SpecialistStartPage(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Верхний голубой блок
        Image(
            painter = painterResource(id = R.drawable.img4), // замените на свой ресурс
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(538.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Текст
        Text(
            text = "Настроим ваш рабочий\nкабинет",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .width(327.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Пару шагов — и вы сможете принимать запросы и работать с семьями.",
            style = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFF8D8D8D)
            ),
            modifier = Modifier
                .width(327.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Кнопка Далее
        Button(
            onClick = {
                navController.navigate("SAccountCreateFirstPage")
            },
            modifier = Modifier
                .width(327.dp)
                .height(45.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006FFD))
        ) {
            Text("Далее", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSpecialistStartPage() {
    SpecialistStartPage(navController = rememberNavController())
}


