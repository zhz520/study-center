package cn.zhzgo.study.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cn.zhzgo.study.ui.screens.*
import cn.zhzgo.study.ui.screens.tools.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val navigateToWebView: (String, String) -> Unit = { url, title ->
        val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
        navController.navigate("webview/$encodedUrl/$encodedTitle")
    }

    NavHost(navController = navController, startDestination = "login") {
        
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
