@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.diploma

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import android.provider.OpenableColumns
import android.util.Log
import com.example.diploma.data.remote.ApiClient
import com.example.diploma.data.remote.ChoiceItem
import com.example.diploma.data.remote.RefreshRequest
import com.example.diploma.data.remote.TokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.util.Locale

data class CourseMaterial(
    val id: Int,
    val type: String,
    var title: String = "",
    var fileUri: String = "",
    var displayName: String = ""
)

@Composable
fun CreateCoursePage(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentStep by rememberSaveable { mutableStateOf(1) }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var outcomes by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var duration by rememberSaveable { mutableStateOf("9") }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var selectedLevelValue by rememberSaveable { mutableStateOf("beginner") }
    var selectedTags by rememberSaveable { mutableStateOf(setOf<String>()) }
    val materialsSaver = remember {
        listSaver<List<CourseMaterial>, String>(
            save = { list ->
                list.map { "${it.id}|${it.type}|${it.title}|${it.fileUri}|${it.displayName}" }
            },
            restore = { saved ->
                saved.mapNotNull { raw ->
                    val parts = raw.split('|')
                    if (parts.size < 4) null
                    else CourseMaterial(
                        id = parts[0].toIntOrNull() ?: 0,
                        type = parts[1],
                        title = parts[2],
                        fileUri = parts[3],
                        displayName = parts.getOrElse(4) { "" }
                    )
                }
            }
        )
    }
    var materials by rememberSaveable(stateSaver = materialsSaver) { mutableStateOf(listOf<CourseMaterial>()) }

    var createError by remember { mutableStateOf<String?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var coverUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedCoverUri: Uri? = coverUriString?.let { Uri.parse(it) }

    val categoryChoices = remember { defaultCategoryChoices() }
    val levelChoices = remember { defaultLevelChoices() }
    val tagChoices = remember { defaultTagChoices() }
    val scope = rememberCoroutineScope()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coverUriString = uri.toString()
        }
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Button(
                onClick = {
                    createError = null
                    Log.d("CreateCourse", "Button clicked, step=$currentStep")
                    if (currentStep == 1) {
                        if (title.isBlank() || description.isBlank() || selectedCategory.isBlank() || selectedLevelValue.isBlank() || price.isBlank()) {
                            createError = "Заполните обязательные поля"
                            Log.w("CreateCourse", "Step1 validation fail: title=${title.isBlank()}, desc=${description.isBlank()}, cat=${selectedCategory.isBlank()}, level=${selectedLevelValue.isBlank()}, price=${price.isBlank()}")
                            return@Button
                        }
                        if (selectedCoverUri == null) {
                            createError = "Загрузите обложку курса"
                            Log.w("CreateCourse", "Step1 validation fail: cover is null")
                            return@Button
                        }
                        Log.d("CreateCourse", "Step1 OK -> moving to step 2")
                        currentStep = 2
                        return@Button
                    }

                    val priceParsed = parseCoursePrice(price)
                    val durationInt = duration.trim().toIntOrNull() ?: 9
                    if (priceParsed == null || selectedLevelValue.isBlank()) {
                        createError = "Проверьте цену и уровень"
                        Log.w("CreateCourse", "Step2 validation fail: price=$priceParsed, level=${selectedLevelValue}")
                        return@Button
                    }
                    if (selectedCoverUri == null) {
                        createError = "Загрузите обложку курса"
                        Log.w("CreateCourse", "Step2 validation fail: cover URI lost")
                        return@Button
                    }
                    Log.d("CreateCourse", "Step2 validation OK, starting POST...")

                    isCreating = true
                    val priceStr = String.format(Locale.US, "%.2f", priceParsed)
                    var createFailureMessage: String? = null

                    suspend fun tryCreateCourse(): Boolean {
                        val uri = selectedCoverUri ?: return false

                        val tagParts = selectedTags.map { tag ->
                            MultipartBody.Part.createFormData("tags", tag)
                        }

                        suspend fun doCreateCourse(): Result<com.example.diploma.data.remote.CreatedCourseResponse> = try {
                            val response = withContext(Dispatchers.IO) {
                                Log.i("CreateCourse", "POST /api/courses/ tags=${selectedTags.toList()}")
                                val previewPart = buildPreviewImagePart(context, uri)
                                ApiClient.api.createCourse(
                                    title = plainBody(title.trim()),
                                    description = plainBody(description.trim()),
                                    category = plainBody(selectedCategory),
                                    level = plainBody(selectedLevelValue),
                                    price = plainBody(priceStr),
                                    duration = plainBody(durationInt.toString()),
                                    learningOutcomes = plainBody(outcomes.trim()),
                                    tags = tagParts,
                                    previewImage = previewPart
                                )
                            }
                            Log.i("CreateCourse", "Course created OK, id=${response.id}")
                            Result.success(response)
                        } catch (e: java.net.ProtocolException) {
                            Log.w("CreateCourse", "Course created (201) but response body truncated by server", e)
                            val stub = com.example.diploma.data.remote.CreatedCourseResponse(
                                id = -1, title = title.trim(), description = description.trim(),
                                category = selectedCategory, level = selectedLevelValue,
                                price = priceStr, duration = durationInt,
                                preview_image = null, modules = emptyList()
                            )
                            Result.success(stub)
                        } catch (e: Throwable) {
                            val msg = if (e is HttpException) {
                                val errorBody = try { e.response()?.errorBody()?.string() } catch (_: Throwable) { null }
                                Log.e("CreateCourse", "POST /api/courses/ failed ${e.code()}: $errorBody", e)
                                "Сервер ${e.code()}: ${errorBody ?: "Bad Request"}"
                            } else {
                                Log.e("CreateCourse", "POST /api/courses/ failed", e)
                                e.message
                            }
                            createFailureMessage = msg
                            Result.failure(e)
                        }

                        var result = doCreateCourse()

                        if (result.isFailure) {
                            val ex = result.exceptionOrNull()
                            if ((ex as? HttpException)?.code() == 401) {
                                val refresh = TokenStorage.refreshToken
                                if (refresh != null) {
                                    val newAccess = try {
                                        withContext(Dispatchers.IO) {
                                            ApiClient.api.refresh(RefreshRequest(refresh))
                                        }.access
                                    } catch (_: Throwable) { null }
                                    if (newAccess != null) {
                                        TokenStorage.saveTokens(context, newAccess, refresh)
                                        result = doCreateCourse()
                                    }
                                }
                            }
                        }

                        val courseResponse = result.getOrNull() ?: return false
                        val courseId = courseResponse.id
                        Log.i("CreateCourse", "Course ready, id=$courseId")

                        if (courseId < 0) {
                            Log.w("CreateCourse", "Course ID unknown (body truncated), skipping module upload")
                            return true
                        }

                        val validModules = materials.filter { it.title.isNotBlank() && it.fileUri.isNotBlank() }
                        for ((idx, m) in validModules.withIndex()) {
                            try {
                                val moduleFileUri = Uri.parse(m.fileUri)
                                val filePart = buildModuleFilePart(context, moduleFileUri, m.type)
                                withContext(Dispatchers.IO) {
                                    Log.i("CreateCourse", "POST module $idx: title=${m.title}, type=${m.type}")
                                    ApiClient.api.createModule(
                                        courseId = courseId,
                                        title = plainBody(m.title),
                                        description = plainBody(m.title),
                                        materialType = plainBody(m.type),
                                        file = filePart
                                    )
                                }
                                Log.i("CreateCourse", "Module $idx uploaded OK")
                            } catch (e: Throwable) {
                                val msg = if (e is HttpException) {
                                    val body = try { e.response()?.errorBody()?.string() } catch (_: Throwable) { null }
                                    "Модуль «${m.title}»: ${e.code()} $body"
                                } else e.message
                                Log.e("CreateCourse", "Module $idx upload failed: $msg", e)
                            }
                        }

                        return true
                    }

                    scope.launch {
                        val ok = try {
                            tryCreateCourse()
                        } catch (e: Throwable) {
                            isCreating = false
                            Log.e("CreateCourse", "create course exception", e)
                            createError = e.message ?: "Не удалось создать курс. Проверьте данные."
                            return@launch
                        }
                        isCreating = false
                        if (ok) navController.popBackStack()
                        else {
                            createError = createFailureMessage?.let { msg ->
                                if (msg.contains("401")) "Сессия истекла. Войдите заново."
                                else "Не удалось создать курс: $msg"
                            } ?: "Сессия истекла. Войдите заново и повторите."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006FFD)),
                enabled = !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(if (currentStep == 1) "Далее" else "Создать Курс", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(inner)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(4.dp))
            CreateCourseHeader(navController)
            Spacer(Modifier.height(10.dp))
            StepIndicator(currentStep = currentStep)
            Spacer(Modifier.height(12.dp))

            if (createError != null) {
                Text(createError!!, color = Color(0xFFD32F2F), fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
            }

            if (currentStep == 1) {
                Text("Базовая информация", fontWeight = FontWeight.Bold, fontSize = 28.sp / 2)
                Text("Важные детали о вашем курсе", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))

                FieldTitle("Заголовок курса*")
                AppTextField(title, { title = it }, "Спектр Аутизма")
                Spacer(Modifier.height(10.dp))

                FieldTitle("Описание курса*")
                AppTextField(description, { description = it }, "Опишите чему родители и дети могут научиться...", height = 110)
                Spacer(Modifier.height(10.dp))

                FieldTitle("Чему научатся пользователи?")
                AppTextField(outcomes, { outcomes = it }, "Опишите чему могут научиться пользователи", height = 90)
                Spacer(Modifier.height(10.dp))

                FieldTitle("Тэги")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tagChoices.forEach { tag ->
                        val selected = tag.value in selectedTags
                        Text(
                            text = tag.label,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (selected) Color(0xFF006FFD) else Color(0xFFE9F1FF))
                                .clickable {
                                    selectedTags =
                                        if (selected) selectedTags - tag.value else selectedTags + tag.value
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            color = if (selected) Color.White else Color(0xFF2D5D9F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                FieldTitle("Категория*")
                AppChoiceDropdown(categoryChoices, selectedCategory, "Выберите категорию") { selectedCategory = it }
                Spacer(Modifier.height(10.dp))

                FieldTitle("Уровень*")
                AppChoiceDropdown(levelChoices, selectedLevelValue, "Выберите уровень") { selectedLevelValue = it }
                Spacer(Modifier.height(10.dp))

                FieldTitle("Цена (₸)*")
                AppTextField(price, { price = it }, "99,999.9")
                Spacer(Modifier.height(10.dp))

                FieldTitle("Обложка*")
                val coverReady = selectedCoverUri != null
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (coverReady) Color(0xFFE8F5E9) else Color.White)
                        .then(Modifier.coverUploadDashedBorder(coverReady))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (!coverReady) {
                            "Нажмите чтобы загрузить изображение"
                        } else {
                            "Изображение выбрано"
                        },
                        color = if (coverReady) Color(0xFF1B5E20) else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = if (coverReady) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            } else {
                Text("Материалы курса", fontWeight = FontWeight.Bold, fontSize = 28.sp / 2)
                Text("Загрузите файлы вашего курса в нужном порядке", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MaterialButton("Добавить Артикль", R.drawable.ic_material_article) {
                        materials = materials + CourseMaterial(materials.size + 1, "article")
                    }
                    MaterialButton("Добавить PDF", R.drawable.ic_material_pdf) {
                        materials = materials + CourseMaterial(materials.size + 1, "pdf")
                    }
                }
                Spacer(Modifier.height(8.dp))
                MaterialButton("Добавить Видео", R.drawable.ic_material_video) {
                    materials = materials + CourseMaterial(materials.size + 1, "video")
                }

                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .dashedBorder()
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        materials.forEach { material ->
                            MaterialCard(
                                material = material,
                                onMaterialChange = { updated ->
                                    materials = materials.map { if (it.id == updated.id) updated else it }
                                },
                                onDelete = { materials = materials.filter { it.id != material.id } }
                            )
                        }
                        if (materials.isEmpty()) {
                            Text("Добавьте материалы", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun CreateCourseHeader(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Отмена", color = Color(0xFF006FFD), modifier = Modifier.clickable { navController.popBackStack() })
        Text("Создать Курс", fontWeight = FontWeight.Bold, fontSize = 20.sp / 1.3f)
        Spacer(Modifier.width(40.dp))
    }
}

@Composable
private fun StepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StepCircle(number = 1, active = currentStep == 1, done = currentStep > 1)
            Spacer(Modifier.height(4.dp))
            Text("Оформление курса", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (currentStep == 1) Color.Black else Color.Gray)
        }
        Spacer(Modifier.width(36.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StepCircle(number = 2, active = currentStep == 2, done = false)
            Spacer(Modifier.height(4.dp))
            Text("Загрузка материалов", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (currentStep == 2) Color.Black else Color.Gray)
        }
    }
}

@Composable
private fun StepCircle(number: Int, active: Boolean, done: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(
                when {
                    done -> Color(0xFFE3F2FD)
                    active -> Color(0xFF0A7AFF)
                    else -> Color(0xFFE0E0E0)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (done) {
            Icon(
                painter = painterResource(R.drawable.icon_done),
                contentDescription = null,
                tint = Color(0xFF0A7AFF),
                modifier = Modifier.size(12.dp)
            )
        } else {
            Text(number.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else Color.Gray)
        }
    }
}

@Composable
private fun FieldTitle(text: String) {
    Text(text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    Spacer(Modifier.height(4.dp))
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    height: Int = 48
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = height.dp),
        placeholder = { Text(placeholder, color = Color.Gray, fontSize = 13.sp) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF006FFD),
            unfocusedBorderColor = Color(0xFFD8DCE2),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
private fun AppChoiceDropdown(
    choices: List<ChoiceItem>,
    selectedValue: String,
    placeholder: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayLabel = choices.find { it.value == selectedValue }?.label ?: ""
    Box {
        OutlinedTextField(
            value = displayLabel,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder, color = Color.Gray, fontSize = 13.sp) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD8DCE2),
                unfocusedBorderColor = Color(0xFFD8DCE2),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f),
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            choices.forEach { ch ->
                DropdownMenuItem(
                    text = {
                        Text(
                            ch.label,
                            color = Color(0xFF222222),
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onSelect(ch.value)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = Color(0xFF222222)
                    )
                )
            }
        }
    }
}

@Composable
fun AppDropdown(
    items: List<String>,
    selectedItem: String,
    placeholder: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder, color = Color.Gray, fontSize = 13.sp) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD8DCE2),
                unfocusedBorderColor = Color(0xFFD8DCE2),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f),
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = Color(0xFF222222), fontSize = 14.sp) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(textColor = Color(0xFF222222))
                )
            }
        }
    }
}

