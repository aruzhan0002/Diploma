package com.example.diploma

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class CourseMaterial(
    val id: Int,
    val type: String,   // article / pdf / video
    var title: String = "",
    var fileName: String = ""
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCoursePage(navController: NavController) {

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var materials by remember { mutableStateOf(listOf<CourseMaterial>()) }

    val categories = listOf(
        "Autism","Speech Therapy","ADHD","Sensory Processing",
        "Social Development","Physical Therapy",
        "Behavioral Support","Learning Disabilities"
    )

    val levels = listOf("Начинающий","Средний","Продвинутый")

    var selectedCategory by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("") }

    var catExpanded by remember { mutableStateOf(false) }
    var levelExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,

        bottomBar = {

            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank() && price.isNotBlank()) {
                        val formattedPrice = if (price.trim().endsWith("₸")) {
                            price.trim()
                        } else {
                            "${price.trim()} ₸"
                        }
                        CourseRepository.addCourse(
                            title = title.trim(),
                            description = description.trim(),
                            price = formattedPrice
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                    .fillMaxWidth()
                    .height(48.dp),

                shape = RoundedCornerShape(12.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006FFD)
                )

            ) {
                Text(
                    "Создать Курс",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }

    ) { inner ->

        Column(
            modifier = Modifier
                .padding(inner)
                .padding(top = 24.dp, start = 24.dp, end =24.dp)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())

        ) {

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    "Отмена",
                    color = Color(0xFF006FFD),
                    modifier = Modifier.clickable { navController.popBackStack() }
                )

                Text(
                    "Создать Курс",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.width(40.dp))
            }

            Spacer(Modifier.height(35.dp))

            Text(
                "Базовая информация",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.padding(8.dp))

            Text(
                "Важные детали о вашем курсе",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(30.dp))

            Text("Заголовок курса*",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp)
            Spacer(modifier = Modifier.padding(3.dp))

            AppTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Спектры Аутизма",


            )

            Spacer(Modifier.height(16.dp))

            Text("Описание*",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp)
            Spacer(modifier = Modifier.padding(3.dp))

            AppTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Опишите чему родители и дети могут научиться...",
                height = 120
            )

            Spacer(Modifier.height(16.dp))

            Text("Категория*",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp)

            Spacer(Modifier.height(4.dp))

            AppDropdown(
                items = categories,
                selectedItem = selectedCategory,
                placeholder = "Выберите категорию",
                onItemSelected = { selectedCategory = it }
            )

            Spacer(Modifier.height(16.dp))

            Text("Уровень*",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp)

            Spacer(Modifier.height(4.dp))

            AppDropdown(
                items = levels,
                selectedItem = selectedLevel,
                placeholder = "Выберите уровень",
                onItemSelected = { selectedLevel = it }
            )
            Spacer(Modifier.height(16.dp))

            Text("Цена (₸)*",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp)
            Spacer(modifier = Modifier.padding(3.dp))

            AppTextField(
                value = price,
                onValueChange = { price = it },
                placeholder = "99,999.9"
            )

            Spacer(Modifier.height(16.dp))

            Text("Продолжительность (часы)*",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp)
            Spacer(modifier = Modifier.padding(3.dp))

            AppTextField(
                value = duration,
                onValueChange = { duration = it },
                placeholder = "8"
            )

            Spacer(Modifier.height(20.dp))

            Text("Обложка*",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp)
            Spacer(modifier = Modifier.padding(3.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .border(
                        1.dp,
                        Color(0xFFE5E7EB),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Нажмите чтобы загрузить изображение",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp)
                Spacer(modifier = Modifier.padding(3.dp))
            }

            Spacer(Modifier.height(80.dp))

            Text(
                "Материалы курса",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                "Загрузите файлы вашего курса в нужном порядке",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                MaterialButton(
                    text = "Добавить Артикль",
                    icon = R.drawable.icon_camers
                ) {
                    materials = materials + CourseMaterial(
                        id = materials.size + 1,
                        type = "article"
                    )
                }

                MaterialButton(
                    text = "Добавить PDF",
                    icon = R.drawable.icon_camers
                ) {
                    materials = materials + CourseMaterial(
                        id = materials.size + 1,
                        type = "pdf"
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            MaterialButton(
                text = "Добавить Видео",
                icon = R.drawable.icon_camers
            ) {
                materials = materials + CourseMaterial(
                    id = materials.size + 1,
                    type = "video"
                )
            }

            Spacer(Modifier.height(20.dp))

            if (materials.isNotEmpty()) {

                MaterialsContainer(
                    materials = materials,
                    onDelete = {
                        materials = materials.filter { m -> m.id != it.id }
                    }
                )

            }


        }
    }
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

        placeholder = {
            Text(
                placeholder,
                color = Color.Gray,
                fontSize = 12.sp
            )
        },

        shape = RoundedCornerShape(12.dp),

        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF006FFD),
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )


}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDropdown(
    items: List<String>,
    selectedItem: String,
    placeholder: String,
    onItemSelected: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,

            placeholder = {
                Text(
                    placeholder,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            },

            trailingIcon = {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },

            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .heightIn(min = 48.dp),

            shape = RoundedCornerShape(12.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF006FFD),
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            items.forEach { item ->

                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
@Composable
fun MaterialCard(
    material: CourseMaterial,
    onDelete: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFF7F8FB),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color(0xFFDCE6F5),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        painter = painterResource(R.drawable.icon_camers),
                        contentDescription = null,
                        tint = Color(0xFF006FFD),
                        modifier = Modifier.size(18.dp)
                    )
                }

            }

            Icon(
                painter = painterResource(R.drawable.icon_close),
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onDelete() }
            )
        }

        Spacer(Modifier.height(12.dp))

        AppTextField(
            value = material.title,
            onValueChange = { material.title = it },
            placeholder = "Заголовок материала"
        )

        Spacer(Modifier.height(8.dp))

        AppTextField(
            value = material.fileName,
            onValueChange = { material.fileName = it },
            placeholder = "Выберите файл  Файл не выбран"
        )
    }
}
@Composable
fun MaterialButton(
    text: String,
    icon: Int,
    onClick: () -> Unit
) {

    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF006FFD)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF006FFD)
        )
    ) {

        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = text,
            fontSize = 12.sp
        )
    }
}
@Composable
fun MaterialsContainer(
    materials: List<CourseMaterial>,
    onDelete: (CourseMaterial) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    1.dp,
                    Color(0xFFD1D5DB)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {

        materials.forEach { material ->

            MaterialCard(
                material = material,
                onDelete = { onDelete(material) }
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}





@Preview(showBackground = true)
@Composable
fun PreviewCreateCoursePage() {
    CreateCoursePage(navController = rememberNavController())
}
