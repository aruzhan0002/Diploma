import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.diploma.R
import com.example.diploma.ui.auth.AuthViewModel
import com.example.diploma.ui.auth.AuthViewModelFactory

@Composable
fun LoginScreen(navController: NavController, role: String = "specialist") {
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordText by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val authVm: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context.applicationContext as Application)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (role == "specialist") {
            TextButton(
                onClick = { navController.navigate("SettingsPageSpecialist") },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 4.dp, end = 8.dp)
            ) {
                Text("Настройки", style = TextStyle(fontSize = 16.sp, color = Color(0xFF006FFD)))
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Image(
            painter = painterResource(id = R.drawable.img2),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(315.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(
            modifier = Modifier.padding(20.dp)
        )
        Text(
            text = "С возвращением",
            style = TextStyle(
                fontSize = 30.sp,
                color = Color.Black,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            TextField(
                value = emailText,
                onValueChange = {
                    emailText = it
                    errorMessage = null
                },
                label = { Text("Почта") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(50.dp)
                    .border(1.dp, Color(0xC5C6CC), shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                textStyle = TextStyle(fontSize = 16.sp),
                singleLine = true,
                enabled = !isLoading
            )

            TextField(
                value = passwordText,
                onValueChange = {
                    passwordText = it
                    errorMessage = null
                },
                label = { Text("Пароль") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, Color(0xC5C6CC), shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                textStyle = TextStyle(fontSize = 16.sp),
                singleLine = true,
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
                enabled = !isLoading
            )

            TextButton(
                onClick = { navController.navigate("ForgotPasswordEmailPage/$role") },
            ) {
                Text(
                    text = "Забыли пароль?",
                    style = TextStyle(fontSize = 16.sp, color = Color(0xFF006FFD)),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } 

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    if (emailText.isBlank() || passwordText.isBlank()) {
                        errorMessage = "Введите логин и пароль"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    authVm.login(
                        email = emailText.trim(),
                        password = passwordText,
                        onSuccess = { userType ->
                            isLoading = false
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
                        onError = { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                },
                modifier = Modifier
                    .width(327.dp)
                    .height(45.dp)
                    .border(5.dp, Color(0xFF006FFD), shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006FFD),
                    disabledContainerColor = Color(0xFF006FFD)
                ),
                enabled = true
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(text = "Войти", color = Color.White)
                }
            }
            Divider(
                color = Color.Gray.copy(alpha = 0.4f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "или",
                style = TextStyle(fontSize = 16.sp, color = Color.Gray),
                modifier = Modifier.padding(vertical = 5.dp).align(Alignment.CenterHorizontally)
            )

            Button(
                onClick = { navController.popBackStack()},
                modifier = Modifier
                    .width(327.dp)
                    .height(45.dp)
                    .border(1.dp, Color(0xFF006FFD), shape = RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                enabled = !isLoading
            ) {
                Text(text = "Зарегистрироваться", color = Color(0xFF006FFD))
            }
        }
        }
    }
}
