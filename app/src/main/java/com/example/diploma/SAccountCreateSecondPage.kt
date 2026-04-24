package kz.aruzhan.care_steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kz.aruzhan.care_steps.ui.auth.SpecialistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SAccountCreateSecondPage(
    navController: NavController,
    specialistVm: SpecialistViewModel
) {
    val blue = Color(0xFF006FFD)
    val border = Color(0xFFC5C6CC)

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            StepProgressBar1(currentStep = 2)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Коротко о вас",
                style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.width(327.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                text = "Имя и Фамилия",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.width(327.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = specialistVm.fullName,
                onValueChange = {
                    specialistVm.fullName = it
                    errorMessage = null
                },
                placeholder = { Text("Павел Калашенко", color = Color(0xFF9AA0A6)) },
                modifier = Modifier
                    .width(327.dp)
                    .border(1.dp, border, RoundedCornerShape(12.dp)),
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
                    disabledIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Расскажите о своём подходе (3–5 предложений)",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.width(327.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = specialistVm.approachDescription,
                onValueChange = {
                    specialistVm.approachDescription = it
                    errorMessage = null
                },
                placeholder = { Text("О вас…", color = Color(0xFF9AA0A6)) },
                modifier = Modifier
                    .width(327.dp)
                    .height(140.dp)
                    .border(1.dp, border, RoundedCornerShape(16.dp)),
                textStyle = TextStyle(fontSize = 16.sp),
                singleLine = false,
                maxLines = 6,
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.5f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    specialistVm.createSpecialistProfile(
                        onSuccess = {
                            isLoading = false
                            navController.navigate("SAccountCreateThirdPage")
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
                        specialistVm.fullName.isNotBlank() &&
                        specialistVm.approachDescription.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Далее", color = Color.White)
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
                Text("Назад", color = blue)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
