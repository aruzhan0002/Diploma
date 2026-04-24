package com.example.diploma

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.diploma.data.remote.ApiClient
import com.example.diploma.data.remote.SpecialistCardResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SpecialistSearchResultsPage(navController: NavController, query: String) {
    val trimmedQuery = query.trim()

    var specialists by remember { mutableStateOf<List<SpecialistCardResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(trimmedQuery) {
        loading = true
        errorMessage = null
        val result = withContext(Dispatchers.IO) {
            runCatching {
                // Подстрока ищется и по ФИО (`q`), и по специализации (`specialization_search`)
                // одновременно — так пользователь находит специалиста, не думая о категории.
                ApiClient.loadAllSpecialistCards(
                    q = trimmedQuery.takeIf { it.isNotEmpty() },
                    specializationSearch = trimmedQuery.takeIf { it.isNotEmpty() }
                )
            }
        }
        loading = false
        result.onSuccess { specialists = it }
            .onFailure { e -> errorMessage = e.message ?: "Не удалось загрузить специалистов" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .clickable { navController.popBackStack() }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.icon_search),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(query, fontSize = 16.sp, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_sort),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text("Сортирование", fontSize = 13.sp)
                Text("⌄", fontSize = 14.sp, color = Color.Gray)
            }

            Row(
                modifier = Modifier
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_filter),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text("Фильтр", fontSize = 13.sp)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFF006FFD), RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("2", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            loading && specialists.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color(0xFF006FFD),
                        strokeWidth = 3.dp
                    )
                }
            }
            errorMessage != null && specialists.isEmpty() -> {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFD32F2F),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            specialists.isEmpty() -> {
                Text(
                    text = "Ничего не найдено",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(specialists, key = { it.id }) { spec ->
                        SpecialistResultCard(navController, spec)
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SpecialistResultCard(
    navController: NavController,
    spec: SpecialistCardResponse
) {
    val ctx = LocalContext.current
    val avatarUrl = spec.avatarUrlResolved()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(ctx)
                                .data(avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.icon_user),
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        spec.full_name.orEmpty().ifBlank { "Специалист" },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    Text(
                        spec.specialization.orEmpty().ifBlank { "—" },
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                Box(
                    modifier = Modifier
                        .border(1.dp, Color(0xFF006FFD), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { navController.navigate("SpecialistProfilePage/${spec.id}") }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Подробнее", color = Color(0xFF006FFD), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SpecSearchBadge(spec.ratingWithReviews())
                SpecSearchBadge(spec.experienceBadgeText())
                SpecSearchBadge(spec.specialistPriceFromDisplay())
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                (spec.short_description ?: "").ifBlank { " " },
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 3,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SpecSearchBadge(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFE7F0FF), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 10.sp, color = Color(0xFF006FFD), fontWeight = FontWeight.Medium)
    }
}
