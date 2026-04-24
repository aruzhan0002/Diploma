package com.example.diploma

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.diploma.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun SpecialistDashboardPage(navController: NavController) {
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var greetLine by remember { mutableStateOf("С возвращением!") }
    var courseCount by remember { mutableStateOf(0) }
    var purchaseCount by remember { mutableStateOf(0) }
    var totalProfit by remember { mutableStateOf(0.0) }
    var avgRatingLabel by remember { mutableStateOf("—") }

    LaunchedEffect(Unit) {
        loading = true
        loadError = null
        val (courses, coursesErr, settings) = withContext(Dispatchers.IO) {
            val cResult = runCatching { ApiClient.withNetworkRetry { ApiClient.api.getSpecialistCourses() } }
            val sResult = runCatching { ApiClient.withNetworkRetry { ApiClient.api.getSpecialistSettings() } }
            Triple(
                cResult.getOrNull().orEmpty(),
                cResult.exceptionOrNull(),
                sResult.getOrNull()
            )
        }
        val ids = courses.map { it.id }.toSet()
        val (purchases, profit) = PurchaseAnalyticsRepository.statsForCourses(ids)
        val ratings = courses.mapNotNull { it.average_rating }
        val avg = ratings.takeIf { it.isNotEmpty() }?.average()

        val full = settings?.full_name.orEmpty().trim()
        val firstName = full.split("\\s+".toRegex()).firstOrNull()?.takeIf { it.isNotBlank() }
        greetLine = if (firstName != null) "С возвращением, $firstName" else "С возвращением!"

        courseCount = courses.size
        purchaseCount = purchases
        totalProfit = profit
        avgRatingLabel = avg?.let { String.format(Locale.US, "%.1f", it) } ?: "—"
        if (coursesErr != null) {
            loadError = coursesErr.message?.takeIf { it.isNotBlank() } ?: "Не удалось загрузить курсы"
        }
        loading = false
    }

    Scaffold(
        bottomBar = {
            SpecialistBottomBar(navController = navController, selectedIndex = 0)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = greetLine,
                    fontSize = 30.sp / 1.5f,
                    fontWeight = FontWeight.SemiBold
                )
                loadError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = err, fontSize = 13.sp, color = Color(0xFFD32F2F))
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        "Всего курсов",
                        courseCount.toString(),
                        R.drawable.icon_stat_folder,
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "Всего покупок",
                        purchaseCount.toString(),
                        R.drawable.icon_stat_cart,
                        Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        "Общая прибыль",
                        formatDashboardAmount(totalProfit),
                        R.drawable.icon_stat_chart,
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "Средний рейтинг",
                        avgRatingLabel,
                        R.drawable.icon_stat_star,
                        Modifier.weight(1f)
                    )
                }
            }
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator(color = Color(0xFF006FFD))
                }
            }
        }
    }
}

private fun formatDashboardAmount(value: Double): String {
    val n = kotlin.math.abs(value).toLong()
    val s = n.toString()
    return s.reversed().chunked(3).joinToString(" ").reversed()
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.Unspecified
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .background(Color(0xFFF3F5F9), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, fontSize = 12.sp, color = Color(0xFF666666))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp / 1.5f, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
        }
        Box(
            modifier = Modifier
                .size(38.dp)
                .align(Alignment.BottomEnd)
                .background(Color(0xFFE8EBF0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                tint = iconTint
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSpecialistDashboardPage() {
    SpecialistDashboardPage(navController = rememberNavController())
}
