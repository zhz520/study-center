package cn.zhzgo.study.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.browser.customtabs.CustomTabsIntent
import android.net.Uri
import cn.zhzgo.study.ui.screens.*
import cn.zhzgo.study.ui.screens.tools.*
import cn.zhzgo.study.data.UserPreferences
import kotlinx.coroutines.flow.first
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val authState by userPrefs.authToken.collectAsState(initial = "loading")
    
    if (authState == "loading") {
        cn.zhzgo.study.ui.components.LoadingOverlay(isLoading = true, message = "启动中...")
        return
    }

    val startDestination = if (authState.isNullOrEmpty()) "login" else "home"

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

    val bottomNavRoutes = listOf("home", "study", "resources", "favorites")

    Scaffold(
        bottomBar = {
            if (bottomNavRoutes.contains(currentRoute)) {
                val lastClickTime = remember { androidx.compose.runtime.mutableLongStateOf(0L) }
                
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    val colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("首页") },
                        selected = currentRoute == "home",
                        colors = colors,
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTime.longValue > 300L && currentRoute != "home") {
                                lastClickTime.longValue = now
                                navController.navigate("home") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Study") },
                        label = { Text("题库") },
                        selected = currentRoute == "study",
                        colors = colors,
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTime.longValue > 300L && currentRoute != "study") {
                                lastClickTime.longValue = now
                                navController.navigate("study") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Resources") },
                        label = { Text("资源") },
                        selected = currentRoute == "resources",
                        colors = colors,
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTime.longValue > 300L && currentRoute != "resources") {
                                lastClickTime.longValue = now
                                navController.navigate("resources") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("收藏") },
                        selected = currentRoute == "favorites",
                        colors = colors,
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (now - lastClickTime.longValue > 300L && currentRoute != "favorites") {
                                lastClickTime.longValue = now
                                navController.navigate("favorites") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
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
                onNavigateToLeaderboard = {
                    navController.navigate("leaderboard")
                },
                onNavigateToAdmin = {
                    val token = if (authState != "loading") authState else ""
                    val adminUrl = "https://study.zhzgo.cn/admin?token=$token&role=admin"
                    val builder = androidx.browser.customtabs.CustomTabsIntent.Builder()
                    
                    // Match app's dark mode aesthetic if possible
                    val colorSchemeParams = androidx.browser.customtabs.CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(android.graphics.Color.parseColor("#121212"))
                        .build()
                    builder.setDefaultColorSchemeParams(colorSchemeParams)
                    
                    val customTabsIntent = builder.build()
                    customTabsIntent.launchUrl(context, android.net.Uri.parse(adminUrl))
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToWebView = navigateToWebView
            )
        }

        composable("leaderboard") {
            LeaderboardScreen(
                onBack = { navController.navigateUp() }
            )
        }

        // --- Content & Study Screens ---

        composable("study") {
            StudyScreen(
                onSubjectSelected = { subject, isMistakes ->
                    val encodedName = URLEncoder.encode(subject.name.ifEmpty { "Subject" }, StandardCharsets.UTF_8.toString())
                    navController.navigate("question/${subject.id}/$encodedName?isMistakes=$isMistakes")
                }
            )
        }

        composable(
            route = "question/{subjectId}/{subjectName}?isMistakes={isMistakes}",
            arguments = listOf(
                navArgument("subjectId") { type = NavType.IntType },
                navArgument("subjectName") { type = NavType.StringType },
                navArgument("isMistakes") { 
                    type = NavType.BoolType
                    defaultValue = false 
                }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: 0
            val subjectName = URLDecoder.decode(backStackEntry.arguments?.getString("subjectName") ?: "", StandardCharsets.UTF_8.toString())
            val isMistakes = backStackEntry.arguments?.getBoolean("isMistakes") ?: false
            
            QuestionScreen(
                subjectId = subjectId,
                subjectName = subjectName,
                isFavorites = false,
                isMistakes = isMistakes,
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
                onNavigateToPracticeMode = { 
                    navController.navigate("favorite_question")
                }
            )
        }
        
        composable("favorite_question") {
            QuestionScreen(
                subjectId = 0,
                subjectName = "我的收藏",
                isFavorites = true,
                onBack = { navController.navigateUp() }
            )
        }
        
        // Deprecated admin_webview block removed according to user feedback
        
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

        composable("tools/base_converter") { BaseConverterScreen(onBack = { navController.navigateUp() }) }
        composable("tools/bmi_calculator") { BmiCalculatorScreen(onBack = { navController.navigateUp() }) }
        composable("tools/calculator") { CalculatorScreen(onBack = { navController.navigateUp() }) }
        composable("tools/color_picker") { ColorPickerScreen(onBack = { navController.navigateUp() }) }
        composable("tools/date_calculator") { DateCalculatorScreen(onBack = { navController.navigateUp() }) }
        composable("tools/dev_http_test") { DevHttpTestScreen(onBack = { navController.navigateUp() }) }
        composable("tools/dev_json_regex") { DevJsonRegexScreen(onBack = { navController.navigateUp() }) }
        composable("tools/doc_image_to_pdf") { DocImageToPdfScreen(onBack = { navController.navigateUp() }) }
        composable("tools/doc_markdown") { DocMarkdownScreen(onBack = { navController.navigateUp() }) }
        composable("tools/doc_pdf_to_image") { DocPdfToImageScreen(onBack = { navController.navigateUp() }) }
        composable("tools/image_compress") { ImageCompressScreen(onBack = { navController.navigateUp() }) }
        composable("tools/image_filter") { ImageFilterScreen(onBack = { navController.navigateUp() }) }
        composable("tools/image_segment") { ImageSegmentScreen(onBack = { navController.navigateUp() }) }
        composable("tools/image_watermark") { ImageWatermarkScreen(onBack = { navController.navigateUp() }) }
        composable("tools/kinship_calculator") { KinshipCalculatorScreen(onBack = { navController.navigateUp() }) }
        composable("tools/media_audio_format") { MediaAudioFormatScreen(onBack = { navController.navigateUp() }) }
        composable("tools/media_video_compress") { MediaVideoCompressScreen(onBack = { navController.navigateUp() }) }
        composable("tools/media_video_to_audio") { MediaVideoToAudioScreen(onBack = { navController.navigateUp() }) }
        composable("tools/password_generator") { PasswordGeneratorScreen(onBack = { navController.navigateUp() }) }
        composable("tools/random_generator") { RandomGeneratorScreen(onBack = { navController.navigateUp() }) }
        composable("tools/security_codec") { SecurityCodecScreen(onBack = { navController.navigateUp() }) }
        composable("tools/security_crypto") { SecurityCryptoScreen(onBack = { navController.navigateUp() }) }
        composable("tools/security_jwt") { SecurityJwtScreen(onBack = { navController.navigateUp() }) }
        composable("tools/stopwatch") { StopwatchScreen(onBack = { navController.navigateUp() }) }
        composable("tools/text_counter") { TextCounterScreen(onBack = { navController.navigateUp() }) }
        composable("tools/timestamp_converter") { TimestampConverterScreen(onBack = { navController.navigateUp() }) }
        composable("tools/unit_converter") { UnitConverterScreen(onBack = { navController.navigateUp() }) }
        composable("tools/video_parser") { VideoParserScreen(onBack = { navController.navigateUp() }) }
    }
}
}
