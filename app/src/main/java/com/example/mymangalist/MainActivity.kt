package com.example.mymangalist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mymangalist.data.UserRepository
import com.example.mymangalist.ui.screens.HomeScreen
import com.example.mymangalist.ui.screens.LoginScreen
import com.example.mymangalist.ui.screens.RegistrationScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crea un'istanza di UserRepository qui
        val userRepository = UserRepository(application)

        setContent {
            MyMangaListApp(userRepository)
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
