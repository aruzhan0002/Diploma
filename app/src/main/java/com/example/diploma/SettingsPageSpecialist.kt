package com.example.diploma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.diploma.data.remote.ApiClient
import com.example.diploma.data.remote.TokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SettingsPageSpecialist(navController: NavController) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("Специалист") }
    var handle by remember { mutableStateOf("@specialist") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var avatarVersion by remember { mutableStateOf(0L) }
    val backStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(backStackEntry?.destination?.route) {
        val specialist = withContext(Dispatchers.IO) {
            runCatching { ApiClient.withNetworkRetry { ApiClient.api.getSpecialistSettings() } }.getOrNull()
        }
        if (specialist != null) {
            val name = specialist.full_name.ifBlank { "Специалист" }
            fullName = name
            handle = "@${specialist.email.substringBefore("@").ifBlank { "specialist" }}"
            avatarUrl = resolveCourseImageUrl(specialist.avatar)
            avatarVersion = System.currentTimeMillis()
        }
    }

    Scaffold(
        bottomBar = {
            SpecialistBottomBar(
                navController = navController,
                selectedIndex = 2
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 45.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Настройки",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        val avatarWithVersion = appendAvatarVersionForSettings(avatarUrl, avatarVersion)
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarWithVersion)
                                // Пустой ключ для memory/disk cache, чтобы при смене версии
                                // Coil не подсовывал закешированный результат.
                                .memoryCacheKey(avatarWithVersion)
                                .diskCacheKey(avatarWithVersion)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            onError = { state ->
                                android.util.Log.w(
                                    "SettingsPageSpecialist",
                                    "Avatar load failed for $avatarWithVersion: ${state.result.throwable}"
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = fullName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = handle,
                    fontSize = 12.sp,
                    color = Color(0xFF8D8D8D)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsRow("Профиль") {
                navController.navigate("SpecialistProfileSettingsPage")
            }
            Divider(color = Color(0xFFEDEDED))

            SettingsRow(
                title = "Выйти",
                textColor = Color(0xFFFF3B30)
            ) {
                showLogoutDialog = true
            }
            Divider(color = Color(0xFFEDEDED))
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onCancel = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                TokenStorage.clearTokens(navController.context)
                navController.navigate("startScreen") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}

@Composable
private fun LogoutConfirmDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable { onCancel() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .clickable(enabled = false) { },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Выйти", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Вы уверены что хотите выйти? Вам\nнужно будет войти повторно",
                fontSize = 13.sp,
                color = Color(0xFF8A8A8A)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF2F6FED), RoundedCornerShape(12.dp))
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Отмена", color = Color(0xFF2F6FED), fontWeight = FontWeight.SemiBold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF3B30))
                        .clickable { onConfirm() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Выйти", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Text("›", fontSize = 22.sp, color = Color(0xFFB0B0B0))
    }
}

@Composable
fun SpecialistBottomBar(
    navController: NavController,
    selectedIndex: Int
) {

    val items = listOf(
        Triple(R.drawable.ic_spec_dashboard, "Дашборд", "SpecialistDashboardPage"),
        Triple(R.drawable.ic_spec_courses, "Курсы", "SpecialistCoursesPage"),
        Triple(R.drawable.ic_spec_profile_alt, "Профиль", "SettingsPageSpecialist")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {

        items.forEachIndexed { index, item ->

            val color = if (index == selectedIndex) Color(0xFF006FFD) else Color(0xFFBDBDBD)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    navController.navigate(item.third) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = item.first),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(color)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsPageSpecialist() {
    SettingsPageSpecialist(navController = rememberNavController())
}

private fun appendAvatarVersionForSettings(url: String?, version: Long): String? {
    if (url.isNullOrBlank()) return null
    val separator = if (url.contains("?")) "&" else "?"
    return "$url${separator}v=$version"
}