import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.diploma.R

@Composable
fun StartScreen(navController: NavController) {
    /** По умолчанию родитель — иначе «Далее» без выбора вёл на LoginScreen/specialist. */
    var selectedRole by remember { mutableStateOf("parent") }
    val blue = Color(0xFF006FFD)

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.img1),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(411.dp)
                .padding(bottom = 20.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.padding(10.dp))

        Text(
            text = "Давайте начнём",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = blue
            ),
            modifier = Modifier.padding(bottom = 30.dp)
        )

        Text(
            text = "Кем вы будете пользоваться приложением?",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = Color.Black
            ),
            modifier = Modifier.padding(bottom = 40.dp)
        )

        Button(
            onClick = {
                selectedRole = "parent"
                navController.navigate("parentScreen")
            },
            modifier = Modifier
                .width(327.dp)
                .height(45.dp)
                .border(1.dp, blue, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text(text = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67 Я родитель / опекун", color = blue)
        }

        Spacer(modifier = Modifier.padding(10.dp))

        Button(
            onClick = {
                selectedRole = "specialist"
                navController.navigate("SpecialistStartPage")
            },
            modifier = Modifier
                .width(327.dp)
                .height(45.dp)
                .border(1.dp, blue, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text(text = "\uD83D\uDC69\u200D⚕\uFE0F Я специалист", color = blue)
        }

        Spacer(modifier = Modifier.padding(18.dp))

        Button(
            onClick = {
                val role = when (selectedRole) {
                    "specialist" -> "specialist"
                    else -> "parent"
                }
                navController.navigate("LoginScreen/$role")
            },
            modifier = Modifier
                .width(327.dp)
                .height(45.dp)
                .border(5.dp, blue, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = blue)
        ) {
            Text(text = "Далее", color = Color.White)
        }
    }
}
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    // Создаем NavController для Preview
//    val navController = rememberNavController()
//
//    MaterialTheme {
//        StartScreen(navController = navController)
//    }
//}





