package kz.aruzhan.care_steps

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kz.aruzhan.care_steps.ui.child.ChildViewModel
import kotlin.math.roundToInt

@Composable
fun AccountCreateThirdPage(
    navController: NavController,
    childVm: ChildViewModel
) {

    var childName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf(5f) }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)   // только по бокам
                    .padding(bottom = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress bar (3-й шаг)
                StepProgressBar(currentStep = 3)

                Spacer(modifier = Modifier.height(24.dp))

                // Заголовок
                Text(
                    text = "Добавим профиль\nребёнка",
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .width(327.dp)
                        .height(58.dp)

                )

                Spacer(modifier = Modifier.height(24.dp))

                // Имя ребёнка (текст сверху)
                Text(
                    text = "Имя ребёнка",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    modifier = Modifier

                        .width(327.dp)
                        .height(20.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // TextField имени ребёнка
                TextField(
                    value = childName,
                    onValueChange = { childName = it },
                    placeholder = { Text("Павел") },
                    modifier = Modifier
                        .width(327.dp)
                        .height(50.dp)
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
                        disabledIndicatorColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))


                // Slider


                Row(
                    modifier = Modifier.width(327.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Возраст",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = age.toInt().toString(),
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Slider(
                    value = age,
                    onValueChange = { age = it },
                    valueRange = 1f..18f,
                    modifier = Modifier.width(327.dp),
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFF006FFD),
                        inactiveTrackColor = Color(0xFFE8E9F1),
                        thumbColor = Color(0xFF006FFD)
                    )
                )

            }


            // Кнопка Далее
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        println("🔥🔥🔥 КНОПКА НАЖАТА НА 3 ЭКРАНЕ!")
                        println("🔥 childName = $childName")
                        println("🔥 age = ${age.toInt()}")
                        childVm.updateChildInfo(
                            name = childName,
                            age = age.toInt()  // age у тебя Float от слайдера
                        )
                        println("🔥 Данные отправлены в ViewModel")
                        // ✅ Переходим на следующий экран
                        navController.navigate("AccountCreateFourthPage")
                        println("🔥 Переход на 4 экран")
                    },
                    modifier = Modifier
                        .width(327.dp)
                        .height(45.dp)
                        .border(5.dp, Color(0xFF006FFD), shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006FFD))
                ) {
                    Text(text = "Далее", color = Color.White)
                }
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        Log.d("NAV", "⬅️ ThirdPage: BACK pressed, current=${navController.currentDestination?.route}")
                        val popped = navController.popBackStack()
                        Log.d("NAV", "⬅️ ThirdPage: popBackStack result=$popped, now=${navController.currentDestination?.route}")


                        if (!popped) {
                            navController.navigate("AccountCreateSecondPage") {
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier
                        .width(327.dp)
                        .height(45.dp)
                        .border(1.dp, Color(0xFF006FFD), shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(text = "Назад", color = Color(0xFF006FFD))
                }
            }
        }

    }
}