@Composable
fun MaterialCard(
    material: CourseMaterial,
    onMaterialChange: (CourseMaterial) -> Unit,
    onDelete: () -> Unit
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val name = resolveUriFileName(ctx, uri)
            onMaterialChange(material.copy(fileUri = uri.toString(), displayName = name))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE1E4EA), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE4F2FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconForMaterialType(material.type)),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text("✕", color = Color(0xFFE35A68), fontSize = 18.sp, modifier = Modifier.clickable { onDelete() })
        }
        Spacer(Modifier.height(10.dp))
        AppTextField(material.title, { onMaterialChange(material.copy(title = it)) }, "Заголовок материала")
        Spacer(Modifier.height(8.dp))
        Box {
            OutlinedTextField(
                value = material.displayName.ifBlank { "Файл не выбран" },
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Выберите файл", color = Color.Gray, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF006FFD),
                    unfocusedBorderColor = Color(0xFFD8DCE2),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Gray,
                    unfocusedTextColor = Color.Gray
                )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val mimeTypes = when (material.type) {
                            "pdf" -> arrayOf("application/pdf")
                            "video" -> arrayOf("video/*")
                            else -> arrayOf(
                                "text/*",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "*/*"
                            )
                        }
                        filePickerLauncher.launch(mimeTypes)
                    }
            )
        }
    }
}

