@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package kz.aruzhan.care_steps

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kz.aruzhan.care_steps.data.remote.ApiClient
import org.json.JSONArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SpecialistCoursesPage(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(route) {
        if (route != "SpecialistCoursesPage") return@LaunchedEffect
        loading = true
        loadError = null
        val result = withContext(Dispatchers.IO) {
            runCatching {
                ApiClient.withNetworkRetry { ApiClient.api.getSpecialistCourses() }.map { it.toUiCourse() }
            }.recoverCatching {
                // Fallback: если backend вернул смешанные типы в одном из полей.
                val raw = ApiClient.withNetworkRetry { ApiClient.api.getSpecialistCoursesRaw() }.string()
                parseCoursesFromRawJson(raw)
            }
        }
        loading = false
        result.onSuccess { list ->
            courses = list
        }.onFailure { e ->
            loadError = e.message ?: "Не удалось загрузить курсы"
        }
    }

    Scaffold(
        bottomBar = {
            SpecialistBottomBar(navController, selectedIndex = 1)
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Мои курсы",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                Color(0xFF006FFD),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { navController.navigate("CreateCoursePage") }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "+ добавить курс",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF006FFD))
                    }
                }
            }

            if (loadError != null && !loading) {
                item {
                    Text(
                        text = loadError!!,
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            if (!loading && loadError == null && courses.isEmpty()) {
                item {
                    Text(
                        text = "Пока нет курсов. Нажмите «+ добавить курс».",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }

            items(
                items = courses,
                key = { it.id }
            ) { course ->
                CourseCard1(
                    course = course,
                    onDelete = {
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                runCatching {
                                    ApiClient.withNetworkRetry { ApiClient.api.deleteCourse(course.id) }
                                }
                            }
                            result.onSuccess { response ->
                                if (response.isSuccessful) {
                                    courses = courses.filter { it.id != course.id }
                                    Toast.makeText(ctx, "Курс удалён", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(ctx, "Не удалось удалить курс", Toast.LENGTH_SHORT).show()
                                }
                            }.onFailure { e ->
                                Toast.makeText(ctx, e.message ?: "Ошибка удаления курса", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    navController.navigate("CourseDetailsPage/${course.id}?mode=specialist")
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

private fun parseCoursesFromRawJson(raw: String): List<Course> {
    val arr = JSONArray(raw)
    return List(arr.length()) { idx -> arr.optJSONObject(idx) }
        .mapNotNull { obj ->
            if (obj == null) return@mapNotNull null
            val id = obj.optInt("id", -1)
            if (id <= 0) return@mapNotNull null
            val title = obj.optString("title").ifBlank { "Курс #$id" }
            val description = obj.optString("description").ifBlank { "Описание пока недоступно" }
            val priceRaw = obj.opt("price")?.toString().orEmpty().ifBlank { "0" }
            val priceDouble = priceRaw.replace(",", ".").toDoubleOrNull()
            val priceText = priceDouble?.let { "%.2f ₸".format(it) } ?: "$priceRaw ₸"
            val preview = obj.optString("preview_image").ifBlank { null }?.let { resolveCourseImageUrl(it) }

            val tags = mutableListOf<String>()
            val tagsAny = obj.opt("tags")
            when (tagsAny) {
                is JSONArray -> {
                    repeat(tagsAny.length()) { i ->
                        tagsAny.optString(i).takeIf { it.isNotBlank() }?.let { tags += it }
                    }
                }
                is String -> if (tagsAny.isNotBlank()) tags += tagsAny
            }
            if (tags.isEmpty()) {
                obj.optString("category").takeIf { it.isNotBlank() }?.let { tags += it }
            }

            Course(
                id = id,
                title = title,
                description = description,
                price = priceText,
                tagLabels = tags.map {
                    when (it.lowercase()) {
                        "to_parents" -> "РОДИТЕЛЯМ"
                        "for_children" -> "ДЕТЯМ"
                        "self_regulation" -> "САМОРЕГУЛЯЦИЯ"
                        "logical_thinking" -> "ЛОГИКА"
                        "learning_through_play" -> "ИГРА"
                        "intensive_course" -> "ИНТЕНСИВ"
                        "autism" -> "АУТИЗМ"
                        "speech_therapy" -> "ЛОГОПЕДИЯ"
                        "adhd" -> "СДВГ"
                        "sensory_processing" -> "СЕНСОРИКА"
                        else -> it.uppercase()
                    }
                },
                rating = "—",
                previewImageUrl = preview
            )
        }
}

@Composable
fun CourseCard1(
    course: Course,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(343.dp)
            .height(170.dp)
            .background(
                Color(0xFFF4F6FA),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onClick() }
    ) {
        val ctx = LocalContext.current
        Box(
            modifier = Modifier
                .width(115.dp)
                .fillMaxHeight()
                .background(
                    Color(0xFFDCE6F5),
                    shape = RoundedCornerShape(
                        topStart = 22.dp,
                        bottomStart = 22.dp
                    )
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 22.dp,
                        bottomStart = 22.dp
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val url = course.previewImageUrl
            if (!url.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.icon6),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFFB0BDD4)
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    course.tagLabels.forEach { label ->
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFF2F6FED),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                Text(
                    text = course.rating,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = course.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Text(
                text = course.description,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 2
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CourseActionButton(
                        iconRes = R.drawable.icon_delete_btn,
                        bgColor = Color(0xFFFFE0E0),
                        onClick = onDelete
                    )
                }
                Text(
                    text = course.price,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun CourseActionButton(
    iconRes: Int,
    bgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(bgColor, shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSpecialistCoursesPage() {
    SpecialistCoursesPage(navController = rememberNavController())
}
