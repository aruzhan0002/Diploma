package com.example.diploma

import LoginScreen
import StartScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.diploma.data.remote.TokenStorage
import com.example.diploma.ui.child.ChildViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenStorage.loadTokens(this)
        setContent {
            MaterialTheme {
                // Используем NavController для навигации
                val navController = rememberNavController()
                val childVm: ChildViewModel = viewModel()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Навигация
                    NavHost(navController = navController, startDestination = "startScreen") {
                        composable("startScreen") {
                            StartScreen(navController = navController)
                        }
                        composable("parentScreen") {
                            ParentScreen(navController = navController) // Передаем navController
                        }
                        composable("LoginScreen") {
                            LoginScreen(navController = navController)
                        }
                        composable("CreateAccountFirstPage") {
                            CreateAccountFirstPage(navController = navController)
                        }
                        composable("AccountCreateSecondPage") {
                            AccountCreateSecondPage(navController = navController)
                        }
                        composable("AccountCreateThirdPage") {
                            AccountCreateThirdPage(
                                navController = navController,
                                childVm = childVm
                            )
                        }

                        composable("AccountCreateFourthPage") {
                            AccountCreateFourthPage(
                                navController = navController,
                                childVm = childVm
                            )
                        }

                        composable("AccountCreateFifthPage") {
                            AccountCreateFifthPage(
                                navController = navController,
                                childVm = childVm
                            )
                        }

                        composable("AccountCreateSixthPage") {
                            AccountCreateSixthPage(
                                navController = navController,
                                childVm = childVm
                            )
                        }

                        composable("AccountCreateSeventhPage") {
                            AccountCreateSeventhPage(
                                navController = navController,
                                childVm = childVm
                            )
                        }

                        composable("AccountCreateEighthPage") {
                            AccountCreateEighthPage(
                                navController = navController,
                                childVm = childVm
                            )
                        }

                        composable("AccountCreateNinthPage") {
                            AccountCreateNinthPage(
                                navController = navController,
                                childVm = childVm
                            )
                        }
                        composable("SettingsPage") {
                            SettingsPage(navController)
                        }
                        composable("SpecialistStartPage") {
                            SpecialistStartPage(navController)
                        }
                        composable("SAccountCreateFirstPage") {
                            SAccountCreateFirstPage(navController)
                        }
                        composable("SAccountCreateSecondPage") {
                            SAccountCreateSecondPage(navController)
                        }
                        composable("SAccountCreateThirdPage") {
                            SAccountCreateThirdPage(navController)
                        }
                        composable("SAccountCreateFourthPage") {
                            SAccountCreateFourthPage(navController)
                        }
                        composable("SAccountCreateFifthPage") {
                            SAccountCreateFifthPage(navController)
                        }
                        composable("SAccountCreateSixthPage") {
                            SAccountCreateSixthPage(navController)
                        }
                        composable("SAccountCreateSeventhPage") {
                            SAccountCreateSeventhPage(navController)
                        }
                        composable("SettingsPageSpecialist") {
                            SettingsPageSpecialist(navController)
                        }
                        composable("ChildPage") {
                            ChildPage(navController)
                        }
                        composable("ChildModePage") {
                            ChildModePage(navController)
                        }
                        composable("ChildModeSettingsPage") {
                            ChildModeSettingsPage(navController)
                        }
                        composable("HomePage") {
                            ParentHomePage(navController)
                        }
                        composable("SpecialistsPage") {
                            SpecialistsPage(navController)
                        }
                        composable("ChildModulePage") {
                            ChildModulePage(navController)
                        }
                        composable("ChildPracticePage") {
                            ChildPracticePage(navController)
                        }
                        composable("ChildGamesPage") {
                            ChildGamesPage(navController)
                        }
                        composable("ChildProgramPage") {
                            ChildProgramPage(navController)
                        }
                        composable("SpecialistCoursesPage") {
                            SpecialistCoursesPage(navController)
                        }
                        composable("CourseDetailsPage") {
                            CourseDetailsPage(navController)
                        }
                        composable("CreateCoursePage") {
                            CreateCoursePage(navController)
                        }



                    }
                }
            }
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    val navController = rememberNavController()  // Создаем NavController
//    MaterialTheme {
//        LoginScreen(navController = navController) // Передаем его в LoginScreen
//    }
//}






