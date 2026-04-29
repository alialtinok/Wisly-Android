package com.alialtinok.lexiup.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alialtinok.lexiup.ui.screens.home.HomeScreen
import com.alialtinok.lexiup.ui.screens.my.FavoritesScreen
import com.alialtinok.lexiup.ui.screens.my.MyScreen
import com.alialtinok.lexiup.ui.screens.my.MyWordsScreen
import com.alialtinok.lexiup.ui.screens.my.UnknownWordsScreen
import com.alialtinok.lexiup.ui.screens.practice.PracticeScreen
import com.alialtinok.lexiup.ui.screens.study.FlashcardScreen
import com.alialtinok.lexiup.ui.screens.study.StudyScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTopLevel = TopLevelRoute.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    TopLevelRoute.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute == tab.route) return@NavigationBarItem
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelRoute.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(TopLevelRoute.Home.route) { HomeScreen() }
            composable(TopLevelRoute.Study.route) {
                StudyScreen(
                    onNavigateToFlashcards = { navController.navigate(Routes.FLASHCARDS) },
                )
            }
            composable(TopLevelRoute.Practice.route) { PracticeScreen() }
            composable(TopLevelRoute.My.route) {
                MyScreen(
                    onNavigateToFavorites = { navController.navigate(Routes.FAVORITES) },
                    onNavigateToUnknown = { navController.navigate(Routes.UNKNOWN) },
                    onNavigateToMyWords = { navController.navigate(Routes.MY_WORDS) },
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.UNKNOWN) {
                UnknownWordsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.MY_WORDS) {
                MyWordsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.FLASHCARDS) {
                FlashcardScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
