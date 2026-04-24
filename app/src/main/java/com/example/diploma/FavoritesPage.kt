package com.example.diploma

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun FavoritesPage(navController: NavController) {
    FavoritesRepository.items
    val items = FavoritesRepository.items.toList()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Избранные", fontSize = 22.sp / 1.2f, fontWeight = FontWeight.SemiBold)
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(34.dp)
            ) {
                Text(
                    "‹",
                    fontSize = 32.sp,
                    color = Color(0xFF2F6FED)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Пока нет избранных курсов", color = Color(0xFF8A8A8A))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items, key = { it.id }) { item ->
                    FavoriteCourseRow(
                        item = item,
                        onOpen = { navController.navigate("CourseDetailsPage/${item.id}") },
                        onRemove = { }
                    )
                    Divider(color = Color(0xFFEDEDED))
                }
            }
        }
    }
}

@Composable
private fun FavoriteCourseRow(
    item: FavoriteItem,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    val ctx = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(102.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFE4ECF9)),
            contentAlignment = Alignment.Center
        ) {
            if (!item.previewImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(item.previewImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.icon6),
                    contentDescription = null,
                    tint = Color(0xFFAFC3E9),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                    Text(item.author, fontSize = 12.sp, color = Color(0xFF7A7A7A), maxLines = 1)
                }
                Icon(
                    painter = painterResource(R.drawable.icon_heart),
                    contentDescription = "В избранном",
                    tint = Color(0xFF2F6FED),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format(java.util.Locale.US, "%.3f ₸", item.price),
                fontSize = 16.sp / 1.2f,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