@Composable
fun MaterialButton(text: String, icon: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF2A8CEB)),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2A8CEB))
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun iconForMaterialType(type: String): Int = when (type) {
    "article" -> R.drawable.ic_material_article
    "pdf" -> R.drawable.ic_material_pdf
    "video" -> R.drawable.ic_material_video
    else -> R.drawable.ic_material_article
}

/** Парсит цену в тенге: пробелы, ₸, разделители тысяч/десятичные. */
private fun parseCoursePrice(raw: String): Double? {
    var s = raw.trim()
        .replace("₸", "")
        .replace("\u00A0", " ")
        .replace(" ", "")
    if (s.isBlank()) return null
    when {
        s.contains(',') && s.contains('.') -> {
            val lastComma = s.lastIndexOf(',')
            val lastDot = s.lastIndexOf('.')
            if (lastComma > lastDot) {
                s = s.replace(".", "").replace(',', '.')
            } else {
                s = s.replace(",", "")
            }
        }
        s.contains(',') -> {
            val parts = s.split(',')
            if (parts.size == 2 && parts[1].length <= 2 && parts[1].all { it.isDigit() }) {
                s = "${parts[0]}.${parts[1]}"
            } else {
                s = s.replace(",", "")
            }
        }
    }
    return s.toDoubleOrNull()
}

