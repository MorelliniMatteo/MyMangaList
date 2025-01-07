package com.example.mymangalist

import RegistrationScreen
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mymangalist.data.MangaRepository
import com.example.mymangalist.data.UserRepository
import com.example.mymangalist.ui.components.ThemeManager
import com.example.mymangalist.ui.screens.*
import com.example.mymangalist.ui.theme.MyMangaListTheme
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var themeManager: ThemeManager
    private var currentUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "API_KEY")
        }

        val userRepository = UserRepository(application)
        val mangaRepository = MangaRepository(application)
        themeManager = ThemeManager(applicationContext)
        createNotificationChannel()

        setContent {
            var username by remember { mutableStateOf<String?>(null) }
            val themeState = themeManager.isDarkThemeForUser(username ?: "").collectAsState(initial = false)
            val isDarkTheme = themeState.value  // Estraiamo il valore booleano dallo State

            DisposableEffect(Unit) {
                onDispose {
                    currentUsername = username
                }
            }

            MyMangaListApp(
                userRepository = userRepository,
                mangaRepository = mangaRepository,
                isDarkTheme = isDarkTheme,  // Ora passiamo un Boolean invece di uno State<Boolean>
                onThemeChange = { newTheme ->
                    username?.let { currentUser ->
                        lifecycleScope.launch {
                            themeManager.setDarkTheme(currentUser, newTheme)
                        }
                    }
                },
                onUsernameChange = { newUsername ->
                    username = newUsername
                }
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "welcome_channel"
            val channelName = "Welcome Notifications"
            val descriptionText = "Channel for Welcome Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun MyMangaListApp(
    userRepository: UserRepository,
    mangaRepository: MangaRepository,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onUsernameChange: (String) -> Unit
) {
    val navController = rememberNavController()

    // Update username when navigation happens
    navController.addOnDestinationChangedListener { _, destination, arguments ->
        arguments?.getString("username")?.let { username ->
            onUsernameChange(username)
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            MyMangaListTheme(darkTheme = false) {
                LoginScreen(navController = navController, userRepository = userRepository)
            }
        }
        composable("registration") {
            MyMangaListTheme(darkTheme = false) {
                RegistrationScreen(navController = navController, userRepository = userRepository)
            }
        }
        composable("home/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            MyMangaListTheme(darkTheme = isDarkTheme) {
                HomeScreen(
                    navController = navController,
                    userRepository = userRepository,
                    mangaRepository = mangaRepository,
                    username = username,
                    filter = "",
                    onMangaClick = { manga ->
                        navController.navigate("details/${manga.id}/$username")
                    }
                )
            }
        }
        composable(
            route = "home/{username}/{filter}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("filter") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            val filter = backStackEntry.arguments?.getString("filter") ?: ""
            MyMangaListTheme(darkTheme = isDarkTheme) {
                HomeScreen(
                    navController = navController,
                    userRepository = userRepository,
                    mangaRepository = mangaRepository,
                    username = username,
                    filter = filter,
                    onMangaClick = { manga ->
                        navController.navigate("details/${manga.id}/$username")
                    }
                )
            }
        }
        composable("profile/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            MyMangaListTheme(darkTheme = isDarkTheme) {
                UserProfileScreen(
                    navController = navController,
                    userRepository = userRepository,
                    mangaRepository = mangaRepository,
                    username = username,
                    context = LocalContext.current
                )
            }
        }
        composable("add_manga/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            MyMangaListTheme(darkTheme = isDarkTheme) {
                AddScreen(
                    navController = navController,
                    userId = username,
                    mangaRepository = mangaRepository,
                    onMangaAdded = { navController.popBackStack() }
                )
            }
        }
        composable("map") {
            MyMangaListTheme(darkTheme = isDarkTheme) {
                MapScreen(
                    navController = navController,
                    onLocationSelected = { cityName, latLng -> }
                )
            }
        }
        composable("details/{mangaId}/{username}") { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: "unknown"
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            MyMangaListTheme(darkTheme = isDarkTheme) {
                DetailsMangaScreenComposable(
                    mangaId = mangaId,
                    mangaRepository = mangaRepository,
                    navController = navController,
                    username = username,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable("filter_screen/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            MyMangaListTheme(darkTheme = isDarkTheme) {
                FilterScreen(navController = navController, username = username)
            }
        }
        composable("settings/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            MyMangaListTheme(darkTheme = isDarkTheme) {
                SettingsScreen(
                    navController = navController,
                    username = username,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeChange
                )
            }
        }
    }
}