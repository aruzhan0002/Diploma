@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package kz.aruzhan.care_steps

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kz.aruzhan.care_steps.data.remote.ApiClient
import kz.aruzhan.care_steps.data.remote.CourseCardResponse
import kz.aruzhan.care_steps.data.remote.SpecialistCardResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SpecialistsPage(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    /** Увеличивается при нажатии на вкладку «Курсы» — баннер листается на 2-й слайд. */
    var coursesTabClickSignal by remember { mutableStateOf(0) }

    var publicCourses by remember { mutableStateOf<List<CourseCardResponse>>(emptyList()) }
    var coursesLoading by remember { mutableStateOf(true) }
    var coursesError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        coursesLoading = true
        coursesError = null
        val result = withContext(Dispatchers.IO) {
            runCatching { ApiClient.withNetworkRetry { ApiClient.api.getCourseCards() } }
        }
        coursesLoading = false
        result.onSuccess { publicCourses = it }
            .onFailure { e -> coursesError = e.message ?: "Не удалось загрузить курсы" }
    }

    var specialists by remember { mutableStateOf<List<SpecialistCardResponse>>(emptyList()) }
    var specialistsLoading by remember { mutableStateOf(false) }
    var specialistsError by remember { mutableStateOf<String?>(null) }
    var specialistFilterIndex by remember { mutableStateOf(0) }

    val specialistFilterLabels = remember {
        listOf("ВСЕ", "ПСИХОЛОГИ", "ЛОГОПЕДЫ", "НЕЙРОПСИХОЛОГИ", "ДЕФЕКТОЛОГИ", "АБА-ТЕРАПЕВТЫ")
    }
    val specialistFilterQueries = remember {
        listOf<String?>(null, "психолог", "логопед", "нейропсихолог", "дефектолог", "аба")
    }

    LaunchedEffect(selectedTab, specialistFilterIndex) {
        if (selectedTab != 1) return@LaunchedEffect
        specialistsLoading = true
        specialistsError = null
        val specQ = specialistFilterQueries.getOrElse(specialistFilterIndex) { null }
        val result = withContext(Dispatchers.IO) {
            runCatching {
                ApiClient.loadAllSpecialistCards(specializationSearch = specQ)
            }
        }
        specialistsLoading = false
        result.onSuccess { specialists = it }
            .onFailure { e ->
                specialistsError = e.message ?: "Не удалось загрузить специалистов"
            }
    }

    Scaffold(
        bottomBar = {
            BottomBar(navController, selectedIndex = 3)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { TopIconBar(navController, selectedTab) }

            item {
                TabSwitcher(selectedTab) { newTab ->
                    if (newTab == 0) {
                        coursesTabClickSignal++
                    }
                    selectedTab = newTab
                }
            }

            when (selectedTab) {
                0 -> {
                    item {
                        CourseBanner(scrollToSecondSlideSignal = coursesTabClickSignal)
                    }
                    item { SectionTitle("Лучшее для тебя") }
                    item {
                        when {
                            coursesLoading -> {
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
                            coursesError != null -> {
                                Text(
                                    text = coursesError!!,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                            publicCourses.isEmpty() -> {
                                Text(
                                    text = "Пока нет курсов",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                            else -> {
                                CourseCardRow(navController, publicCourses)
                            }
                        }
                    }
                    item { SectionTitle("Курсы для родителей") }
                    items(
                        items = publicCourses,
                        key = { it.id }
                    ) { course ->
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            ParentCourseCard(navController, course)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                1 -> {
                    item {
                        SpecialistFilters(
                            labels = specialistFilterLabels,
                            selectedIndex = specialistFilterIndex,
                            onSelect = { specialistFilterIndex = it }
                        )
                    }
                    item { SectionTitleSimple("Рекомендации для вас") }
                    item {
                        when {
                            specialistsLoading && specialists.isEmpty() -> {
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
                            specialistsError != null && specialists.isEmpty() -> {
                                Text(
                                    text = specialistsError!!,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                            specialists.isEmpty() -> {
                                Text(
                                    text = "Специалисты не найдены",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                            else -> {
                                RecommendedSpecialistsRow(
                                    navController = navController,
                                    specialists = specialists.take(8)
                                )
                            }
                        }
                    }
                    item { SectionTitleSimple("Все специалисты") }
                    itemsIndexed(
                        items = specialists,
                        key = { index, spec -> "${spec.id}_$index" }
                    ) { _, spec ->
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            AllSpecialistCard(navController, spec)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun TopIconBar(navController: NavController, selectedTab: Int) {
    val cartCount = CartRepository.items.size
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon_search),
            contentDescription = null,
            modifier = Modifier
                .size(22.dp)
                .clickable {
                    if (selectedTab == 0) navController.navigate("CourseSearchPage")
                    else navController.navigate("SpecialistSearchPage")
                }
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.icon_heart),
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { navController.navigate("FavoritesPage") }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.clickable { navController.navigate("CartPage") }) {
                Image(
                    painter = painterResource(id = R.drawable.icon_bag),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                        .background(Color(0xFF006FFD), CircleShape)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cartCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        lineHeight = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TabSwitcher(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .background(Color(0xFFF1F3F6), RoundedCornerShape(50))
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(3.dp)
                .clip(RoundedCornerShape(50))
                .background(if (selectedTab == 0) Color.White else Color.Transparent)
                .clickable { onTabSelected(0) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Курсы",
                fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selectedTab == 0) Color.Black else Color.Gray
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(3.dp)
                .clip(RoundedCornerShape(50))
                .background(if (selectedTab == 1) Color.White else Color.Transparent)
                .clickable { onTabSelected(1) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Специалисты",
                fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selectedTab == 1) Color.Black else Color.Gray
            )
        }
    }
}

@Composable
private fun CourseBanner(scrollToSecondSlideSignal: Int) {
    var slides by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        val loaded = withContext(Dispatchers.IO) {
            runCatching { ApiClient.withNetworkRetry { ApiClient.api.getCoursePreviews() } }.getOrNull()
        }
        loading = false
        slides = loaded.orEmpty().mapNotNull { item ->
            val url = resolveCourseImageUrl(item.preview_image) ?: return@mapNotNull null
            item.id to url
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 20.dp)
        ) {
            when {
                loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFDCE6F7), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Color(0xFF006FFD),
                            strokeWidth = 3.dp
                        )
                    }
                }
                slides.isNotEmpty() -> {
                    CourseBannerPager(slides = slides, scrollToSecondSlideSignal = scrollToSecondSlideSignal)
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFDCE6F7), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icon6),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFFB0BDD4)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CourseBannerPager(
    slides: List<Pair<Int, String>>,
    scrollToSecondSlideSignal: Int
) {
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(scrollToSecondSlideSignal) {
        if (scrollToSecondSlideSignal > 0 && slides.size > 1) {
            pagerState.animateScrollToPage(1)
        }
    }

    fun goToNextSlide() {
        if (slides.size <= 1) return
        scope.launch {
            val next = (pagerState.currentPage + 1) % slides.size
            pagerState.animateScrollToPage(next)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        ) { page ->
            val (_, url) = slides[page]
            val tapOnImage = remember(page) { MutableInteractionSource() }
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = tapOnImage,
                        indication = null
                    ) { goToNextSlide() },
                contentScale = ContentScale.Crop
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slides.forEachIndexed { index, _ ->
                key(index) {
                    val dotTap = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) {
                                    Color(0xFF006FFD)
                                } else {
                                    Color(0xFFEFF1F5)
                                },
                                CircleShape
                            )
                            .clickable(
                                interactionSource = dotTap,
                                indication = null
                            ) {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseCardRow(navController: NavController, courses: List<CourseCardResponse>) {
    LazyRow(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = courses,
            key = { it.id }
        ) { course ->
            CatalogCourseCard(navController, course)
        }
    }
}

@Composable
private fun CatalogCourseCard(navController: NavController, course: CourseCardResponse) {
    val ctx = LocalContext.current
    val imageUrl = course.previewImageUrlResolved()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F6)),
        modifier = Modifier
            .width(160.dp)
            .height(210.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { navController.navigate("CourseDetailsPage/${course.id}") }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(Color(0xFFDCE6F7)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(ctx)
                                .data(imageUrl)
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
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFFB0BDD4)
                        )
                    }
                }
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Text(
                        course.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2
                    )
                    Text(
                        course.specialist_name,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    course.formattedPriceTenge(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                FavoriteCourseIcon(course)
                CourseAddToCartIcon(course)
            }
        }
    }
}

@Composable
private fun CourseAddToCartIcon(course: CourseCardResponse) {
    CartRepository.items
    val inCart = CartRepository.contains(course.id)
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color(0xFFE8F1FF))
            .clickable {
                if (CartRepository.contains(course.id)) {
                    CartRepository.remove(course.id)
                } else {
                    CartRepository.add(
                        CartItem(
                            course.id,
                            course.title,
                            course.specialist_name,
                            course.priceAsDouble()
                        )
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_bag),
            contentDescription = if (inCart) "В корзине" else "В корзину",
            modifier = Modifier.size(16.dp),
            tint = if (inCart) Color(0xFF4CAF50) else Color(0xFF006FFD)
        )
    }
}

@Composable
private fun FavoriteCourseIcon(course: CourseCardResponse) {
    FavoritesRepository.items
    val isFavorite = FavoritesRepository.contains(course.id)
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color(0xFFE8F1FF))
            .clickable {
                FavoritesRepository.toggle(
                    FavoriteItem(
                        id = course.id,
                        title = course.title,
                        author = course.specialist_name,
                        price = course.priceAsDouble(),
                        previewImageUrl = course.previewImageUrlResolved()
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_heart),
            contentDescription = if (isFavorite) "В избранном" else "В избранное",
            modifier = Modifier.size(16.dp),
            tint = if (isFavorite) Color(0xFF006FFD) else Color(0xFF9E9E9E)
        )
    }
}

@Composable
private fun ParentCourseCard(navController: NavController, course: CourseCardResponse) {
    val ctx = LocalContext.current
    val imageUrl = course.previewImageUrlResolved()
    val thumbSize = 88.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F6FA), RoundedCornerShape(14.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(thumbSize)
                .background(
                    Color(0xFFDCE6F5),
                    RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                )
                .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                .clickable { navController.navigate("CourseDetailsPage/${course.id}") },
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(imageUrl)
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
                    modifier = Modifier.size(26.dp),
                    tint = Color(0xFFB0BDD4)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier.clickable { navController.navigate("CourseDetailsPage/${course.id}") }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    FlowRow(
                        modifier = Modifier.weight(1f, fill = false),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        course.displayTagLabels(max = 3).forEach { label ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF2F6FED), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Text(
                        course.ratingShortText(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFF006FFD),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    course.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    course.specialist_name,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    course.formattedPriceTenge(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                FavoriteCourseIcon(course)
                CourseAddToCartIcon(course)
            }
        }
    }
}

// =================== СПЕЦИАЛИСТЫ TAB ===================

@Composable
private fun SpecialistFilters(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color(0xFF006FFD) else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color(0xFF006FFD) else Color(0xFFD0D5DD),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.White else Color.Black
                )
            }
        }
    }
}

@Composable
private fun RecommendedSpecialistsRow(
    navController: NavController,
    specialists: List<SpecialistCardResponse>
) {
    LazyRow(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = specialists,
            key = { index, spec -> "${spec.id}_$index" }
        ) { _, spec ->
            RecommendedSpecialistCard(navController, spec)
        }
    }
}

