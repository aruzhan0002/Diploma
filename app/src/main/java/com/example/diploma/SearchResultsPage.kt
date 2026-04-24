package com.example.diploma

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SearchResultsPage(navController: NavController, query: String, filters: CourseFilters) {
    val searchQuery = if (query == "all") "" else query
    var results by remember { mutableStateOf<List<SearchableCourse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(
        searchQuery,
        filters.selectedRatings.toList(),
        filters.priceMin, filters.priceMax,
        filters.selectedLevels.toList()
    ) {
        isLoading = true
        results = withContext(Dispatchers.IO) {
            CourseSearchRepository.searchFromApi(searchQuery, filters)
        }
        isLoading = false
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
            Text(
                if (searchQuery.isBlank()) "Результаты фильтра" else searchQuery,
                fontSize = 16.sp,
                color = if (searchQuery.isBlank()) Color.Gray else Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF006FFD))
            }
        } else if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Нет таких курсов",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(results, key = { it.id }) { course ->
                    Box(
                        modifier = Modifier.clickable {
                            navController.navigate("CourseDetailsPage/${course.id}")
                        }
                    ) {
                        SearchCourseCard(course)
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun SearchCourseCard(course: SearchableCourse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(Color(0xFFF4F6FA), RoundedCornerShape(18.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .background(
                    Color(0xFFDCE6F5),
                    RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.icon6),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color(0xFFB0BDD4)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
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
                    Text(
                        course.audienceLabel,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    "★${course.rating}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF006FFD)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(course.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(course.author, fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "${formatSearchPrice(course.price)} ₸",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

private fun formatSearchPrice(price: Double): String {
    return if (price == price.toLong().toDouble()) {
        "%,.2f".format(price).replace(',', '.')
    } else {
        "%,.2f".format(price).replace(',', '.')
    }
}
