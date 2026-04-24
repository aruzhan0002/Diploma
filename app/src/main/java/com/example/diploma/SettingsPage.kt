package kz.aruzhan.care_steps

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kz.aruzhan.care_steps.data.remote.ApiClient
import kz.aruzhan.care_steps.data.remote.ChangePasswordRequest
import kz.aruzhan.care_steps.data.remote.CreateChildRequest
import kz.aruzhan.care_steps.data.remote.ParentAddressRequest
import kz.aruzhan.care_steps.data.remote.ProfileRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsPage(navController: NavController) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf("Вероника Петрова") }
    var profileNick by remember { mutableStateOf("@nica_petro") }
    var profileAvatarUrl by remember { mutableStateOf<String?>(null) }
    var avatarVersion by remember { mutableStateOf(0L) }
    val backStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(backStackEntry?.destination?.route) {
        val profile = withContext(Dispatchers.IO) {
            runCatching { ApiClient.withNetworkRetry { ApiClient.api.getParentSettingsProfile() } }.getOrNull()
        }
        if (profile != null) {
            profileName = profile.full_name
            profileNick = "@${profile.email.substringBefore("@").ifBlank { "parent" }}"
            profileAvatarUrl = resolveCourseImageUrl(profile.avatar)
            avatarVersion = System.currentTimeMillis()
        }
    }

    Scaffold(
        bottomBar = {
            BottomBar(navController, selectedIndex = 4)
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
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // -------- Аватар --------

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAF2FF))
                ) {
                    if (!profileAvatarUrl.isNullOrBlank()) {
                        val avatarWithVersion = appendAvatarVersion(profileAvatarUrl, avatarVersion)
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarWithVersion)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = profileName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = profileNick,
                    fontSize = 12.sp,
                    color = Color(0xFF8D8D8D)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(12.dp))

            SettingsRow("Профиль") { navController.navigate("ParentProfileEditPage") }
            Divider(color = Color(0xFFEDEDED))

            SettingsRow("Ребенок") { navController.navigate("ParentChildEditPage") }
            Divider(color = Color(0xFFEDEDED))

            SettingsRow(
                title = "Выйти",
                textColor = Color(0xFFFF3B30),
                onClick = {
                    showLogoutDialog = true
                }
            )
            Divider(color = Color(0xFFEDEDED))
        }
    }
    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onCancel = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                navController.navigate("startScreen") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}


@Composable
private fun SettingsRow(
    title: String,
    textColor: Color = Color.Black,
    onClick: () -> Unit = {}
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
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "›",
            fontSize = 22.sp,
            color = Color(0xFFB0B0B0)
        )
    }
}

