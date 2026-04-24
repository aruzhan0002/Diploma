package com.example.diploma

import android.app.Application
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.diploma.domain.model.ParentRelationship
import com.example.diploma.ui.auth.AuthViewModel
import com.example.diploma.ui.auth.AuthViewModelFactory

@Composable
fun RelationshipSelector(
    selectedTitle: String,                 // "Мама" / "Папа" / "Опекун" / "Другое"
    otherText: String,
    onSelectTitle: (String) -> Unit,        // обновляем строку для UI
    onSelectRelationship: (ParentRelationship) -> Unit, // обновляем enum для API
    onOtherChange: (String) -> Unit
) {
    val borderColor = Color(0xFFC5C6CC)
    val selectedBg = Color(0xFFEAF2FF)
    val radius = RoundedCornerShape(16.dp)

    Column(modifier = Modifier.width(327.dp)) {

        Text(
            text = "Кем вы приходитесь ребенку?",
            style = TextStyle(fontSize = 16.sp)
        )
        Spacer(Modifier.height(12.dp))

        // ✅ 3 варианта
        listOf("Мама", "Папа", "Опекун").forEach { title ->
            SelectOptionCard(
                title = title,
                selected = selectedTitle == title,
                borderColor = borderColor,
                selectedBg = selectedBg,
                shape = radius,
                onClick = {
                    onSelectTitle(title)
                    onSelectRelationship(
                        when (title) {
                            "Мама" -> ParentRelationship.MOM
                            "Папа" -> ParentRelationship.DAD
                            "Опекун" -> ParentRelationship.GUARDIAN
                            else -> ParentRelationship.MOM
                        }
                    )
                }
            )
            Spacer(Modifier.height(12.dp))
        }

        // ✅ "Другое"
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            shape = radius,
            color = Color.White,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Box(Modifier.fillMaxSize()) {

                OutlinedTextField(
                    value = otherText,
                    onValueChange = {
                        onOtherChange(it)
                        // как только печатаем — считаем, что выбран OTHER
                        onSelectTitle("Другое")
                        onSelectRelationship(ParentRelationship.OTHER)
                    },
                    placeholder = { Text("Другое", color = Color(0xFF9AA0A6)) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp),
                    maxLines = 5,
                    textStyle = TextStyle(fontSize = 16.sp),
                    shape = radius,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                if (selectedTitle == "Другое") {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color(0xFF006FFD),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectOptionCard(
    title: String,
    selected: Boolean,
    borderColor: Color,
    selectedBg: Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { onClick() },
        shape = shape,
        color = if (selected) selectedBg else Color.White,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier.weight(1f)
            )

            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFF006FFD)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountCreateSecondPage(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }
    var profileError by remember { mutableStateOf<String?>(null) }

    // ✅ UI-строка (чтобы подсветка работала как раньше)
    var relationshipTitle by remember { mutableStateOf("Мама") }

    // ✅ enum для API (это главное!)
    var selectedRelationship by remember { mutableStateOf(ParentRelationship.MOM) }

    var otherText by remember { mutableStateOf("") }

    val blue = Color(0xFF006FFD)
    val context = LocalContext.current
    val authVm: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context.applicationContext as Application)
    )



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // ✅ СКРОЛЛЯЩИЙСЯ КОНТЕНТ
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                // важно: место снизу под кнопки
                .padding(bottom = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            StepProgressBar(currentStep = 2)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Коротко о вас",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .width(327.dp)
                    .height(30.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Ваше имя и фамилия",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                modifier = Modifier
                    .width(327.dp)
                    .height(20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Вероника Петрова") },
                modifier = Modifier
                    .width(327.dp)
                    .height(48.dp)
                    .border(1.dp, Color(0xFFC5C6CC), RoundedCornerShape(12.dp)),
                textStyle = TextStyle(fontSize = 16.sp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(35.dp))

            RelationshipSelector(
                selectedTitle = relationshipTitle,
                otherText = otherText,
                onSelectTitle = { relationshipTitle = it },
                onSelectRelationship = { selectedRelationship = it },
                onOtherChange = { otherText = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.width(327.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it }
                )
                Text(
                    text = "Я ознакомился(-ась) с ТП Правами и Условиями и Политикой безопасности.",
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ✅ КНОПКИ — ВСЕГДА СНИЗУ (НЕ ПРОПАДАЮТ)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    profileError = null
                    authVm.createProfile(
                        fullName = name,
                        relationship = selectedRelationship, // enum
                        otherText = otherText,
                        onSuccess = {
                            navController.navigate("AccountCreateThirdPage")
                        },
                        onError = { error ->
                            profileError = error
                            println("PROFILE ERROR: $error")
                        }
                    )
                },
                modifier = Modifier
                    .width(327.dp)
                    .height(45.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blue)
            ) {
                Text(text = "Далее", color = Color.White)
            }

            if (profileError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = profileError ?: "",
                    color = Color(0xFFD64045),
                    style = TextStyle(fontSize = 13.sp),
                    modifier = Modifier.width(327.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .width(327.dp)
                    .height(45.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, blue),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Text(text = "Назад", color = blue)
            }
        }
    }
}






@Preview(showBackground = true)
@Composable
fun PreviewAccountCreateSecondPage() {
    AccountCreateSecondPage(navController = rememberNavController())
}
