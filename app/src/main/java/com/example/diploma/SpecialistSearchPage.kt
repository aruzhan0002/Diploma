package com.example.diploma

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SpecialistSearchPage(navController: NavController) {
    var query by remember { mutableStateOf("") }
    val recentSearches = remember {
        mutableStateListOf("Карим Мардан", "Психолог")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.icon_search),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                singleLine = true,
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text("Поиск специалистов...", color = Color.Gray, fontSize = 16.sp)
                    }
                    inner()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "RECENT SEARCHES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        recentSearches.toList().forEach { search ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        query = search
                        navController.navigate("SpecialistSearchResultsPage/$search")
                    }
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(search, fontSize = 16.sp)
                Image(
                    painter = painterResource(R.drawable.icon_close_circle),
                    contentDescription = "Удалить",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { recentSearches.remove(search) }
                )
            }
            Divider(color = Color(0xFFF0F0F0))
        }
    }
}