@Composable
fun BottomBar(
    navController: NavController,
    selectedIndex: Int
) {

    val items = listOf(
        Triple(R.drawable.ic_mood_tracking_nav, "Трекер", "ParentInsightsPage"),
        Triple(R.drawable.chat_bot, "Чат-бот", "ChatBotPage"),
        Triple(R.drawable.ic_nav_categories, "Ребенок", "ChildModePage"),
        Triple(R.drawable.ic_nav_shop, "Курсы", "SpecialistsPage"),
        Triple(R.drawable.user_parent, "Настройки", "SettingsPage")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {

        items.forEachIndexed { index, item ->

            val isSelected = index == selectedIndex
            val color = if (isSelected) Color(0xFF006FFD) else Color(0xFFBDBDBD)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    navController.navigate(item.third) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            ) {

                Image(
                    painter = painterResource(id = item.first),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(color)
                )

                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.second,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
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
            Text("Выйти", fontSize = 30.sp / 2, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Вы уверены что хотите выйти? Вам\nнужно будет войти повторно",
                fontSize = 13.sp,
                color = Color(0xFF8A8A8A),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
fun ParentProfileEditPage(navController: NavController) {
    val context = LocalContext.current
    var fullName by rememberSaveable { mutableStateOf("Вероника Петрова") }
    var relation by rememberSaveable { mutableStateOf("Мама") }
    var email by rememberSaveable { mutableStateOf("name@email.com") }
    var oldPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmNewPassword by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var avatarUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedAvatarUri by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedAvatarUri = uri.toString()
            saveMessage = null
        }
    }

    LaunchedEffect(Unit) {
        loading = true
        val profile = withContext(Dispatchers.IO) {
            runCatching { ApiClient.withNetworkRetry { ApiClient.api.getParentSettingsProfile() } }.getOrNull()
        }
        val addressResp = withContext(Dispatchers.IO) {
            runCatching { ApiClient.withNetworkRetry { ApiClient.api.getParentAddress() } }.getOrNull()
        }
        if (profile != null) {
            fullName = profile.full_name
            email = profile.email
            relation = mapRelationship(profile.relationship, profile.relationship_other)
            avatarUrl = resolveCourseImageUrl(profile.avatar)
        }
        if (addressResp != null) {
            address = addressResp.address
        }
        loading = false
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF2F6FED))
                    .clickable {
                        scope.launch {
                            saveMessage = null
                            val profileResult = withContext(Dispatchers.IO) {
                                runCatching {
                                    val rel = toApiRelationship(relation)
                                    ApiClient.withNetworkRetry {
                                        ApiClient.api.updateParentSettingsProfile(
                                            ProfileRequest(
                                                full_name = fullName.trim(),
                                                relationship = rel.first,
                                                relationship_other = rel.second
                                            )
                                        )
                                    }
                                }.map { Unit }
                            }
                            if (profileResult.isFailure) {
                                saveMessage = "Не удалось обновить профиль"
                                return@launch
                            }

                            val passFilled = oldPassword.isNotBlank() || newPassword.isNotBlank() || confirmNewPassword.isNotBlank()
                            if (passFilled && (oldPassword.isBlank() || newPassword.isBlank() || confirmNewPassword.isBlank())) {
                                saveMessage = "Заполните старый, новый и подтверждение"
                                return@launch
                            }
                            if (passFilled && newPassword != confirmNewPassword) {
                                saveMessage = "Пароли не совпадают"
                                return@launch
                            }
                            val passResult = withContext(Dispatchers.IO) {
                                if (!passFilled) Result.success(Unit) else runCatching {
                                    ApiClient.withNetworkRetry {
                                        val response = ApiClient.api.changeParentPassword(
                                            ChangePasswordRequest(
                                                old_password = oldPassword,
                                                new_password = newPassword,
                                                new_password_confirm = confirmNewPassword
                                            )
                                        )
                                        if (!response.isSuccessful) {
                                            val serverError = runCatching { response.errorBody()?.string() }.getOrNull()
                                            throw IllegalStateException(
                                                serverError?.takeIf { it.isNotBlank() }
                                                    ?: "change-password failed: ${response.code()}"
                                            )
                                        }
                                    }
                                }.map { Unit }
                            }
                            if (passResult.isFailure) {
                                saveMessage = passResult.exceptionOrNull()?.message ?: "Не удалось обновить пароль"
                                return@launch
                            }
                            val addressResult = withContext(Dispatchers.IO) {
                                if (address.isBlank()) Result.success(Unit) else runCatching {
                                    ApiClient.withNetworkRetry {
                                        val body = ParentAddressRequest(address.trim())
                                        runCatching { ApiClient.api.createParentAddress(body) }
                                            .getOrElse { ApiClient.api.updateParentAddress(body) }
                                    }
                                }.map { Unit }
                            }
                            if (addressResult.isFailure) {
                                saveMessage = "Не удалось сохранить адрес"
                                return@launch
                            }

                            val avatarResult = withContext(Dispatchers.IO) {
                                if (selectedAvatarUri.isNullOrBlank()) {
                                    Result.success(Unit)
                                } else {
                                    runCatching {
                                        val part = buildAvatarPartFromUri(
                                            context = context,
                                            uri = Uri.parse(selectedAvatarUri!!)
                                        ) ?: throw IllegalStateException("Не удалось подготовить изображение")
                                        val response = ApiClient.withNetworkRetry {
                                            ApiClient.api.patchParentAvatar(part)
                                        }
                                        if (!response.isSuccessful) {
                                            val serverError = runCatching { response.errorBody()?.string() }.getOrNull()
                                            throw IllegalStateException(
                                                serverError?.takeIf { it.isNotBlank() }
                                                    ?: "avatar upload failed: ${response.code()}"
                                            )
                                        }
                                        avatarUrl = response.body()?.avatar ?: avatarUrl
                                    }.map { Unit }
                                }
                            }
                            if (avatarResult.isFailure) {
                                saveMessage = avatarResult.exceptionOrNull()?.message ?: "Не удалось загрузить изображение"
                                return@launch
                            }

                            saveMessage = "Изменения сохранены"
                            navController.popBackStack()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Сохранить изменения", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("‹", fontSize = 28.sp, color = Color(0xFF2F6FED), modifier = Modifier.clickable { navController.popBackStack() })
                Text("Профиль", fontSize = 19.sp / 1.2f, fontWeight = FontWeight.SemiBold)
                Text("Отмена", color = Color(0xFF2F6FED), modifier = Modifier.clickable { navController.popBackStack() })
            }
            Spacer(modifier = Modifier.height(18.dp))
            if (loading) {
                Text("Загрузка...", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            saveMessage?.let {
                Text(it, color = Color(0xFF666666), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            ProfileInput("Имя и фамилия", fullName) { fullName = it }
            ProfileInput("Кем вы приходитесь ребенку?", relation) { relation = it }
            ProfileInput("Ваша почта", email) { email = it }
            Text("Смена пароля", fontWeight = FontWeight.SemiBold, fontSize = 15.sp / 1.2f)
            Spacer(modifier = Modifier.height(8.dp))
            ProfilePasswordInput(oldPassword, "Старый пароль") { oldPassword = it }
            ProfilePasswordInput(newPassword, "Новый пароль") { newPassword = it }
            ProfilePasswordInput(confirmNewPassword, "Подтвердите новый пароль") { confirmNewPassword = it }
            ProfileInput("Адрес", address, "Укажите ваш адрес") { address = it }
            AvatarUploadSection(
                imageUri = selectedAvatarUri,
                imageUrl = avatarUrl
            ) { avatarPicker.launch("image/*") }
        }
    }
}

private fun mapRelationship(relationship: String, other: String?): String = when (relationship.lowercase()) {
    "mom" -> "Мама"
    "dad" -> "Папа"
    "mother" -> "Мама"
    "father" -> "Папа"
    "guardian" -> "Опекун"
    "other" -> other?.ifBlank { null } ?: "Другое"
    else -> relationship
}

private fun toApiRelationship(uiValue: String): Pair<String, String?> = when (uiValue.trim().lowercase()) {
    "мама" -> "mom" to null
    "папа" -> "dad" to null
    "опекун" -> "guardian" to null
    "другое" -> "other" to "Другое"
    else -> "other" to uiValue.trim()
}

@Composable
private fun ProfileInput(label: String, value: String, placeholder: String = "", onValue: (String) -> Unit) {
    if (label.isNotBlank()) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp / 1.2f)
        Spacer(modifier = Modifier.height(8.dp))
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        placeholder = { if (placeholder.isNotBlank()) Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFD3D8E3),
            unfocusedBorderColor = Color(0xFFD3D8E3),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun ProfilePasswordInput(value: String, placeholder: String, onValue: (String) -> Unit) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFD3D8E3),
            unfocusedBorderColor = Color(0xFFD3D8E3),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            Text(
                text = if (visible) "🙈" else "👁",
                modifier = Modifier.clickable { visible = !visible }
            )
        },
        singleLine = true
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun AvatarUploadSection(
    imageUri: String?,
    imageUrl: String?,
    onPickImage: () -> Unit
) {
    Text("Изображение", fontWeight = FontWeight.SemiBold, fontSize = 15.sp / 1.2f)
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFD3D8E3), RoundedCornerShape(14.dp))
            .clickable { onPickImage() }
            .background(Color(0xFFFAFAFC)),
        contentAlignment = Alignment.Center
    ) {
        val previewModel: Any? = when {
            !imageUri.isNullOrBlank() -> Uri.parse(imageUri)
            !imageUrl.isNullOrBlank() -> imageUrl
            else -> null
        }
        if (previewModel == null) {
            Text(
                text = "Нажмите чтобы загрузить изображение",
                color = Color(0xFF8D92A1),
                fontSize = 14.sp
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(previewModel).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    Spacer(modifier = Modifier.height(14.dp))
}

private fun resolveFileNameFromUri(context: android.content.Context, uri: Uri): String {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
        if (c.moveToFirst()) {
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0) {
                val name = c.getString(idx)
                if (!name.isNullOrBlank()) return name
            }
        }
    }
    return "avatar.jpg"
}

private fun buildAvatarPartFromUri(
    context: android.content.Context,
    uri: Uri
): MultipartBody.Part? {
    val cr = context.contentResolver
    val bytes = runCatching { cr.openInputStream(uri)?.use { it.readBytes() } }.getOrNull() ?: return null
    val mime = cr.getType(uri) ?: "image/jpeg"
    val name = resolveFileNameFromUri(context, uri)
    val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("avatar", name, body)
}

@Composable
fun ParentChildEditPage(navController: NavController) {
    var fullName by rememberSaveable { mutableStateOf("Павел Василенко") }
    var age by rememberSaveable { mutableStateOf("5 лет") }
    var lessonTime by rememberSaveable { mutableStateOf("5 минут") }
    var developmentType by rememberSaveable { mutableStateOf("🧩 Аутизм (РАС)") }
    var communicationLevel by rememberSaveable { mutableStateOf("Не говорит") }
    var instructionLevel by rememberSaveable { mutableStateOf("Только жесты / картинки") }
    var sensitivity by rememberSaveable { mutableStateOf("🔊 громкие звуки") }
    var difficulty by rememberSaveable { mutableStateOf("нажимать маленькие кнопки") }
    var features by rememberSaveable { mutableStateOf("Быстро устает") }
    var motivation by rememberSaveable { mutableStateOf("🌟 звёзды") }
    var loading by remember { mutableStateOf(false) }
    var interests by rememberSaveable { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val lessonTimeOptions = listOf("5 минут", "10 минут", "15 минут", "Свободно")
    val developmentTypeOptions = listOf("🧩 Аутизм (РАС)", "🧠 Синдром Дауна", "🌱 ЗПР", "❓ Пока не знаем / не уверены")
    val communicationOptions = listOf("Не говорит", "Говорит отдельные слова", "Говорит фразами", "Говорит свободно")
    val instructionOptions = listOf("Только жесты / картинки", "Короткие инструкции", "Понимает объяснения")
    val sensitivityOptions = listOf("🎧 громкие звуки", "💡 яркий свет", "✨ анимации", "📳 вибрации", "👥 персонажи / лица")
    val difficultyOptions = listOf("нажимать маленькие кнопки", "удерживать палец", "делать drag & drop")
    val featuresOptions = listOf("Быстро устает", "Расстраивается при ошибке", "Боится нового", "Любит повторения")
    val motivationOptions = listOf("⭐ звёзды", "👏 аплодисменты", "🧸 наклейки", "📺 мультик после занятия")

    LaunchedEffect(Unit) {
        loading = true
        val child = withContext(Dispatchers.IO) {
            runCatching { ApiClient.withNetworkRetry { ApiClient.api.getParentSettingsChild() } }.getOrNull()
        }
        if (child != null) {
            fullName = child.name
            age = "${child.age} лет"
            lessonTime = mapLessonTime(child.comfortable_duration)
            developmentType = mapDevelopmentType(child.development_type)
            communicationLevel = mapCommunicationStyle(child.communication_style)
            instructionLevel = mapInstructions(child.understands_instructions)
            sensitivity = mapSensitivity(child.sensory_sensitivities)
            difficulty = mapDifficulties(child.motor_difficulties)
            features = mapBehaviorNotices(child.behavior_notices)
            motivation = mapMotivators(child.motivators)
            interests = child.interests
        }
        loading = false
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF2F6FED))
                    .clickable {
                        scope.launch {
                            val ageNumber = age.filter { it.isDigit() }.toIntOrNull()
                            if (fullName.isBlank() || ageNumber == null) {
                                saveMessage = "Заполните имя и возраст корректно"
                                return@launch
                            }

                            val body = CreateChildRequest(
                                name = fullName.trim(),
                                age = ageNumber,
                                development_type = toApiDevelopmentType(developmentType),
                                communication_style = toApiCommunicationStyle(communicationLevel),
                                understands_instructions = toApiInstructionLevel(instructionLevel),
                                sensory_sensitivities = toApiSensitivities(sensitivity),
                                motor_difficulties = toApiDifficulties(difficulty),
                                behavior_notices = toApiBehaviorNotices(features),
                                motivators = toApiMotivators(motivation),
                                interests = interests,
                                comfortable_duration = toApiLessonTime(lessonTime)
                            )

                            val result = withContext(Dispatchers.IO) {
                                runCatching {
                                    ApiClient.withNetworkRetry {
                                        val response = ApiClient.api.updateParentSettingsChild(body)
                                        if (!response.isSuccessful) {
                                            val serverError = runCatching { response.errorBody()?.string() }.getOrNull()
                                            throw IllegalStateException(
                                                serverError?.takeIf { it.isNotBlank() }
                                                    ?: "update-child failed: ${response.code()}"
                                            )
                                        }
                                    }
                                }
                            }

                            if (result.isFailure) {
                                saveMessage = result.exceptionOrNull()?.message ?: "Не удалось сохранить изменения"
                                return@launch
                            }

                            saveMessage = "Изменения сохранены"
                            navController.popBackStack()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Сохранить изменения", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("‹", fontSize = 28.sp, color = Color(0xFF2F6FED), modifier = Modifier.clickable { navController.popBackStack() })
                Text("Ребенок", fontSize = 19.sp / 1.2f, fontWeight = FontWeight.SemiBold)
                Text("Отмена", color = Color(0xFF2F6FED), modifier = Modifier.clickable { navController.popBackStack() })
            }
            Spacer(modifier = Modifier.height(18.dp))
            if (loading) {
                Text("Загрузка...", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            saveMessage?.let {
                Text(it, color = Color(0xFF666666), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            ProfileInput("Имя и фамилия", fullName) { fullName = it }
            ProfileInput("Возраст ребенка", age) { age = it }
            ChildSelectField("Время занятий", lessonTime, lessonTimeOptions) { lessonTime = it }
            ChildSelectField("Тип развития", developmentType, developmentTypeOptions) { developmentType = it }
            ChildSelectField("Уровень общения", communicationLevel, communicationOptions) { communicationLevel = it }
            ChildSelectField("Понимание инструкций", instructionLevel, instructionOptions) { instructionLevel = it }
            ChildMultiSelectField("Чувствительность", sensitivity, sensitivityOptions) { sensitivity = it }
            ChildMultiSelectField("Сложности", difficulty, difficultyOptions) { difficulty = it }
            ChildMultiSelectField("Особенности", features, featuresOptions) { features = it }
            ChildMultiSelectField("Мотивация ребенка", motivation, motivationOptions) { motivation = it }
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

private fun mapDevelopmentType(value: String): String = when (value.lowercase()) {
    "autism" -> "🧩 Аутизм (РАС)"
    "autism_type" -> "🧩 Аутизм (РАС)"
    "down_syndrome" -> "🧠 Синдром Дауна"
    "speech_delay", "zpr" -> "🌱 ЗПР"
    "unknown", "adhd_type", "typical_type" -> "❓ Пока не знаем / не уверены"
    else -> value
}

private fun toApiDevelopmentType(value: String): String = when (value.trim().lowercase()) {
    "🧩 аутизм (рас)", "аутизм (рас)" -> "autism"
    "🧠 синдром дауна", "синдром дауна" -> "down_syndrome"
    "🌱 зпр", "зпр", "🗣️ задержка речи", "🗣 задержка речи", "задержка речи" -> "zpr"
    "❓ пока не знаем / не уверены", "пока не знаем / не уверены" -> "unknown"
    else -> value
}

private fun mapCommunicationStyle(value: String): String = when (value.lowercase()) {
    "no_speech" -> "Не говорит"
    "single_words", "uses_words" -> "Говорит отдельные слова"
    "phrases", "uses_short_phrases" -> "Говорит фразами"
    "fluent", "full_sentences" -> "Говорит свободно"
    else -> value
}

private fun toApiCommunicationStyle(value: String): String = when (value.trim().lowercase()) {
    "не говорит" -> "no_speech"
    "говорит отдельные слова", "отдельные слова" -> "single_words"
    "говорит фразами", "короткие фразы" -> "phrases"
    "говорит свободно", "полные предложения" -> "fluent"
    else -> value
}

private fun mapInstructions(value: String): String = when (value.lowercase()) {
    "gestures_pictures" -> "Только жесты / картинки"
    "short_instructions", "simple_verbal" -> "Короткие инструкции"
    "understands_explanations", "multi_step" -> "Понимает объяснения"
    else -> value
}

private fun toApiInstructionLevel(value: String): String = when (value.trim().lowercase()) {
    "только жесты / картинки" -> "gestures_pictures"
    "короткие инструкции", "простые устные инструкции" -> "short_instructions"
    "понимает объяснения", "многошаговые инструкции" -> "understands_explanations"
    else -> value
}

private fun mapSensitivity(value: String): String {
    if (value.isBlank()) return "—"
    val mapped = value.split(",")
        .map { it.trim().lowercase() }
        .map {
            when (it) {
                "loud_sounds", "loud_sounds/" -> "🎧 громкие звуки"
                "animations" -> "✨ анимации"
                "vibrations" -> "📳 вибрации"
                "bright_light", "bright_lights" -> "💡 яркий свет"
                "characters", "faces", "characters_faces", "faces_characters" -> "👥 персонажи / лица"
                else -> it
            }
        }
        .filter { it.isNotBlank() }
    return if (mapped.isEmpty()) "—" else mapped.joinToString(", ")
}

private fun mapLessonTime(value: String): String = when (value.trim().lowercase()) {
    "5_min", "5 минут" -> "5 минут"
    "10_min", "10 минут" -> "10 минут"
    "15_min", "15 минут" -> "15 минут"
    "unlimited", "свободно" -> "Свободно"
    else -> value
}

private fun toApiLessonTime(value: String): String = when (value.trim().lowercase()) {
    "5 минут", "5_min" -> "5_min"
    "10 минут", "10_min" -> "10_min"
    "15 минут", "15_min" -> "15_min"
    "свободно", "unlimited" -> "unlimited"
    else -> value
}

private fun mapDifficulties(value: String): String {
    if (value.isBlank()) return "—"
    val mapped = value.split(",")
        .map { it.trim().lowercase() }
        .map {
            when (it) {
                "small_buttons" -> "нажимать маленькие кнопки"
                "hold_finger" -> "удерживать палец"
                "drag_drop", "drag_and_drop" -> "делать drag & drop"
                else -> it
            }
        }
        .filter { it.isNotBlank() }
    return if (mapped.isEmpty()) "—" else mapped.joinToString(", ")
}

private fun toApiDifficulties(value: String): String {
    if (value.isBlank() || value == "—") return ""
    return value.split(",")
        .map { it.trim().lowercase() }
        .map {
            when (it) {
                "нажимать маленькие кнопки" -> "small_buttons"
                "удерживать палец" -> "hold_finger"
                "делать drag & drop", "делать drag and drop" -> "drag_drop"
                else -> it
            }
        }
        .joinToString(",")
}

private fun mapBehaviorNotices(value: String): String {
    if (value.isBlank()) return "—"
    val mapped = value.split(",")
        .map { it.trim().lowercase() }
        .map {
            when (it) {
                "tires_quickly" -> "Быстро устает"
                "upset_by_mistake" -> "Расстраивается при ошибке"
                "fears_new" -> "Боится нового"
                "loves_repetition" -> "Любит повторения"
                else -> it
            }
        }
        .filter { it.isNotBlank() }
    return if (mapped.isEmpty()) "—" else mapped.joinToString(", ")
}

private fun toApiBehaviorNotices(value: String): String {
    if (value.isBlank() || value == "—") return ""
    return value.split(",")
        .map { it.trim().lowercase() }
        .map {
            when (it) {
                "быстро устает" -> "tires_quickly"
                "расстраивается при ошибке" -> "upset_by_mistake"
                "боится нового" -> "fears_new"
                "любит повторения" -> "loves_repetition"
                else -> it
            }
        }
        .joinToString(",")
}

private fun mapMotivators(value: String): String {
    if (value.isBlank()) return "—"
    val mapped = value.split(",")
        .map { it.trim().lowercase() }
        .map {
            when (it) {
                "stars" -> "⭐ звёзды"
                "applause" -> "👏 аплодисменты"
                "stickers" -> "🧸 наклейки"
                "cartoon" -> "📺 мультик после занятия"
                else -> it
            }
        }
        .filter { it.isNotBlank() }
    return if (mapped.isEmpty()) "—" else mapped.joinToString(", ")
}

private fun toApiMotivators(value: String): String {
    if (value.isBlank() || value == "—") return ""
    return value.split(",")
        .map { it.trim().lowercase() }
        .map {
            when (it) {
                "⭐ звёзды", "🌟 звёзды", "звёзды" -> "stars"
                "👏 аплодисменты", "аплодисменты" -> "applause"
                "🧸 наклейки", "наклейки" -> "stickers"
                "📺 мультик после занятия", "мультик после занятия" -> "cartoon"
                else -> it
            }
        }
        .joinToString(",")
}

private fun toApiSensitivities(value: String): String {
    if (value.isBlank() || value == "—") return ""
    return value.split(",")
        .map { it.trim().lowercase() }
        .map {
            when (it) {
                "🎧 громкие звуки", "🔊 громкие звуки", "громкие звуки" -> "loud_sounds"
                "💡 яркий свет", "яркий свет" -> "bright_light"
                "✨ анимации", "анимации" -> "animations"
                "📳 вибрации", "вибрации" -> "vibrations"
                "👥 персонажи / лица", "персонажи / лица" -> "faces_characters"
                else -> it
            }
        }
        .joinToString(",")
}

@Composable
private fun ChildSelectField(
    label: String,
    value: String,
    options: List<String>,
    onValue: (String) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }
    val normalizedOptions = remember(options, value) {
        if (value.isNotBlank() && options.none { it.equals(value, ignoreCase = true) }) {
            listOf(value) + options
        } else {
            options
        }
    }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFFD3D8E3),
        unfocusedBorderColor = Color(0xFFD3D8E3),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledBorderColor = Color(0xFFD3D8E3),
        disabledContainerColor = Color.White,
        disabledTextColor = Color(0xFF202020),
        disabledTrailingIconColor = Color(0xFF9AA2AF)
    )
    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openDialog = true }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
            trailingIcon = {
                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
            },
            readOnly = true,
            enabled = false,
            singleLine = true
        )
    }
    Spacer(modifier = Modifier.height(14.dp))

    if (openDialog) {
        Dialog(onDismissRequest = { openDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFC5C6CC), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    normalizedOptions.forEach { item ->
                        val isSelected = item == value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFEAF1FF) else Color.Transparent)
                                .clickable {
                                    openDialog = false
                                    onValue(item)
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item, modifier = Modifier.weight(1f), color = if (isSelected) Color.Black else Color(0xFF6B6B6B))
                            if (isSelected) Text("✓", color = Color(0xFF006FFD), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChildMultiSelectField(
    label: String,
    value: String,
    options: List<String>,
    onValue: (String) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }
    var selected by remember(value) {
        mutableStateOf(value.split(",").map { it.trim() }.filter { it.isNotBlank() && it != "—" }.toSet())
    }
    val displayValue = remember(value) { compactMultiValue(value) }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFFD3D8E3),
        unfocusedBorderColor = Color(0xFFD3D8E3),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledBorderColor = Color(0xFFD3D8E3),
        disabledContainerColor = Color.White,
        disabledTextColor = Color(0xFF202020),
        disabledTrailingIconColor = Color(0xFF9AA2AF)
    )

    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openDialog = true }
    ) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
            trailingIcon = {
                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
            },
            readOnly = true,
            enabled = false,
            singleLine = true
        )
    }
    Spacer(modifier = Modifier.height(14.dp))

    if (openDialog) {
        Dialog(onDismissRequest = {
            openDialog = false
            onValue(selected.toList().joinToString(", ").ifBlank { "—" })
        }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFC5C6CC), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    options.forEach { item ->
                        val isSelected = selected.contains(item)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFEAF1FF) else Color.Transparent)
                                .clickable {
                                    val updated = if (isSelected) selected - item else selected + item
                                    selected = updated
                                    onValue(updated.toList().joinToString(", ").ifBlank { "—" })
                                    openDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item, modifier = Modifier.weight(1f), color = if (isSelected) Color.Black else Color(0xFF6B6B6B))
                            if (isSelected) Text("✓", color = Color(0xFF006FFD), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

private fun compactMultiValue(value: String): String {
    val parts = value.split(",").map { it.trim() }.filter { it.isNotBlank() && it != "—" }
    return when {
        parts.isEmpty() -> "—"
        parts.size <= 2 -> parts.joinToString(", ")
        else -> "${parts[0]}, ${parts[1]} +${parts.size - 2}"
    }
}

private fun appendAvatarVersion(url: String?, version: Long): String? {
    if (url.isNullOrBlank()) return null
    val separator = if (url.contains("?")) "&" else "?"
    return "$url${separator}v=$version"
}

@Composable
private fun BottomItem(
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    Image(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        modifier = Modifier
            .size(20.dp)
            .clickable { onClick() }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsPage() {
    SettingsPage(navController = rememberNavController())
}