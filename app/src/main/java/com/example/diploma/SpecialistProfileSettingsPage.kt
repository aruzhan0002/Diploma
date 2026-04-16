package com.example.diploma

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.gson.JsonElement
import com.example.diploma.data.remote.ApiClient
import com.example.diploma.data.remote.ChangePasswordRequest
import com.example.diploma.data.remote.SpecialistSettingsUpdateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

private val fieldBorder = Color(0xFFD0D5DD)
private val labelColor = Color(0xFF344054)
private val placeholderColor = Color(0xFF667085)
private val sectionTitle = Color(0xFF101828)
private val specializationOptions = listOf(
    "ABA-терапия",
    "Логопед",
    "Нейропсихолог",
    "Эрготерапия",
    "Арт-терапия",
    "Сенсорная терапия",
    "Спецпедагог"
)
private val methodOptions = listOf("ABA", "PECS", "DIR / Floortime", "Сенсорная интеграция")
private val ageRangeOptions = listOf("2-4", "5-7", "8-10", "11-13", "Подростки", "2-4, Подростки")
private val timeZoneOptions = listOf("UTC/GMT +5 часов", "UTC/GMT +6 часов", "UTC/GMT +3 часов")
private val cityOptions = listOf("Алматы", "Астана", "Шымкент", "Караганда")

/** Swagger: codes из GET choices / как при регистрации. */
private fun specializationUiToApi(ui: String): String {
    val k = ui.trim().lowercase()
    return when (k) {
        "aba-терапия" -> "aba"
        "логопед" -> "speech_therapist"
        "нейропсихолог" -> "neuropsychologist"
        "эрготерапия" -> "occupational_therapy"
        "арт-терапия" -> "art_therapy"
        "сенсорная терапия" -> "sensory_therapy"
        "спецпедагог" -> "special_education"
        else -> ui.trim().lowercase().replace(" ", "_").replace("-", "_").ifBlank { "aba" }
    }
}

private fun specializationApiToUi(code: String): String {
    val c = code.trim().lowercase().trim('"', '[', ']')
    return when (c) {
        "aba" -> "ABA-терапия"
        "speech_therapist" -> "Логопед"
        "neuropsychologist" -> "Нейропсихолог"
        "occupational_therapy" -> "Эрготерапия"
        "art_therapy" -> "Арт-терапия"
        "sensory_therapy" -> "Сенсорная терапия"
        "special_education" -> "Спецпедагог"
        else -> code.trim()
    }
}

private fun methodUiToApi(ui: String): String {
    val k = ui.trim().lowercase()
    return when (k) {
        "aba" -> "aba"
        "pecs" -> "pecs"
        "dir / floortime" -> "dir_floortime"
        "сенсорная интеграция" -> "sensory_integration"
        else -> ui.trim().lowercase().replace(" ", "_").ifBlank { "aba" }
    }
}

private fun methodApiToUi(code: String): String {
    val c = code.trim().lowercase().trim('"', '[', ']')
    return when (c) {
        "aba" -> "ABA"
        "pecs" -> "PECS"
        "dir_floortime" -> "DIR / Floortime"
        "sensory_integration" -> "Сенсорная интеграция"
        else -> code.trim()
    }
}

private fun timeZoneUiToApi(ui: String): String {
    val t = ui.trim()
    if (t.contains("/") && !t.contains("часов")) return t
    return when {
        t.contains("+3", ignoreCase = true) -> "Europe/Moscow"
        t.contains("+5", ignoreCase = true) -> "Asia/Almaty"
        t.contains("+6", ignoreCase = true) -> "Asia/Almaty"
        else -> "Asia/Almaty"
    }
}

private fun timeZoneApiToUi(api: String): String {
    val a = api.trim()
    return when (a) {
        "Europe/Moscow" -> "UTC/GMT +3 часов"
        "Asia/Almaty", "Asia/Aqtobe", "Asia/Qyzylorda", "Asia/Astana" -> "UTC/GMT +5 часов"
        "Asia/Omsk" -> "UTC/GMT +6 часов"
        else -> if (a.isNotBlank() && a.contains("/")) a else api
    }
}

private fun sanitizeAgeRangeForApi(v: String): String {
    val t = v.trim()
    return t.substringBefore(",").trim().ifBlank { t }
}

private fun parseYearsToInt(experience: String, rawYears: Int?): Int? {
    Regex("\\d+").find(experience.trim())?.value?.toIntOrNull()?.let { return it }
    return rawYears
}

private fun formatExperienceForField(years: Int?): String =
    years?.toString().orEmpty()

