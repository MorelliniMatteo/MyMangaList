package com.example.mymangalist.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.mymangalist.data.UserRepositoryInterface
import com.example.mymangalist.R
import com.example.mymangalist.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(navController: NavController, userRepository: UserRepositoryInterface) {
    val context = LocalContext.current

    // State per i campi di input
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Funzione per creare il canale di notifica
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "welcome_channel"
            val name = "Registration Notifications"
            val descriptionText = "Channel for registration success notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Funzione per inviare la notifica di benvenuto con controllo permessi
    fun sendWelcomeNotification() {
        val channelId = "welcome_channel"

        // Verifica se l'app ha il permesso di inviare notifiche
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Richiedi il permesso
                if (context is ComponentActivity) {
                    ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
                }
                return
            }
        }

        createNotificationChannel()

        // Crea e invia la notifica
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Registrazione avvenuta con successo")
            .setContentText("Benvenuto, $username! Grazie per esserti unito a MyMangaList.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }

    // Funzione per gestire la registrazione
    fun registerUser() {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        userRepository.isUsernameTaken(username, object : UserRepositoryInterface.Callback<Boolean> {
            override fun onResult(isTaken: Boolean) {
                if (isTaken) {
                    Toast.makeText(context, "Username already taken", Toast.LENGTH_SHORT).show()
                } else {
                    userRepository.isEmailTaken(email, object : UserRepositoryInterface.Callback<Boolean> {
                        override fun onResult(isTaken: Boolean) {
                            if (isTaken) {
                                Toast.makeText(context, "Email already taken", Toast.LENGTH_SHORT).show()
                            } else {
                                val newUser = User(username, email, password)
                                userRepository.registerUser(newUser)
                                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()

                                // Invia la notifica di benvenuto
                                sendWelcomeNotification()

                                // Naviga alla schermata home
                                navController.navigate("home/${newUser.username}") // Passa il nome utente
                            }
                        }
                    })
                }
            }
        })
    }

    // Composable per la UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(217.dp, 233.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Register to MyMangaList", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { registerUser() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("REGISTER")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Do you already have an account? Sign in",
            color = Color.Blue,
            modifier = Modifier
                .clickable {
                    navController.navigate("login") // Naviga alla schermata di login
                }
        )
    }
}
