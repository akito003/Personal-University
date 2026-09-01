package com.personaluniversity.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.personaluniversity.app.ui.screens.*
import com.personaluniversity.app.ui.theme.*

private data class RoleTab(val route: String, val letter: String, val label: String)

private val tabs = listOf(
    RoleTab("daily_review", "R", "Review"),
    RoleTab("syllabus", "L", "Learn"),
    RoleTab("council", "C", "Council"),
    RoleTab("progress", "P", "Progress"),
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Bottom nav only shows on the four top-level destinations
    val showBottomNav = tabs.any { it.route == currentRoute }

    Scaffold(
        containerColor = Ink,
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(containerColor = Surface, contentColor = Parchment) {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Text(
                                    tab.letter,
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp,
                                    color = if (selected) Gold else TextMuted
                                )
                            },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Gold,
                                selectedTextColor = Gold,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = SurfaceRaised
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "daily_review",
            modifier = Modifier.padding(padding)
        ) {
            composable("daily_review") {
                DailyReviewScreen(
                    onNavigateToSyllabus = { navController.navigate("syllabus") }
                )
            }
            composable("syllabus") {
                SyllabusCatalogScreen(
                    onOpenCourse = { courseId -> navController.navigate("course/$courseId") }
                )
            }
            composable("council") {
                CouncilScreen()
            }
            composable("progress") {
                ProgressScreen()
            }
            composable(
                route = "course/{courseId}",
                arguments = listOf(navArgument("courseId") { type = NavType.IntType })
            ) { backStack ->
                val courseId = backStack.arguments?.getInt("courseId") ?: return@composable
                CourseDetailScreen(
                    courseId = courseId,
                    onBack = { navController.popBackStack() },
                    onOpenLesson = { lessonId -> navController.navigate("course/$courseId/lesson/$lessonId") }
                )
            }
            composable(
                route = "course/{courseId}/lesson/{lessonId}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.IntType },
                    navArgument("lessonId") { type = NavType.IntType }
                )
            ) { backStack ->
                val courseId = backStack.arguments?.getInt("courseId") ?: return@composable
                val lessonId = backStack.arguments?.getInt("lessonId") ?: return@composable
                LessonScreen(
                    lessonId = lessonId,
                    courseId = courseId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
