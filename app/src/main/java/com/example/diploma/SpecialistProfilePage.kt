package kz.aruzhan.care_steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kz.aruzhan.care_steps.data.remote.ApiClient
import kz.aruzhan.care_steps.data.remote.CourseCardResponse
import kz.aruzhan.care_steps.data.remote.SpecialistDetailResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialistProfilePage(navController: NavController, specialistId: Int = 1) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var showContactSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var specialist by remember { mutableStateOf<SpecialistDetailResponse?>(null) }
    var specialistCourses by remember { mutableStateOf<List<CourseCardResponse>>(emptyList()) }
    var coursesLoading by remember { mutableStateOf(false) }

    LaunchedEffect(specialistId) {
        val loaded = withContext(Dispatchers.IO) {
            loadSpecialistDetailWithFallback(specialistId)
        }
        if (loaded != null) {
            specialist = loaded
        }
        coursesLoading = true
        specialistCourses = withContext(Dispatchers.IO) {
            runCatching {
                ApiClient.withNetworkRetry { ApiClient.api.getSpecialistPublicCourses(specialistId) }
            }.recoverCatching { e ->
                android.util.Log.w("SpecialistProfilePage", "typed courses failed, raw fallback: $e")
                val raw = ApiClient.withNetworkRetry {
                    ApiClient.api.getSpecialistPublicCoursesRaw(specialistId).string()
                }
                parseCourseCardListFromRawJson(raw)
            }.getOrNull().orEmpty()
        }
        coursesLoading = false
    }

    val avatarUrl = specialist?.avatarUrlResolved()
    val fullName = specialist?.full_name?.ifBlank { null } ?: "Специалист"
    val subtitle = specialist?.subtitle()?.ifBlank { null } ?: "Специалист"
    val ratingText = specialist?.ratingText() ?: "★0.0"

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF006FFD))
                    .clickable { showContactSheet = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Записаться на консультацию",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "✕",
                        fontSize = 22.sp,
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                    Image(
                        painter = painterResource(R.drawable.icon_heart),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(ctx).data(avatarUrl).crossfade(true).build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.icon_user),
                                contentDescription = null,
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(subtitle, fontSize = 13.sp, color = Color.Gray)
                    }

                    Text(ratingText, color = Color(0xFF006FFD), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                ProfileTabSwitcher(selectedTab) { selectedTab = it }

                Spacer(modifier = Modifier.height(16.dp))
            }

            when (selectedTab) {
                0 -> item { AboutSpecialistContent(specialist) }
                1 -> item {
                    SpecialistCoursesContent(
                        navController = navController,
                        courses = specialistCourses,
                        loading = coursesLoading
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    if (showContactSheet) {
        ModalBottomSheet(
            onDismissRequest = { showContactSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                ContactOption(
                    iconRes = R.drawable.ic_phone_circle,
                    label = "Телефон",
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showContactSheet = false
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                ContactOption(
                    iconRes = R.drawable.ic_whatsapp_circle,
                    label = "WhatsApp",
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showContactSheet = false
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                ContactOption(
                    iconRes = R.drawable.ic_telegram_circle,
                    label = "Telegram",
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showContactSheet = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ContactOption(iconRes: Int, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(44.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileTabSwitcher(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        val tabs = listOf("О специалисте", "Курсы")
        tabs.forEachIndexed { index, title ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(index) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = if (index == selectedTab) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (index == selectedTab) Color.Black else Color.Gray,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(if (index == selectedTab) Color.Black else Color(0xFFE0E0E0))
                )
            }
        }
    }
}

@Composable
private fun AboutSpecialistContent(specialist: SpecialistDetailResponse?) {
    val description = specialist?.approach_description?.ifBlank { null }
        ?: "Информация о специалисте пока недоступна."
    val specializations = specialist?.specializationLabels().orEmpty()
    val developmentTypes = specialist?.developmentTypeLabels().orEmpty()
    val methods = specialist?.methodLabels().orEmpty()
    val languages = specialist?.languageLabels().orEmpty()
    val workFormat = specialist?.workFormatLabels().orEmpty()
    val price = specialist?.priceFromDisplay()
    val years = specialist?.years_experience?.let { el ->
        if (el.isJsonNull) null
        else when {
            el.isJsonPrimitive && el.asJsonPrimitive.isNumber -> el.asInt
            el.isJsonPrimitive && el.asJsonPrimitive.isString -> el.asString.toIntOrNull()
            else -> null
        }
    }
    val ageRange = specialist?.age_range?.ifBlank { null }
    val city = specialist?.city?.ifBlank { null }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        ProfileSection("О специалисте") {
            Text(
                description,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
        }

        ProfileSection("Специализация") {
            val items = specializations.ifEmpty {
                developmentTypes.ifEmpty { listOf("—") }
            }
            items.forEach { item ->
                Text("• $item", fontSize = 14.sp, color = Color.Gray)
            }
        }

        if (developmentTypes.isNotEmpty() && specializations.isNotEmpty()) {
            ProfileSection("Работает с") {
                developmentTypes.forEach { item ->
                    Text("• $item", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }

        if (methods.isNotEmpty()) {
            ProfileSection("Методики") {
                methods.forEach { item ->
                    Text("• $item", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }

        ProfileSection("Опыт") {
            val yearsText = years?.let {
                val w = when {
                    it % 10 == 1 && it % 100 != 11 -> "год"
                    it % 10 in 2..4 && it % 100 !in 12..14 -> "года"
                    else -> "лет"
                }
                "$it $w опыта"
            } ?: "—"
            Text(yearsText, fontSize = 14.sp, color = Color.Gray)
            if (!ageRange.isNullOrBlank()) {
                Text("Возраст детей: $ageRange", fontSize = 14.sp, color = Color.Gray)
            }
        }

        ProfileSection("Формат занятий") {
            val items = workFormat.ifEmpty { listOf("—") }
            items.forEach { f ->
                Text(f, fontSize = 14.sp, color = Color.Gray)
            }
        }

        if (languages.isNotEmpty()) {
            ProfileSection("Язык занятий") {
                Text(languages.joinToString(", "), fontSize = 14.sp, color = Color.Gray)
            }
        }

        if (!city.isNullOrBlank()) {
            ProfileSection("Город") {
                Text(city, fontSize = 14.sp, color = Color.Gray)
            }
        }

        ProfileSection("Цена") {
            Text("Стоимость: ${price ?: "—"}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Column { content() }
    Spacer(modifier = Modifier.height(12.dp))
    Divider(color = Color(0xFFF0F0F0))
}

@Composable
private fun SpecialistCoursesContent(
    navController: NavController,
    courses: List<CourseCardResponse>,
    loading: Boolean
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            loading -> Text("Загрузка курсов...", fontSize = 14.sp, color = Color.Gray)
            courses.isEmpty() -> Text("У этого специалиста пока нет курсов", fontSize = 14.sp, color = Color.Gray)
            else -> courses.forEach { course ->
                SpecProfileCourseCard(navController, course)
            }
        }
    }
}

@Composable
private fun SpecProfileCourseCard(navController: NavController, course: CourseCardResponse) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val imageUrl = course.previewImageUrlResolved()
    val tag = course.displayTagLabels(max = 1).firstOrNull() ?: "РОДИТЕЛЯМ"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color(0xFFF4F6FA), RoundedCornerShape(18.dp))
            .clickable { navController.navigate("CourseDetailsPage/${course.id}") },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(90.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
                .background(
                    Color(0xFFDCE6F5),
                    RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx).data(imageUrl).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.icon6),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color(0xFFB0BDD4)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF2F6FED), RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(tag, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                }
                Text(
                    course.ratingShortText(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF006FFD)
                )
            }

            Text(course.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2)
            Text(course.specialist_name, fontSize = 12.sp, color = Color.Gray, maxLines = 1)

            Text(
                course.formattedPriceTenge(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * Грузит детальную карточку специалиста с несколькими попытками:
 *  1) типизированный DTO через Gson;
 *  2) если Gson упал (type-mismatch / обрыв) — raw JSON и ручная десериализация.
 * Возвращает null, только если оба варианта провалились.
 */
private suspend fun loadSpecialistDetailWithFallback(
    specialistId: Int
): SpecialistDetailResponse? {
    val typed = runCatching {
        ApiClient.withNetworkRetry { ApiClient.api.getSpecialistCardById(specialistId) }
    }.onFailure {
        android.util.Log.w("SpecialistProfilePage", "typed detail failed: $it")
    }.getOrNull()
    if (typed != null) return typed

    val raw = runCatching {
        ApiClient.withNetworkRetry {
            ApiClient.api.getSpecialistCardByIdRaw(specialistId).string()
        }
    }.onFailure {
        android.util.Log.w("SpecialistProfilePage", "raw detail failed: $it")
    }.getOrNull()
    if (raw.isNullOrBlank()) return null

    android.util.Log.d("SpecialistProfilePage", "raw detail: $raw")
    return parseSpecialistDetailFromRawJson(raw)
}
