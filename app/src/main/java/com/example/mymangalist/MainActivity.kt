package com.example.mymangalist

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mymangalist.data.MangaRepository
import com.example.mymangalist.data.UserRepository
import com.example.mymangalist.ui.screens.*
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializzazione della Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "API_KEY")
        }

        val userRepository = UserRepository(application)
        val mangaRepository = MangaRepository(application)
        createNotificationChannel()

        setContent {
            MyMangaListApp(userRepository = userRepository, mangaRepository = mangaRepository)
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
fun MyMangaListApp(userRepository: UserRepository, mangaRepository: MangaRepository) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController, userRepository = userRepository)
        }
        composable("registration") {
            RegistrationScreen(navController = navController, userRepository = userRepository)
        }
        composable("home/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            val filter = backStackEntry.arguments?.getString("filter") ?: ""
            HomeScreen(
                navController = navController,
                userRepository = userRepository,
                mangaRepository = mangaRepository,
                username = username,
                filter = filter,  // Passa il filtro
                onMangaClick = { manga ->
                    navController.navigate("details/${manga.id}/$username")
                }
            )
        }
        composable("profile/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            UserProfileScreen(
                navController = navController,
                userRepository = userRepository,
                mangaRepository = mangaRepository,
                username = username,
                context = context
            )
        }
        composable("add_manga/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            AddScreen(
                navController = navController,
                userId = username,
                mangaRepository = mangaRepository,
                onMangaAdded = { navController.popBackStack() }
            )
        }
        composable("map") {
            MapScreen(
                navController = navController,
                onLocationSelected = { cityName, latLng ->
                    // Handle selected location (cityName, latLng)
                }
            )
        }
        composable("details/{mangaId}/{username}") { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: "unknown"
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            DetailsMangaScreenComposable(
                mangaId = mangaId,
                mangaRepository = mangaRepository,
                navController = navController,
                username = username,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("filter_screen/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            FilterScreen(navController = navController, username = username)
        }
        composable("home/{username}/{filter}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            val filter = backStackEntry.arguments?.getString("filter") ?: ""
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
}
