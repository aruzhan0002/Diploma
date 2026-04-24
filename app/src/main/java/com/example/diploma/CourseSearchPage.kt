package kz.aruzhan.care_steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun CourseSearchPage(navController: NavController, filters: CourseFilters) {
    var query by remember { mutableStateOf("") }
    val recentSearches = remember {
        mutableStateListOf("Моторика", "Аутизм", "Для родителей")
    }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.icon_search),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Поиск курсов...", color = Color.Gray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        if (query.isNotBlank()) {
                            if (query !in recentSearches) recentSearches.add(0, query)
                            navController.navigate("SearchResultsPage/$query")
                        }
                    },
                    onDone = {
                        focusManager.clearFocus()
                        if (query.isNotBlank()) {
                            if (query !in recentSearches) recentSearches.add(0, query)
                            navController.navigate("SearchResultsPage/$query")
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { navController.navigate("CourseFilterPage") },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_filter),
                    contentDescription = "Фильтр",
                    modifier = Modifier.size(22.dp)
                )
                if (filters.activeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .background(Color(0xFF006FFD), RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${filters.activeCount}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
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
                        navController.navigate("SearchResultsPage/$search")
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
