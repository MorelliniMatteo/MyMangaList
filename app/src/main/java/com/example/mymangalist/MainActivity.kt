package com.example.mymangalist

import HomeScreen
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mymangalist.data.UserRepository

import com.example.mymangalist.ui.screens.LoginScreen
import com.example.mymangalist.ui.screens.RegistrationScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crea un'istanza di UserRepository qui
        val userRepository = UserRepository(application)

        // Crea il canale di notifica
        createNotificationChannel()

        setContent {
            MyMangaListApp(userRepository)
        }
    }

    // Funzione per creare il canale di notifica
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
fun MyMangaListApp(userRepository: UserRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController, userRepository = userRepository)
        }
        composable("registration") {
            RegistrationScreen(navController = navController, userRepository = userRepository)
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
    }
}