@Composable
private fun RecommendedSpecialistCard(
    navController: NavController,
    spec: SpecialistCardResponse
) {
    val ctx = LocalContext.current
    val avatarUrl = spec.avatarUrlResolved()
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA)),
        modifier = Modifier.width(210.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
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
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(spec.full_name.orEmpty(), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
            Text(spec.specialization.orEmpty(), fontSize = 11.sp, color = Color.Gray, maxLines = 1)

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SpecBadge(spec.ratingWithReviews())
                SpecBadge(spec.experienceBadgeText())
            }

            Spacer(modifier = Modifier.height(4.dp))

            SpecBadge(spec.specialistPriceFromDisplay())

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                (spec.short_description ?: "").ifBlank { " " },
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 2,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .border(1.dp, Color(0xFF006FFD), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { navController.navigate("SpecialistProfilePage/${spec.id}") },
                contentAlignment = Alignment.Center
            ) {
                Text("Подробнее", color = Color(0xFF006FFD), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun AllSpecialistCard(navController: NavController, spec: SpecialistCardResponse) {
    val ctx = LocalContext.current
    val avatarUrl = spec.avatarUrlResolved()
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                    Text(spec.full_name.orEmpty(), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1)
                    Text(spec.specialization.orEmpty(), fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }

                Box(
                    modifier = Modifier
                        .border(1.dp, Color(0xFF006FFD), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { navController.navigate("SpecialistProfilePage/${spec.id}") }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Подробнее", color = Color(0xFF006FFD), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SpecBadge(spec.ratingWithReviews())
                SpecBadge(spec.experienceBadgeText())
                SpecBadge(spec.specialistPriceFromDisplay())
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                (spec.short_description ?: "").ifBlank { " " },
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 3,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SpecBadge(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFE7F0FF), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 10.sp, color = Color(0xFF006FFD), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionTitleSimple(title: String) {
    Text(
        title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun SpecialistsPagePreview() {
    SpecialistsPage(navController = rememberNavController())
}