private fun extractFirstString(raw: JsonElement?): String {
    if (raw == null || raw.isJsonNull) return ""
    if (raw.isJsonArray) {
        val first = raw.asJsonArray.firstOrNull() ?: return ""
        if (!first.isJsonPrimitive) return ""
        return first.asString.trim()
    }
    if (raw.isJsonPrimitive) return raw.asString.trim()
    return ""
}

private fun extractIntFromFlexibleField(raw: JsonElement?): Int? {
    if (raw == null || raw.isJsonNull) return null
    if (raw.isJsonPrimitive) {
        val primitive = raw.asJsonPrimitive
        if (primitive.isNumber) return primitive.asInt
        if (primitive.isString) return primitive.asString.trim().toIntOrNull()
    }
    return null
}

private fun mapSpecializationsFromResponse(raw: JsonElement?): String {
    val first = extractFirstString(raw)
    if (first.isBlank()) return ""
    return specializationApiToUi(first)
}

private fun mapMethodsFromResponse(raw: JsonElement?): String {
    val first = extractFirstString(raw)
    if (first.isBlank()) return ""
    return methodApiToUi(first)
}

@Composable
fun SpecialistProfileSettingsPage(navController: NavController) {
    val context = LocalContext.current
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var oldPassword by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var about by rememberSaveable { mutableStateOf("") }
    var specializations by rememberSaveable { mutableStateOf("") }
    var experience by rememberSaveable { mutableStateOf("") }
    var methods by rememberSaveable { mutableStateOf("") }
    var ageRange by rememberSaveable { mutableStateOf("") }
    var workFormat by rememberSaveable { mutableStateOf("online") }
    var timezone by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var avatarUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedAvatarUri by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var oldPwVisible by rememberSaveable { mutableStateOf(false) }
    var pwVisible by rememberSaveable { mutableStateOf(false) }
    var cpwVisible by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var rawExperienceYears by remember { mutableStateOf<Int?>(null) }
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
        val s = withContext(Dispatchers.IO) {
            runCatching { ApiClient.withNetworkRetry { ApiClient.api.getSpecialistSettings() } }.getOrNull()
        }
        if (s != null) {
            fullName = s.full_name
            email = s.email
            about = s.approach_description.orEmpty()
            avatarUrl = resolveCourseImageUrl(s.avatar)
            specializations = mapSpecializationsFromResponse(s.specializations)
            val years = extractIntFromFlexibleField(s.years_experience)
            rawExperienceYears = years
            experience = formatExperienceForField(years)
            methods = mapMethodsFromResponse(s.methods)
            ageRange = s.age_range.orEmpty()
            workFormat = s.work_format?.lowercase().orEmpty().ifBlank { "online" }
            timezone = timeZoneApiToUi(s.time_zone.orEmpty())
            city = s.city.orEmpty()
        } else {
            saveMessage = "Не удалось загрузить профиль. Проверьте сеть и попробуйте снова."
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "‹",
                fontSize = 26.sp,
                color = Color(0xFF006FFD),
                fontWeight = FontWeight.Light,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Text("Профиль", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Text(
                "Отмена",
                fontSize = 14.sp,
                color = Color(0xFF006FFD),
                modifier = Modifier.clickable { navController.popBackStack() }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (loading) {
                Text("Загрузка...", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
            }
            saveMessage?.let {
                Text(it, color = if (it.contains("сохранен")) Color(0xFF2E7D32) else Color.Red, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
            }

            SpecialistSectionTitle("Аккаунт")
            Spacer(modifier = Modifier.height(8.dp))
            FieldLabel("Имя и фамилия")
            SimpleField(fullName) { fullName = it }
            FieldLabel("Ваша почта")
            SimpleField(email) { email = it }
            FieldLabel("Старый пароль")
            PwField(oldPassword, "Введите старый пароль", oldPwVisible, { oldPwVisible = !oldPwVisible }) { oldPassword = it }
            FieldLabel("Пароль и подтверждение")
            PwField(password, "Измените пароль", pwVisible, { pwVisible = !pwVisible }) { password = it }
            PwField(confirmPassword, "Подтвердите пароль", cpwVisible, { cpwVisible = !cpwVisible }) { confirmPassword = it }

            Spacer(modifier = Modifier.height(12.dp))
            SpecialistSectionTitle("Информация специалиста")
            Spacer(modifier = Modifier.height(8.dp))
            FieldLabel("О вас")
            MultiField(about, 4) { about = it }
            SelectDialogField(
                label = "Ваша специализация",
                value = specializations,
                options = specializationOptions
            ) { specializations = it }
            FieldLabel("Стаж работы")
            SimpleField(experience) { experience = it }
            SelectDialogField(
                label = "Методики",
                value = methods,
                options = methodOptions
            ) { methods = it }
            SelectDialogField(
                label = "Возрастная категория",
                value = ageRange,
                options = ageRangeOptions
            ) { ageRange = it }

            Spacer(modifier = Modifier.height(12.dp))
            SpecialistSectionTitle("Работа")
            Spacer(modifier = Modifier.height(8.dp))
            FieldLabel("Формат работы")
            Spacer(modifier = Modifier.height(4.dp))
            FormatToggle(workFormat) { workFormat = it }
            Spacer(modifier = Modifier.height(10.dp))
            SelectDialogField(
                label = "Часовой пояс",
                value = timezone,
                options = timeZoneOptions
            ) { timezone = it }
            SelectDialogField(
                label = "Город",
                value = city,
                options = cityOptions
            ) { city = it }
            AvatarUploadSection(
                imageUri = selectedAvatarUri,
                imageUrl = avatarUrl
            ) { avatarPicker.launch("image/*") }

            Spacer(modifier = Modifier.height(20.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF006FFD))
                .clickable {
                    scope.launch {
                        saveMessage = null
                        val passFilled =
                            oldPassword.isNotBlank() || password.isNotBlank() || confirmPassword.isNotBlank()
                        if (passFilled) {
                            if (oldPassword.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                saveMessage = "Заполните старый, новый пароль и подтверждение"
                                return@launch
                            }
                            if (password != confirmPassword) {
                                saveMessage = "Новые пароли не совпадают"
                                return@launch
                            }
                        }
                        val yearsInt = parseYearsToInt(experience, rawExperienceYears)
                        if (yearsInt == null) {
                            saveMessage = "Укажите стаж работы числом (например, 5)"
                            return@launch
                        }
                        if (specializations.isBlank()) {
                            saveMessage = "Выберите специализацию"
                            return@launch
                        }
                        if (methods.isBlank()) {
                            saveMessage = "Выберите методику"
                            return@launch
                        }
                        val specCode = specializationUiToApi(specializations)
                        val methodCode = methodUiToApi(methods)
                        val tzApi = timeZoneUiToApi(timezone)
                        val ageApi = sanitizeAgeRangeForApi(ageRange)

                        val body = SpecialistSettingsUpdateRequest(
                            full_name = fullName.trim().ifBlank { null },
                            approach_description = about.trim().ifBlank { null },
                            specializations = listOf(specCode),
                            years_experience = yearsInt,
                            methods = listOf(methodCode),
                            age_range = ageApi.ifBlank { null },
                            work_format = workFormat.ifBlank { null },
                            time_zone = tzApi.ifBlank { null },
                            city = city.trim().ifBlank { null }
                        )

                        val result = withContext(Dispatchers.IO) {
                            runCatching {
                                val response = ApiClient.withNetworkRetry {
                                    ApiClient.api.updateSpecialistSettings(body)
                                }
                                if (!response.isSuccessful) {
                                    val err = runCatching { response.errorBody()?.string() }.getOrNull()
                                    throw IllegalStateException(
                                        err?.takeIf { it.isNotBlank() }
                                            ?: "Ошибка: ${response.code()}"
                                    )
                                }
                            }
                        }

                        if (result.isFailure) {
                            saveMessage = result.exceptionOrNull()?.message
                                ?: "Не удалось сохранить"
                            return@launch
                        }

                        if (!selectedAvatarUri.isNullOrBlank()) {
                            val avatarResult = withContext(Dispatchers.IO) {
                                runCatching {
                                    val part = buildAvatarPartFromUri(
                                        context = context,
                                        uri = Uri.parse(selectedAvatarUri!!)
                                    ) ?: throw IllegalStateException("Не удалось подготовить изображение")
                                    val response = ApiClient.withNetworkRetry {
                                        ApiClient.api.putSpecialistSettingsAvatar(part)
                                    }
                                    if (!response.isSuccessful) {
                                        val err = runCatching { response.errorBody()?.string() }.getOrNull()
                                        throw IllegalStateException(
                                            err?.takeIf { it.isNotBlank() }
                                                ?: "Загрузка изображения: ${response.code()}"
                                        )
                                    }
                                    avatarUrl = response.body()?.avatar ?: avatarUrl
                                }
                            }
                            if (avatarResult.isFailure) {
                                saveMessage = avatarResult.exceptionOrNull()?.message ?: "Не удалось загрузить изображение"
                                return@launch
                            }
                        }

                        if (passFilled) {
                            val passResult = withContext(Dispatchers.IO) {
                                runCatching {
                                    val response = ApiClient.withNetworkRetry {
                                        ApiClient.api.changeParentPassword(
                                            ChangePasswordRequest(
                                                old_password = oldPassword,
                                                new_password = password,
                                                new_password_confirm = confirmPassword
                                            )
                                        )
                                    }
                                    if (!response.isSuccessful) {
                                        val err = runCatching { response.errorBody()?.string() }.getOrNull()
                                        throw IllegalStateException(
                                            err?.takeIf { it.isNotBlank() }
                                                ?: "Смена пароля: ${response.code()}"
                                        )
                                    }
                                }
                            }
                            if (passResult.isFailure) {
                                saveMessage = passResult.exceptionOrNull()?.message
                                    ?: "Не удалось сменить пароль"
                                return@launch
                            }
                        }

                        saveMessage = "Изменения сохранены"
                        navController.popBackStack()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("Сохранить изменения", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun SpecialistSectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = sectionTitle)
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = labelColor)
    Spacer(modifier = Modifier.height(4.dp))
}

private val tfColors
    @Composable get() = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = fieldBorder,
        unfocusedBorderColor = fieldBorder,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

@Composable
private fun SimpleField(value: String, onValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = tfColors,
        textStyle = TextStyle(fontSize = 15.sp),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun MultiField(value: String, lines: Int, onValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = tfColors,
        textStyle = TextStyle(fontSize = 15.sp),
        singleLine = false,
        minLines = lines
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun ChevronField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = fieldBorder,
            unfocusedBorderColor = fieldBorder,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledBorderColor = fieldBorder,
            disabledContainerColor = Color.White,
            disabledTextColor = Color(0xFF101828),
            disabledTrailingIconColor = placeholderColor
        ),
        textStyle = TextStyle(fontSize = 15.sp),
        readOnly = true,
        enabled = false,
        singleLine = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = placeholderColor
            )
        }
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun SelectDialogField(
    label: String,
    value: String,
    options: List<String>,
    onValue: (String) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }
    val normalizedOptions = remember(options, value) {
        if (value.isNotBlank() && options.none { it.equals(value, ignoreCase = true) }) {
            listOf(value) + options
        } else options
    }

    FieldLabel(label)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openDialog = true }
    ) {
        ChevronField(value = value.ifBlank { "Выберите" })
    }

    if (openDialog) {
        Dialog(onDismissRequest = { openDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    normalizedOptions.forEach { item ->
                        val selected = item == value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) Color(0xFFEAF2FF) else Color.Transparent)
                                .clickable {
                                    onValue(item)
                                    openDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item,
                                modifier = Modifier.weight(1f),
                                color = if (selected) Color.Black else Color(0xFF6B6B6B)
                            )
                            if (selected) {
                                Text("✓", color = Color(0xFF006FFD), fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PwField(
    value: String,
    placeholder: String,
    visible: Boolean,
    onToggle: () -> Unit,
    onValue: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        placeholder = { Text(placeholder, color = placeholderColor, fontSize = 15.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = tfColors,
        textStyle = TextStyle(fontSize = 15.sp),
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
                Icon(
                    painter = painterResource(
                        id = if (visible) R.drawable.ic_password_show else R.drawable.ic_password_hide
                    ),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun AvatarUploadSection(
    imageUri: String?,
    imageUrl: String?,
    onPickImage: () -> Unit
) {
    FieldLabel("Изображение")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFAFAFC))
            .clickable { onPickImage() },
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
                color = placeholderColor,
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
    Spacer(modifier = Modifier.height(12.dp))
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
private fun FormatToggle(current: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(Color(0xFFF2F4F7), RoundedCornerShape(10.dp))
            .padding(3.dp)
    ) {
        val offline = current == "offline"
        val online = current == "online"
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (offline) Color.White else Color.Transparent)
                .clickable { onSelect("offline") },
            contentAlignment = Alignment.Center
        ) {
            Text("Офлайн", fontSize = 14.sp, color = if (offline) Color(0xFF101828) else Color(0xFF667085))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (online) Color.White else Color.Transparent)
                .clickable { onSelect("online") },
            contentAlignment = Alignment.Center
        ) {
            Text("Онлайн", fontSize = 14.sp, color = if (online) Color(0xFF101828) else Color(0xFF667085))
        }
    }
}