private fun plainBody(s: String) = s.toRequestBody("text/plain; charset=utf-8".toMediaType())

private fun jsonBody(json: String) = json.toRequestBody("application/json; charset=utf-8".toMediaType())

private fun buildModuleFilePart(context: android.content.Context, uri: Uri, materialType: String): MultipartBody.Part {
    val cr = context.contentResolver
    val mime = cr.getType(uri) ?: when (materialType) {
        "pdf" -> "application/pdf"
        "video" -> "video/mp4"
        else -> "application/octet-stream"
    }
    val bytes = cr.openInputStream(uri)?.use { it.readBytes() } ?: error("Не удалось прочитать файл модуля")
    val name = resolveUriFileName(context, uri)
    val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("file", name, body)
}

private fun buildPreviewImagePart(context: android.content.Context, uri: Uri): MultipartBody.Part {
    val cr = context.contentResolver
    val mime = cr.getType(uri) ?: "image/jpeg"
    val bytes = cr.openInputStream(uri)?.use { it.readBytes() } ?: error("Не удалось прочитать файл")
    val name = resolveUriFileName(context, uri)
    val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("preview_image", name, body)
}

private fun resolveUriFileName(context: android.content.Context, uri: Uri): String {
    context.contentResolver.query(uri, null, null, null, null)?.use { c ->
        if (c.moveToFirst()) {
            val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (i >= 0) return c.getString(i) ?: "preview.jpg"
        }
    }
    return uri.lastPathSegment ?: "preview.jpg"
}

