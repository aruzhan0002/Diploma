package kz.aruzhan.care_steps

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kz.aruzhan.care_steps.R
import kz.aruzhan.care_steps.ui.auth.AuthViewModel
import kz.aruzhan.care_steps.ui.auth.AuthViewModelFactory

@Composable
fun StepProgressBar(currentStep: Int) {
    Row(
        modifier = Modifier
            .padding(top = 50.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 1..9) {
            Box(
                modifier = Modifier
                    .width(34.8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        color = if (i <= currentStep) Color(0xFF006FFD) else Color(0xFFE8E9F1)
                    )
            )
        }
    }
}

@Composable
fun CreateAccountFirstPage(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isChecked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val blue = Color(0xFF006FFD)
    val context = LocalContext.current
    val authVm: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context.applicationContext as Application)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // СКРОЛЛЯЩИЙСЯ КОНТЕНТ
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StepProgressBar(currentStep = 1)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Создайте аккаунт",
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .width(327.dp)
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Сообщение об ошибке
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .width(327.dp)
                            .padding(vertical = 8.dp)
                    )
                }

                // Ваша почта
                Text(
                    text = "Ваша почта",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                    modifier = Modifier
                        .width(327.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    placeholder = { Text("name@email.com") },
                    modifier = Modifier
                        .width(327.dp)
                        .defaultMinSize(minHeight = 56.dp)
                        .border(1.dp, Color(0xFFC5C6CC), RoundedCornerShape(12.dp)),
                    textStyle = TextStyle(fontSize = 14.sp),
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Создайте пароль
                Text(
                    text = "Создайте пароль",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                    modifier = Modifier
                        .width(327.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    placeholder = { Text("Пароль") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            enabled = !isLoading
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (passwordVisible) R.drawable.ic_password_show else R.drawable.ic_password_hide
                                ),
                                contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                                modifier = Modifier.size(20.dp),
                                tint = Color.Unspecified
                            )
                        }
                    },
                    modifier = Modifier
                        .width(327.dp)
                        .defaultMinSize(minHeight = 56.dp)
                        .border(1.dp, Color(0xFFC5C6CC), RoundedCornerShape(12.dp)),
                    textStyle = TextStyle(fontSize = 14.sp),
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Подтвердите пароль
                Text(
                    text = "Подтвердите пароль",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                    modifier = Modifier
                        .width(327.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    placeholder = { Text("Подтвердите пароль") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { confirmPasswordVisible = !confirmPasswordVisible },
                            enabled = !isLoading
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (confirmPasswordVisible) R.drawable.ic_password_show else R.drawable.ic_password_hide
                                ),
                                contentDescription = if (confirmPasswordVisible) "Скрыть пароль" else "Показать пароль",
                                modifier = Modifier.size(20.dp),
                                tint = Color.Unspecified
                            )
                        }
                    },
                    modifier = Modifier
                        .width(327.dp)
                        .defaultMinSize(minHeight = 56.dp)
                        .border(1.dp, Color(0xFFC5C6CC), RoundedCornerShape(12.dp)),
                    textStyle = TextStyle(fontSize = 14.sp),
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // чекбокс
                Row(
                    modifier = Modifier.width(327.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it },
                        enabled = !isLoading
                    )
                    Text(
                        text = "Я ознакомился(-ась) с ТП Правами и Условиями и Политикой безопасности.",
                        style = TextStyle(fontSize = 14.sp),
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // КНОПКИ — ВСЕГДА СНИЗУ
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
                        if (password == confirmPassword) {
                            if (isChecked) {
                                isLoading = true
                                errorMessage = null

                                authVm.register(
                                    email = email,
                                    password = password,
                                    passwordConfirm = confirmPassword,
                                    onSuccess = {
                                        isLoading = false
                                        println("✅ Успех! Переходим на второй экран")
                                        navController.navigate("AccountCreateSecondPage") {
                                        }
                                    },
                                    onError = { error ->
                                        isLoading = false
                                        errorMessage = error
                                        println("❌ Ошибка: $error")
                                    }
                                )
                            } else {
                                errorMessage = "Необходимо согласиться с условиями"
                            }
                        } else {
                            errorMessage = "Пароли не совпадают"
                        }
                    },
                    modifier = Modifier
                        .width(327.dp)
                        .height(45.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = blue,
                        disabledContainerColor = blue.copy(alpha = 0.5f)
                    ),
                    enabled = !isLoading &&
                            email.isNotBlank() &&
                            password.isNotBlank() &&
                            confirmPassword.isNotBlank() &&
                            isChecked
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(text = "Далее", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .width(327.dp)
                        .height(45.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, blue),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.5f)
                    ),
                    enabled = !isLoading
                ) {
                    Text(text = "Назад", color = blue)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCreateAccountFirstPage() {
    CreateAccountFirstPage(navController = rememberNavController())
}