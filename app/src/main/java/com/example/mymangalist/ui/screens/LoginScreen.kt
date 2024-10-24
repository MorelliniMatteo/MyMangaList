package com.example.mymangalist.ui.screens

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import com.example.mymangalist.data.UserRepositoryInterface
import com.example.mymangalist.data.LoginResult
import com.example.mymangalist.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(navController: NavController, userRepository: UserRepositoryInterface) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // State per i campi di input
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }
    var location: Location? by remember { mutableStateOf(null) }

    val scope = rememberCoroutineScope()

    // Funzione per creare il canale di notifica
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Login Channel"
            val descriptionText = "Channel for login notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("login_channel", name, importance).apply {
                description = descriptionText
            }
            // Registra il canale con il sistema
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Funzione per inviare la notifica di accesso
    fun sendLoginNotification(context: Context, username: String, location: Location?) {
        // Verifica il permesso per inviare notifiche
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1003)
            return
        }

        val channelId = "login_channel"
        val locationText = location?.let { "from ${it.latitude}, ${it.longitude}" } ?: "location not available"

        // Crea la notifica
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("New Login Detected")
            .setContentText("New login to profile: $username, $locationText.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Mostra la notifica
        with(NotificationManagerCompat.from(context)) {
            notify(2, builder.build())
        }
    }

    // Funzione per gestire il login
    fun loginUser(location: Location?) {
        if (username.isEmpty() || password.isEmpty()) {
            loginError = "All fields must be filled"
            return
        }

        userRepository.loginUser(username, password, object : UserRepositoryInterface.Callback<LoginResult> {
            override fun onResult(result: LoginResult) {
                scope.launch {
                    when (result) {
                        is LoginResult.Success -> {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                createNotificationChannel(context) // Crea il canale prima di inviare la notifica
                                sendLoginNotification(context, username, location)
                                navController.navigate("home")
                            }
                        }
                        is LoginResult.InvalidCredentials -> {
                            loginError = "Invalid username or password"
                        }
                        is LoginResult.UserNotFound -> {
                            loginError = "User not found"
                        }
                        is LoginResult.Error -> {
                            loginError = "An error occurred during login"
                        }
                    }
                }
            }
        })
    }

    // Funzione per ottenere la posizione corrente e gestire il login
    fun getLocationAndLogin() {
        // Verifica i permessi di localizzazione
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Richiedi i permessi di localizzazione
            ActivityCompat.requestPermissions(context as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1002
            )
            return
        }

        // Ottieni l'ultima posizione
        fusedLocationClient.lastLocation.addOnCompleteListener { task: Task<Location> ->
            location = if (task.isSuccessful && task.result != null) task.result else null
            loginUser(location) // Chiama loginUser() dopo aver ottenuto la posizione
        }
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

        Text(text = "Login to MyMangaList", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
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

        // Mostra gli errori di login, se presenti
        if (loginError.isNotEmpty()) {
            Text(text = loginError, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { getLocationAndLogin() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LOGIN")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Collegamento alla pagina di registrazione
        Text(
            text = "Don't have an account? Register here",
            color = Color.Blue,
            modifier = Modifier
                .clickable {
                    navController.navigate("registration")
                }
        )
    }
}