private fun defaultCategoryChoices() = listOf(
    ChoiceItem("autism", "Аутизм"),
    ChoiceItem("speech_therapy", "Логопедия"),
    ChoiceItem("adhd", "СДВГ"),
    ChoiceItem("sensory_processing", "Сенсорная")
)

private fun defaultLevelChoices() = listOf(
    ChoiceItem("beginner", "Начинающий"),
    ChoiceItem("intermediate", "Средний"),
    ChoiceItem("advanced", "Продвинутый")
)

private fun defaultTagChoices() = listOf(
    ChoiceItem("to_parents", "РОДИТЕЛЯМ"),
    ChoiceItem("self_regulation", "САМОРЕГУЛЯЦИЯ"),
    ChoiceItem("logical_thinking", "ЛОГИЧЕСКОЕ МЫШЛЕНИЕ"),
    ChoiceItem("learning_through_play", "ОБУЧЕНИЕ ЧЕРЕЗ ИГРУ"),
    ChoiceItem("for_children", "ДЕТЯМ"),
    ChoiceItem("easy_start", "ЛЕГКИЙ СТАРТ"),
    ChoiceItem("speech_therapy_work", "ЛОГОПЕДИЧЕСКАЯ РАБОТА"),
    ChoiceItem("social_skills_start", "СОЦИАЛЬНЫЕ НАВЫКИ СТАРТ"),
    ChoiceItem("with_parent", "С УЧАСТИЕМ РОДИТЕЛЯ"),
    ChoiceItem("speech_understanding", "ПОНИМАНИЕ РЕЧИ"),
    ChoiceItem("gradual_development", "ПОСТЕПЕННОЕ РАЗВИТИЕ"),
    ChoiceItem("memory", "ПАМЯТЬ"),
    ChoiceItem("intensive_course", "ИНТЕНСИВНЫЙ КУРС")
)

private fun Modifier.dashedBorder(): Modifier = drawBehind {
    val stroke = Stroke(
        width = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    )
    drawRoundRect(
        color = Color(0xFFD1D5DB),
        style = stroke,
        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
    )
}

/** Пунктир обложки: после выбора файла — зелёная рамка и заметнее штрих. */
private fun Modifier.coverUploadDashedBorder(selected: Boolean): Modifier = drawBehind {
    val r = 12.dp.toPx()
    val stroke = Stroke(
        width = if (selected) 2.dp.toPx() else 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    )
    drawRoundRect(
        color = if (selected) Color(0xFF43A047) else Color(0xFFD1D5DB),
        style = stroke,
        cornerRadius = CornerRadius(r, r)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCreateCoursePage() {
    CreateCoursePage(navController = rememberNavController())
}
