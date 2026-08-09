package com.example

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.account.AccountScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.saved.SavedScreen
import com.example.ui.search.SearchResultsScreen

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Saved : Screen("saved", "Saved", Icons.Filled.Bookmark)
    object History : Screen("history", "History", Icons.Filled.History)
    object Account : Screen("account", "Account", Icons.Filled.Person)
}

val items = listOf(
    Screen.Home,
    Screen.Saved,
    Screen.History,
    Screen.Account
)

@Composable
fun SrachApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var isLoggedIn by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var username by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    val showBottomBar = currentDestination?.route in items.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "welcome",
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            composable("welcome") {
                com.example.ui.onboarding.WelcomeScreen(
                    onNavigateToLogin = { navController.navigate("login") },
                    onNavigateToSignup = { navController.navigate("signup") }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onSearch = { query ->
                        if (query.isNotBlank()) {
                            navController.navigate("search?q=${query}")
                        }
                    }
                )
            }
            composable(Screen.Saved.route) { SavedScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Account.route) { 
                AccountScreen(
                    isLoggedIn = isLoggedIn,
                    username = username,
                    onNavigateToLogin = { navController.navigate("login") },
                    onNavigateToSignup = { navController.navigate("signup") },
                    onLogout = {
                        isLoggedIn = false
                        username = ""
                        navController.navigate("welcome") {
                            popUpTo(0)
                        }
                    }
                )
            }
            composable(
                route = "search?q={query}",
                arguments = listOf(navArgument("query") { type = NavType.StringType })
            ) { backStackEntry ->
                val query = backStackEntry.arguments?.getString("query") ?: ""
                SearchResultsScreen(
                    initialQuery = query,
                    onBack = { navController.popBackStack() },
                    onSearch = { newQuery ->
                        navController.navigate("search?q=${newQuery}") {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onResultClick = { url ->
                        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                        navController.navigate("browser?url=$encodedUrl")
                    }
                )
            }
            composable("login") {
                com.example.ui.auth.LoginScreen(
                    onBack = { navController.popBackStack() },
                    onLoginSuccess = { user ->
                        isLoggedIn = true
                        username = user
                        navController.navigate(Screen.Home.route) {
                            popUpTo("welcome") { inclusive = true }
                        }
                    },
                    onNavigateToSignup = {
                        navController.navigate("signup") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable("signup") {
                com.example.ui.auth.SignupScreen(
                    onBack = { navController.popBackStack() },
                    onSignupSuccess = { user ->
                        isLoggedIn = true
                        username = user
                        navController.navigate(Screen.Home.route) {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = "browser?url={url}",
                arguments = listOf(navArgument("url") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
                val decodedUrl = java.net.URLDecoder.decode(encodedUrl, "UTF-8")
                com.example.ui.browser.BrowserScreen(
                    url = decodedUrl,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
