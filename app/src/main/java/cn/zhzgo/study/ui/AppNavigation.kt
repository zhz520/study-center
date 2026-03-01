package cn.zhzgo.study.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.browser.customtabs.CustomTabsIntent
import android.net.Uri
import cn.zhzgo.study.ui.screens.*
import cn.zhzgo.study.ui.screens.tools.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val navigateToWebView: (String, String) -> Unit = { url, _ ->
        val customTabsIntent = CustomTabsIntent.Builder().build()
        try {
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("/")

    val bottomNavRoutes = listOf("home", "study", "resources", "tools", "settings")

    Scaffold(
        bottomBar = {
            if (bottomNavRoutes.contains(currentRoute)) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("首页") },
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Study") },
                        label = { Text("题库") },
                        selected = currentRoute == "study",
                        onClick = {
                            navController.navigate("study") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Resources") },
                        label = { Text("资源") },
                        selected = currentRoute == "resources",
                        onClick = {
                            navController.navigate("resources") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Build, contentDescription = "Tools") },
                        label = { Text("工具") },
                        selected = currentRoute == "tools",
                        onClick = {
                            navController.navigate("tools") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Me") },
                        label = { Text("我的") },
                        selected = currentRoute == "settings",
                        onClick = {
                            navController.navigate("settings") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
        
        // --- Core Auth & Main Screens ---

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateToBindQQ = { openId ->
                    navController.navigate("bind_qq/$openId")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigateUp()
                },
                onBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(
            route = "bind_qq/{openId}",
            arguments = listOf(navArgument("openId") { type = NavType.StringType })
        ) { backStackEntry ->
            val openId = backStackEntry.arguments?.getString("openId") ?: ""
            BindQQScreen(
                openId = openId,
                onBindSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToSubject = { subjectId, subjectName ->
                    val encodedName = URLEncoder.encode(subjectName, StandardCharsets.UTF_8.toString())
                    navController.navigate("question/$subjectId/$encodedName")
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToData = {
                    navController.navigate("data_center")
                },
                onNavigateToStats = {
                    navController.navigate("stats")
                },
                onNavigateToAdmin = {
                    // navController.navigate("admin")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToWebView = navigateToWebView
            )
        }

        // --- Content & Study Screens ---

        composable("study") {
            StudyScreen(
                onSubjectSelected = { subject ->
                    val encodedName = URLEncoder.encode(subject.name.ifEmpty { "Subject" }, StandardCharsets.UTF_8.toString())
                    navController.navigate("question/${subject.id}/$encodedName")
                }
            )
        }

        composable(
            route = "question/{subjectId}/{subjectName}",
            arguments = listOf(
                navArgument("subjectId") { type = NavType.IntType },
                navArgument("subjectName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: 0
            val subjectName = URLDecoder.decode(backStackEntry.arguments?.getString("subjectName") ?: "", StandardCharsets.UTF_8.toString())
            QuestionScreen(
                subjectId = subjectId,
                subjectName = subjectName,
                isFavorites = false,
                onBack = { navController.navigateUp() }
            )
        }

        composable("resources") {
            ResourcesScreen(
                onNavigateToArticle = { articleId -> navController.navigate("article/$articleId") },
                onNavigateToCategory = { categoryId -> /* no-op or filter local */ },
                onNavigateToContribution = { navController.navigate("contribution") },
                onNavigateToTool = { toolRoute -> navController.navigate(toolRoute) },
                onNavigateToAllTools = { navController.navigate("tools") }
            )
        }

        composable(
            route = "article/{articleId}",
            arguments = listOf(navArgument("articleId") { type = NavType.IntType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getInt("articleId") ?: 0
            ArticleDetailScreen(
                articleId = articleId,
                onBack = { navController.navigateUp() },
                onUserClick = { userId -> navController.navigate("user_profile/$userId") },
                onOpenLink = navigateToWebView
            )
        }

        composable(
            route = "user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserProfileScreen(
                userId = userId,
                onBack = { navController.navigateUp() },
                onArticleClick = { articleId -> navController.navigate("article/$articleId") }
            )
        }

        composable("contribution") {
            ContributionScreen(
                onBack = { navController.navigateUp() }
            )
        }

        composable("favorites") {
            FavoritesScreen(
                onNavigateToPracticeMode = { /* no-op */ }
            )
        }
        
        // --- Settings & Metadata ---

        composable("settings") {
            SettingsScreen(
                onBack = { navController.navigateUp() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToWebView = navigateToWebView,
                onNavigateToAccountSettings = {
                    navController.navigate("account_settings")
                }
            )
        }

        composable("account_settings") {
            AccountSettingsScreen(
                onBack = { navController.navigateUp() }
            )
        }

        composable("stats") {
            StatsScreen(
                onBack = { navController.navigateUp() }
            )
        }

        composable("data_center") {
            DataCenterScreen(
                onBack = { navController.navigateUp() }
            )
        }

        composable(
            route = "webview/{url}/{title}",
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val url = URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", StandardCharsets.UTF_8.toString())
            val title = URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", StandardCharsets.UTF_8.toString())
            WebViewScreen(
                url = url,
                title = title,
                onBack = { navController.navigateUp() }
            )
        }

        // --- Tools Screens ---

        composable("tools") {
            ToolsListScreen(
                onNavigateToTool = { toolRoute ->
                    navController.navigate(toolRoute)
                },
                onBack = { navController.navigateUp() }
            )
        }

        composable("BaseConverterScreen") { BaseConverterScreen(onBack = { navController.navigateUp() }) }
        composable("BmiCalculatorScreen") { BmiCalculatorScreen(onBack = { navController.navigateUp() }) }
        composable("CalculatorScreen") { CalculatorScreen(onBack = { navController.navigateUp() }) }
        composable("ColorPickerScreen") { ColorPickerScreen(onBack = { navController.navigateUp() }) }
        composable("DateCalculatorScreen") { DateCalculatorScreen(onBack = { navController.navigateUp() }) }
        composable("DevHttpTestScreen") { DevHttpTestScreen(onBack = { navController.navigateUp() }) }
        composable("DevJsonRegexScreen") { DevJsonRegexScreen(onBack = { navController.navigateUp() }) }
        composable("DocImageToPdfScreen") { DocImageToPdfScreen(onBack = { navController.navigateUp() }) }
        composable("DocMarkdownScreen") { DocMarkdownScreen(onBack = { navController.navigateUp() }) }
        composable("DocPdfToImageScreen") { DocPdfToImageScreen(onBack = { navController.navigateUp() }) }
        composable("ImageCompressScreen") { ImageCompressScreen(onBack = { navController.navigateUp() }) }
        composable("ImageFilterScreen") { ImageFilterScreen(onBack = { navController.navigateUp() }) }
        composable("ImageSegmentScreen") { ImageSegmentScreen(onBack = { navController.navigateUp() }) }
        composable("ImageWatermarkScreen") { ImageWatermarkScreen(onBack = { navController.navigateUp() }) }
        composable("KinshipCalculatorScreen") { KinshipCalculatorScreen(onBack = { navController.navigateUp() }) }
        composable("MediaAudioFormatScreen") { MediaAudioFormatScreen(onBack = { navController.navigateUp() }) }
        composable("MediaVideoCompressScreen") { MediaVideoCompressScreen(onBack = { navController.navigateUp() }) }
        composable("MediaVideoToAudioScreen") { MediaVideoToAudioScreen(onBack = { navController.navigateUp() }) }
        composable("PasswordGeneratorScreen") { PasswordGeneratorScreen(onBack = { navController.navigateUp() }) }
        composable("RandomGeneratorScreen") { RandomGeneratorScreen(onBack = { navController.navigateUp() }) }
        composable("SecurityCodecScreen") { SecurityCodecScreen(onBack = { navController.navigateUp() }) }
        composable("SecurityCryptoScreen") { SecurityCryptoScreen(onBack = { navController.navigateUp() }) }
        composable("SecurityJwtScreen") { SecurityJwtScreen(onBack = { navController.navigateUp() }) }
        composable("StopwatchScreen") { StopwatchScreen(onBack = { navController.navigateUp() }) }
        composable("TextCounterScreen") { TextCounterScreen(onBack = { navController.navigateUp() }) }
        composable("TimestampConverterScreen") { TimestampConverterScreen(onBack = { navController.navigateUp() }) }
        composable("UnitConverterScreen") { UnitConverterScreen(onBack = { navController.navigateUp() }) }
    }
}
}
