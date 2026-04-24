package kz.aruzhan.care_steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kz.aruzhan.care_steps.ui.auth.SpecialistViewModel

@Composable
fun SAccountCreateSeventhPage(
    navController: NavController,
    specialistVm: SpecialistViewModel
) {
    val blue = Color(0xFF006FFD)
    val border = Color(0xFFC5C6CC)

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                StepProgressBar1(currentStep = 7)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Как вы хотите работать\nв приложении?",
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))
            }

            item {
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

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Переключатели",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                SwitchCard(
                    text = "Оказывать индивидуальные\nконсультации",
                    checked = specialistVm.provideConsultations,
                    onCheckedChange = { specialistVm.provideConsultations = it },
                    width = 327.dp,
                    borderColor = border,
                    blue = blue
                )

                Spacer(modifier = Modifier.height(12.dp))

                SwitchCard(
                    text = "Работать с ребёнком через\nродителя",
                    checked = specialistVm.workViaParent,
                    onCheckedChange = { specialistVm.workViaParent = it },
                    width = 327.dp,
                    borderColor = border,
                    blue = blue
                )

                Spacer(modifier = Modifier.height(12.dp))

                SwitchCard(
                    text = "Давать рекомендации и планы\nзанятий",
                    checked = specialistVm.provideRecommendations,
                    onCheckedChange = { specialistVm.provideRecommendations = it },
                    width = 327.dp,
                    borderColor = border,
                    blue = blue
                )

                Spacer(modifier = Modifier.height(12.dp))

                SwitchCard(
                    text = "Вести прогресс и аналитику",
                    checked = specialistVm.trackAnalytics,
                    onCheckedChange = { specialistVm.trackAnalytics = it },
                    width = 327.dp,
                    borderColor = border,
                    blue = blue
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    specialistVm.submitSpecialistDescription(
                        onSuccess = {
                            isLoading = false
                            navController.navigate("SettingsPageSpecialist") {
                                popUpTo("startScreen") { inclusive = false }
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
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = blue,
                    disabledContainerColor = blue.copy(alpha = 0.5f)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Перейти в приложение", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .width(327.dp)
                    .height(45.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, blue),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                enabled = !isLoading
            ) {
                Text("Назад", color = blue)
            }
        }
    }
}

@Composable
private fun SwitchCard(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    width: androidx.compose.ui.unit.Dp,
    borderColor: Color,
    blue: Color
) {
    Row(
        modifier = Modifier
            .width(width)
            .heightIn(min = 64.dp)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.7f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = blue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD8D8D8)
            )
        )
    }
}

@Composable
fun ScreenWithStickyButtons(
    navController: NavController,
    currentStep: Int,
    title: String,
    nextText: String = "Далее",
    onNext: () -> Unit,
    onBack: () -> Unit = { navController.popBackStack() },
    content: LazyListScope.() -> Unit
) {
    val blue = Color(0xFF006FFD)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                StepProgressBar1(currentStep = currentStep)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = title,
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(327.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))
            }

            content()
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .width(327.dp)
                    .height(45.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blue)
            ) {
                Text(nextText, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .width(327.dp)
                    .height(45.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, blue),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Text("Назад", color = blue)
            }
        }
    }
}
