package com.example.mymangalist

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mymangalist.data.UserRepository
import com.example.mymangalist.data.MangaRepository
import com.example.mymangalist.data.UserRepositoryInterface
import com.example.mymangalist.ui.screens.HomeScreen
import com.example.mymangalist.ui.screens.LoginScreen
import com.example.mymangalist.ui.screens.RegistrationScreen
import com.example.mymangalist.ui.screens.UserProfileScreen
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userRepository = UserRepository(application)
        val mangaRepository = MangaRepository(application)
        createNotificationChannel()

        setContent {
            MyMangaListApp(userRepository, mangaRepository)
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
            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun MyMangaListApp(userRepository: UserRepository, mangaRepository: MangaRepository) {
    val navController = rememberNavController()
    val context = LocalContext.current  // Otteniamo il context

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController, userRepository = userRepository)
        }
        composable("registration") {
            RegistrationScreen(navController = navController, userRepository = userRepository)
        }
        composable("home/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            HomeScreen(
                navController = navController,
                userRepository = userRepository,
                mangaRepository = mangaRepository,
                username = username
            )
        }

        composable("profile/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "unknown"
            UserProfileScreen(
                navController = navController,
                userRepository = userRepository,
                mangaRepository = mangaRepository,
                username = username,
                context = context  // passiamo il context a UserProfileScreen
            )
        }
    }
}