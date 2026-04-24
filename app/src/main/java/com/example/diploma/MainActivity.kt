package kz.aruzhan.care_steps

import LoginScreen
import ForgotPasswordCodePage
import ForgotPasswordEmailPage
import ForgotPasswordNewPasswordPage
import StartScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kz.aruzhan.care_steps.data.remote.TokenStorage
import kz.aruzhan.care_steps.ui.auth.SpecialistViewModel
import kz.aruzhan.care_steps.ui.child.ChildViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenStorage.loadTokens(this)
        CartRepository.init(this)
        FavoritesRepository.init(this)
        PurchaseAnalyticsRepository.init(this)
        setContent {
            MaterialTheme {
                // РСЃРїРѕР»СЊР·СѓРµРј NavController РґР»СЏ РЅР°РІРёРіР°С†РёРё
                val navController = rememberNavController()
                val childVm: ChildViewModel = viewModel()
                val specialistVm: SpecialistViewModel = viewModel()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val courseFilters = remember { CourseFilters() }
                    NavHost(navController = navController, startDestination = "startScreen") {
                        composable("startScreen") {
                            StartScreen(navController = navController)
                        }
                        composable("parentScreen") {
                            ParentScreen(navController = navController) // РџРµСЂРµРґР°РµРј navController
                        }
                        composable(
                            "LoginScreen/{role}",
                            arguments = listOf(navArgument("role") {
                                type = NavType.StringType
                                defaultValue = "specialist"
                            })
                        ) { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role") ?: "specialist"
                            LoginScreen(navController = navController, role = role)
                        }
                        composable(
                            "ForgotPasswordEmailPage/{role}",
                            arguments = listOf(navArgument("role") {
                                type = NavType.StringType
                                defaultValue = "specialist"
                            })
                        ) { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role") ?: "specialist"
                            ForgotPasswordEmailPage(navController = navController, role = role)
                        }
                        composable(
                            "ForgotPasswordCodePage/{role}?email={email}&retryAfter={retryAfter}",
                            arguments = listOf(
                                navArgument("role") {
                                    type = NavType.StringType
                                    defaultValue = "specialist"
                                },
                                navArgument("email") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                                navArgument("retryAfter") {
                                    type = NavType.IntType
                                    defaultValue = 14
                                }
                            )
                        ) { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role") ?: "specialist"
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            val retryAfter = backStackEntry.arguments?.getInt("retryAfter") ?: 14
                            ForgotPasswordCodePage(
                                navController = navController,
                                role = role,
                                email = email,
                                initialRetryAfter = retryAfter
                            )
                        }
                        composable(
                            "ForgotPasswordNewPasswordPage/{role}?email={email}&resetToken={resetToken}",
                            arguments = listOf(
                                navArgument("role") {
                                    type = NavType.StringType
                                    defaultValue = "specialist"
                                },
                                navArgument("email") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                                navArgument("resetToken") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                }
                            )
                        ) { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role") ?: "specialist"
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            val resetToken = backStackEntry.arguments?.getString("resetToken") ?: ""
                            ForgotPasswordNewPasswordPage(
                                navController = navController,
                                role = role,
                                email = email,
                                resetToken = resetToken
                            )
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
                        composable("ParentProfileEditPage") {
                            ParentProfileEditPage(navController)
                        }
                        composable("ParentChildEditPage") {
                            ParentChildEditPage(navController)
                        }
                        composable("SpecialistStartPage") {
                            SpecialistStartPage(navController)
                        }
                        composable("SAccountCreateFirstPage") {
                            SAccountCreateFirstPage(navController, specialistVm)
                        }
                        composable("SAccountCreateSecondPage") {
                            SAccountCreateSecondPage(navController, specialistVm)
                        }
                        composable("SAccountCreateThirdPage") {
                            SAccountCreateThirdPage(navController, specialistVm)
                        }
                        composable("SAccountCreateFourthPage") {
                            SAccountCreateFourthPage(navController, specialistVm)
                        }
                        composable("SAccountCreateFifthPage") {
                            SAccountCreateFifthPage(navController, specialistVm)
                        }
                        composable("SAccountCreateSixthPage") {
                            SAccountCreateSixthPage(navController, specialistVm)
                        }
                        composable("SAccountCreateSeventhPage") {
                            SAccountCreateSeventhPage(navController, specialistVm)
                        }
                        composable("SettingsPageSpecialist") {
                            SettingsPageSpecialist(navController)
                        }
                        composable("SpecialistProfileSettingsPage") {
                            SpecialistProfileSettingsPage(navController)
                        }
                        composable("SpecialistDashboardPage") {
                            SpecialistDashboardPage(navController)
                        }
                        composable("ChildModePage") {
                            ChildModePage(navController)
                        }
                        composable("ChildModeSettingsPage") {
                            ChildModeSettingsPage(navController)
                        }
                        composable("ChatBotPage") {
                            ChatBotPage(navController)
                        }
                        composable("ParentInsightsPage") {
                            ParentInsightsPage(navController)
                        }
                        composable("DailySurveyPage") {
                            DailySurveyPage(navController)
                        }
                        composable("SpecialistsPage") {
                            SpecialistsPage(navController)
                        }
                        composable("ChildGamesPage") {
                            ChildGamesPage(navController)
                        }
                        composable("ModuleOneGamesPage") {
                            ModuleOneGamesPage(navController)
                        }
                        composable("DailyRoutineGamePage") {
                            DailyRoutineGamePage(navController)
                        }
                        composable("EmotionGamePage") {
                            EmotionGamePage(navController)
                        }
                        composable("AssembleAnimalGamePage") {
                            AssembleAnimalGameCalibratedPage(navController)
                        }
                        composable("DrawTriangleSquareGamePage") {
                            DrawTriangleSquareGamePage(navController)
                        }
                        composable("ColorFinderGamePage") {
                            ColorFinderGamePage(navController)
                        }
                        composable("CountingGamePage") {
                            CountingGamePage(navController)
                        }
                        composable("GuessSoundGamePage") {
                            GuessSoundGamePage(navController)
                        }
                        composable("UpDownGamePage") {
                            UpDownGamePage(navController)
                        }
                        composable("SpecialistCoursesPage") {
                            SpecialistCoursesPage(navController)
                        }
                        composable(
                            "CourseDetailsPage/{courseId}?mode={mode}",
                            arguments = listOf(navArgument("courseId") {
                                type = NavType.IntType
                                defaultValue = 1
                            }, navArgument("mode") {
                                type = NavType.StringType
                                defaultValue = "parent"
                            })
                        ) { backStackEntry ->
                            val courseId = backStackEntry.arguments?.getInt("courseId") ?: 1
                            val mode = backStackEntry.arguments?.getString("mode") ?: "parent"
                            CourseDetailsPage(navController, courseId, mode)
                        }
                        composable("CreateCoursePage") {
                            CreateCoursePage(navController)
                        }
                        composable("CourseSearchPage") {
                            CourseSearchPage(navController, courseFilters)
                        }
                        composable("SearchResultsPage/{query}") { backStackEntry ->
                            val query = backStackEntry.arguments?.getString("query") ?: ""
                            SearchResultsPage(navController, query, courseFilters)
                        }
                        composable("CourseFilterPage") {
                            CourseFilterPage(navController, courseFilters)
                        }
                        composable("SpecialistSearchPage") {
                            SpecialistSearchPage(navController)
                        }
                        composable("SpecialistSearchResultsPage/{query}") { backStackEntry ->
                            val query = backStackEntry.arguments?.getString("query") ?: ""
                            SpecialistSearchResultsPage(navController, query)
                        }
                        composable(
                            "SpecialistProfilePage/{specialistId}",
                            arguments = listOf(navArgument("specialistId") {
                                type = NavType.IntType
                                defaultValue = 1
                            })
                        ) { backStackEntry ->
                            val specialistId = backStackEntry.arguments?.getInt("specialistId") ?: 1
                            SpecialistProfilePage(navController, specialistId)
                        }
                        composable("CartPage") {
                            CartPage(navController)
                        }
                        composable("FavoritesPage") {
                            FavoritesPage(navController)
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
//    val navController = rememberNavController()  // РЎРѕР·РґР°РµРј NavController
//    MaterialTheme {
//        LoginScreen(navController = navController) // РџРµСЂРµРґР°РµРј РµРіРѕ РІ LoginScreen
//    }
//}









