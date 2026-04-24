package com.example.diploma

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.diploma.R
import com.example.diploma.ui.auth.SpecialistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SAccountCreateFirstPage(
    navController: NavController,
    specialistVm: SpecialistViewModel
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isChecked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val blue = Color(0xFF006FFD)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StepProgressBar1(currentStep = 1)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Создайте аккаунт",
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .width(327.dp)
                        .height(40.dp)
                )

                Spacer(modifier = Modifier.height(15.dp))

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

                Text(
                    text = "Ваша почта",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                    modifier = Modifier
                        .width(327.dp)
                        .height(20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = specialistVm.email,
                    onValueChange = {
                        specialistVm.email = it
                        errorMessage = null
                    },
                    label = { Text("name@email.com") },
                    modifier = Modifier
                        .width(327.dp)
                        .defaultMinSize(minHeight = 56.dp)
                        .border(1.dp, Color(0xFFC5C6CC), RoundedCornerShape(12.dp)),
                    textStyle = TextStyle(fontSize = 16.sp),
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

                Text(
                    text = "Создайте пароль",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                    modifier = Modifier
                        .width(327.dp)
                        .height(20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = specialistVm.password,
                    onValueChange = {
                        specialistVm.password = it
                        errorMessage = null
                    },
                    label = { Text("Пароль") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
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
                    textStyle = TextStyle(fontSize = 16.sp),
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

                Text(
                    text = "Подтвердите пароль",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                    modifier = Modifier
                        .width(327.dp)
                        .height(20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = specialistVm.confirmPassword,
                    onValueChange = {
                        specialistVm.confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Подтвердите пароль") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
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
                    textStyle = TextStyle(fontSize = 16.sp),
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
                        if (specialistVm.password != specialistVm.confirmPassword) {
                            errorMessage = "Пароли не совпадают"
                            return@Button
                        }
                        if (!isChecked) {
                            errorMessage = "Необходимо согласиться с условиями"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        specialistVm.register(
                            onSuccess = {
                                isLoading = false
                                navController.navigate("SAccountCreateSecondPage")
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        )
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
                            specialistVm.email.isNotBlank() &&
                            specialistVm.password.isNotBlank() &&
                            specialistVm.confirmPassword.isNotBlank() &&
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
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    enabled = !isLoading
                ) {
                    Text(text = "Назад", color = blue)
                }
            }
        }
    }
}

@Composable
fun StepProgressBar1(currentStep: Int) {
    Row(
        modifier = Modifier
            .padding(top = 50.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 1..7) {
            Box(
                modifier = Modifier
                    .width(43.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        color = if (i <= currentStep) Color.Blue else Color(0xFFE8E9F1)
                    )
            )
        }
    }
}
