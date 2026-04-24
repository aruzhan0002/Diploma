import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kz.aruzhan.care_steps.R
import kz.aruzhan.care_steps.ui.auth.AuthViewModel
import kz.aruzhan.care_steps.ui.auth.AuthViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordEmailPage(navController: NavController, role: String) {
    var email by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val authVm: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context.applicationContext as Application)
    )

    ForgotPasswordScreenContainer {
        Text(
            text = "Восстановление пароля",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2024)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Введите почту, указанную при регистрации. Мы отправим код подтверждения",
            color = Color(0xFF8E8E93),
            fontSize = 18.sp,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "Почта",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1F2024)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorText = null
            },
            singleLine = true,
            placeholder = { Text("email@example.com") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF006FFD),
                unfocusedBorderColor = Color(0xFFC8CBD3),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorText != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = errorText!!, color = Color(0xFFD64045), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.weight(1f))
        AuthPrimaryButton(text = "Получить код", isLoading = isLoading) {
            val normalizedEmail = email.trim()
            if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
                errorText = "Введите корректную почту"
                return@AuthPrimaryButton
            }
            isLoading = true
            authVm.requestPasswordResetCode(
                email = normalizedEmail,
                onSuccess = { retryAfter ->
                    isLoading = false
                    val timer = retryAfter ?: 14
                    navController.navigate(
                        "ForgotPasswordCodePage/$role?email=${Uri.encode(normalizedEmail)}&retryAfter=$timer"
                    )
                },
                onError = { message ->
                    isLoading = false
                    errorText = message
                }
            )
        }
    }
}

@Composable
fun ForgotPasswordCodePage(
    navController: NavController,
    role: String,
    email: String,
    initialRetryAfter: Int
) {
    var code by remember { mutableStateOf("") }
    var secondsLeft by remember { mutableIntStateOf(initialRetryAfter.coerceAtLeast(0)) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val authVm: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context.applicationContext as Application)
    )

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
    }

    ForgotPasswordScreenContainer {
        Text(
            text = "Подтверждение почты",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2024)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Мы отправили 4-значный код на почту\n$email",
            color = Color(0xFF8E8E93),
            fontSize = 18.sp,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(34.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        ) {
            BasicTextField(
                value = code,
                onValueChange = {
                    val onlyDigits = it.filter { ch -> ch.isDigit() }.take(4)
                    code = onlyDigits
                    errorText = null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                textStyle = TextStyle(color = Color.Transparent),
                modifier = Modifier
                    .size(1.dp)
                    .align(Alignment.TopStart)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(4) { index ->
                    val currentSymbol = code.getOrNull(index)?.toString().orEmpty()
                    val isActive = index == code.length && code.length < 4
                    Box(
                        modifier = Modifier
                            .size(width = 62.dp, height = 72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.Transparent)
                            .border(
                                width = 2.dp,
                                color = if (isActive) Color(0xFF006FFD) else Color(0xFFC8CBD3),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { focusRequester.requestFocus() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentSymbol,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1F2024)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
        if (secondsLeft > 0) {
            Text(
                text = "Отправить код повторно через 0:${secondsLeft.toString().padStart(2, '0')}",
                color = Color(0xFF8E8E93),
                fontSize = 16.sp
            )
        } else {
            Text(
                text = "Отправить код ещё раз",
                color = Color(0xFF006FFD),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    if (!isLoading) {
                        isLoading = true
                        authVm.requestPasswordResetCode(
                            email = email,
                            onSuccess = { retryAfter ->
                                isLoading = false
                                secondsLeft = (retryAfter ?: 14).coerceAtLeast(0)
                                code = ""
                                errorText = null
                                focusRequester.requestFocus()
                            },
                            onError = { message ->
                                isLoading = false
                                errorText = message
                            }
                        )
                    }
                }
            )
        }

        if (errorText != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = errorText!!, color = Color(0xFFD64045), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.weight(1f))
        AuthPrimaryButton(text = "Подтвердить", isLoading = isLoading) {
            if (code.length != 4) return@AuthPrimaryButton
            isLoading = true
            authVm.verifyPasswordResetCode(
                email = email,
                code = code,
                onSuccess = { resetToken ->
                    isLoading = false
                    navController.navigate(
                        "ForgotPasswordNewPasswordPage/$role?email=${Uri.encode(email)}&resetToken=${Uri.encode(resetToken)}"
                    )
                },
                onError = { message ->
                    isLoading = false
                    errorText = message
                }
            )
        }
    }
}

@Composable
fun ForgotPasswordNewPasswordPage(
    navController: NavController,
    role: String,
    email: String,
    resetToken: String
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val authVm: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context.applicationContext as Application)
    )

    ForgotPasswordScreenContainer {
        Text(
            text = "Придумайте пароль",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2024)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Пароль должен быть не менее 8 символов и содержать цифры",
            color = Color(0xFF8E8E93),
            fontSize = 18.sp,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Новый пароль",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1F2024)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                errorText = null
            },
            singleLine = true,
            placeholder = { Text("Введите пароль") },
            visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { newVisible = !newVisible }) {
                    Icon(
                        painter = painterResource(id = if (newVisible) R.drawable.ic_password_show else R.drawable.ic_password_hide),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF006FFD),
                unfocusedBorderColor = Color(0xFFC8CBD3),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorText = null
            },
            singleLine = true,
            placeholder = { Text("Повторите пароль") },
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                    Icon(
                        painter = painterResource(id = if (confirmVisible) R.drawable.ic_password_show else R.drawable.ic_password_hide),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF006FFD),
                unfocusedBorderColor = Color(0xFFC8CBD3),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorText != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = errorText!!, color = Color(0xFFD64045), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.weight(1f))
        AuthPrimaryButton(text = "Сохранить и войти", isLoading = isLoading) {
            val hasMinLength = newPassword.length >= 8
            val hasDigit = newPassword.any { it.isDigit() }
            when {
                !hasMinLength || !hasDigit -> errorText = "Пароль должен быть не менее 8 символов и содержать цифры"
                newPassword != confirmPassword -> errorText = "Пароли не совпадают"
                else -> {
                    isLoading = true
                    authVm.confirmPasswordReset(
                        email = email,
                        resetToken = resetToken,
                        newPassword = newPassword,
                        onSuccess = { userType ->
                            isLoading = false
                            Toast.makeText(context, "Пароль успешно обновлен", Toast.LENGTH_SHORT).show()
                            val destination = when (userType?.lowercase()) {
                                "specialist" -> "SettingsPageSpecialist"
                                "parent" -> "SettingsPage"
                                "both" -> if (role == "specialist") "SettingsPageSpecialist" else "SettingsPage"
                                "none" -> if (role == "specialist") "SettingsPageSpecialist" else "SettingsPage"
                                else -> if (role == "specialist") "SettingsPageSpecialist" else "SettingsPage"
                            }
                            navController.navigate(destination) {
                                popUpTo("startScreen") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onError = { message ->
                            isLoading = false
                            errorText = message
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ForgotPasswordScreenContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F5F7))
            .padding(horizontal = 24.dp, vertical = 52.dp)
            .imePadding(),
        horizontalAlignment = Alignment.Start,
        content = content
    )
}

@Composable
private fun AuthPrimaryButton(
    text: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006FFD)),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}
