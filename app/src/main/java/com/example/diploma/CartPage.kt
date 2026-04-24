package com.example.diploma

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.diploma.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CartPage(navController: NavController) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val cartItems = CartRepository.items
    val total = CartRepository.total()
    var previewById by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var paying by remember { mutableStateOf(false) }
    var payError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(cartItems.size) {
        val result = withContext(Dispatchers.IO) {
            runCatching { ApiClient.withNetworkRetry { ApiClient.api.getCoursePreviews() } }
        }
        val mapped = result.getOrNull().orEmpty()
            .mapNotNull { item ->
                val url = resolveCourseImageUrl(item.preview_image) ?: return@mapNotNull null
                item.id to url
            }
            .toMap()
        previewById = mapped
    }

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Всего", fontSize = 16.sp, color = Color.Gray)
                    Text(
                        "${formatPrice(total)} ₸",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                payError?.let { err ->
                    Text(
                        text = err,
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = {
                        if (cartItems.isEmpty() || paying) return@Button
                        payError = null
                        scope.launch {
                            paying = true
                            val snapshot = cartItems.toList()
                            val failures = mutableListOf<String>()
                            for (item in snapshot) {
                                val ok = withContext(Dispatchers.IO) {
                                    runCatching {
                                        ApiClient.withNetworkRetry {
                                            ApiClient.api.purchaseCourse(item.id)
                                        }
                                    }
                                }
                                ok.onSuccess {
                                    PurchaseAnalyticsRepository.recordPurchase(item.id, item.price)
                                    CartRepository.remove(item.id)
                                }.onFailure { e ->
                                    failures.add("${item.title}: ${e.message ?: "ошибка"}")
                                }
                            }
                            paying = false
                            if (failures.isNotEmpty()) {
                                payError = failures.joinToString("\n")
                            }
                        }
                    },
                    enabled = !paying && cartItems.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                ) {
                    Text(
                        text = if (paying) "Оплата…" else "Оплатить",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "‹",
                    fontSize = 28.sp,
                    color = Color(0xFF006FFD),
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Корзина",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(24.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (cartItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Корзина пуста",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                itemsIndexed(
                    items = cartItems.toList(),
                    key = { _, item -> item.id }
                ) { _, item ->
                    Column {
                        CartItemCard(
                            item = item,
                            previewUrl = previewById[item.id],
                            ctx = ctx,
                            onOpen = { navController.navigate("CourseDetailsPage/${item.id}") }
                        ) {
                            CartRepository.remove(item.id)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .height(1.dp)
                                .background(Color(0xFFE7E9EF))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    previewUrl: String?,
    ctx: android.content.Context,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onOpen() },
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Color(0xFFDCE6F5),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!previewUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(previewUrl)
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
                    modifier = Modifier.size(28.dp),
                    tint = Color(0xFFB0BDD4)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 0.dp, top = 2.dp, bottom = 0.dp)
        ) {
            Text(
                text = item.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(item.author, fontSize = 12.sp, color = Color(0xFF8A8F9C), maxLines = 1)
            Spacer(modifier = Modifier.height(6.dp))

            Image(
                painter = painterResource(R.drawable.icon_trash_red),
                contentDescription = "Удалить",
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onRemove() }
            )
        }
        Text(
            text = "${formatPrice(item.price)} ₸",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier
                .padding(top = 28.dp)
        )
    }
}

private fun formatPrice(price: Double): String {
    return if (price == price.toLong().toDouble()) {
        "%,.0f".format(price).replace(',', '.')
    } else {
        "%,.2f".format(price).replace(',', '.')
    }
}
