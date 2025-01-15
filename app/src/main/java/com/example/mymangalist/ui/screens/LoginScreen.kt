package com.example.mymangalist.ui.screens

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.location.Geocoder
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.content.ContextCompat

@Composable
fun LoginScreen(navController: NavController, userRepository: UserRepositoryInterface) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()
    val sharedPrefs = remember { context.getSharedPreferences("login_prefs", MODE_PRIVATE) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }
    var location: Location? by remember { mutableStateOf(null) }

    var hasShownNotificationRequest by remember {
        mutableStateOf(sharedPrefs.getBoolean("notification_permission_requested", false))
    }

    var hasShownLocationRequest by remember {
        mutableStateOf(sharedPrefs.getBoolean("location_permission_requested", false))
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Permesso per le notifiche non concesso", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Login Notifications"
            val descriptionText = "Channel for login notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("login_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!notificationPermissionGranted && sharedPrefs.getBoolean("notification_permission_requested", false)) {
                Toast.makeText(
                    context,
                    "Le notifiche sono disabilitate. Abilitale dalle impostazioni per ricevere aggiornamenti.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun getCityFromLocation(location: Location?): String? {
        location ?: return null
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.get(0)?.locality
        } catch (e: Exception) {
            null
        }
    }

    fun requestNotificationPermission(onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission && !hasShownNotificationRequest) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.POST_NOTIFICATIONS)) {
                    Toast.makeText(context, "L'app necessita del permesso per inviare notifiche", Toast.LENGTH_SHORT).show()
                }
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                sharedPrefs.edit().putBoolean("notification_permission_requested", true).apply()
                hasShownNotificationRequest = true
            } else {
                onPermissionGranted()
            }
        } else {
            onPermissionGranted()
        }
    }

    fun sendLoginNotification(username: String, location: Location?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission) {
                return
            }
        }

        val city = getCityFromLocation(location)
        val locationText = if (city != null) {
            "Login da $city (${location?.latitude}, ${location?.longitude})"
        } else {
            "Login da coordinate (${location?.latitude}, ${location?.longitude})"
        }

        val builder = NotificationCompat.Builder(context, "login_channel")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Login avvenuto")
            .setContentText(locationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(locationText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(2, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "Errore notifica", Toast.LENGTH_SHORT).show()
        }
    }

    val requestLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                location = if (task.isSuccessful && task.result != null) task.result else null
                sendLoginNotification(username, location)
            }
        } else {
            Toast.makeText(context, "Permesso posizione negato", Toast.LENGTH_SHORT).show()
        }
    }

    fun loginUser() {
        if (username.isEmpty() || password.isEmpty()) {
            loginError = "Campi vuoti"
            return
        }

        val trimmedPassword = password.trim()

        // Log prima dell'invio al repository
        Log.d("LoginScreen", "Debug - Tentativo di login con username: $username")
        Log.d("LoginScreen", "Debug - Password inserita (originale): $password")
        Log.d("LoginScreen", "Debug - Password inserita (dopo trim): $trimmedPassword")
        Log.d("LoginScreen", "Debug - Lunghezza password: ${trimmedPassword.length}")

        userRepository.loginUser(username, trimmedPassword, object : UserRepositoryInterface.Callback<LoginResult> {
            override fun onResult(result: LoginResult) {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        when (result) {
                            is LoginResult.Success -> {
                                Log.d("LoginScreen", "Debug - Login riuscito")
                                Log.d("LoginScreen", "Debug - Password hashata nel DB: ${result.user.password}")
                                Log.d("LoginScreen", "Debug - Lunghezza hash nel DB: ${result.user.password.length}")
                                Log.d("LoginScreen", "Debug - Hash inizia con \$2a\$: ${result.user.password.startsWith("\$2a\$")}")

                                Toast.makeText(context, "Login riuscito", Toast.LENGTH_SHORT).show()
                                requestNotificationPermission {
                                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED && !hasShownLocationRequest) {
                                        requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                        sharedPrefs.edit().putBoolean("location_permission_requested", true).apply()
                                        hasShownLocationRequest = true
                                    } else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                        == PackageManager.PERMISSION_GRANTED) {
                                        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                                            location = if (task.isSuccessful && task.result != null) task.result else null
                                            sendLoginNotification(username, location)
                                        }
                                    } else {
                                        // Invia la notifica anche senza la posizione
                                        sendLoginNotification(username, null)
                                    }
                                }
                                navController.navigate("home/${username}") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                            is LoginResult.InvalidCredentials -> {
                                Log.d("LoginScreen", "Debug - Credenziali non valide")
                                loginError = "Credenziali errate"
                            }
                            is LoginResult.UserNotFound -> {
                                Log.d("LoginScreen", "Debug - Utente non trovato nel database")
                                loginError = "Utente non trovato"
                            }
                            is LoginResult.Failure -> {
                                Log.e("LoginScreen", "Debug - Errore durante il login: ${result.errorMessage}")
                                loginError = result.errorMessage
                            }
                        }
                    }
                }
            }
        })
    }

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

        Text(text = "Login a MyMangaList", style = MaterialTheme.typography.headlineSmall)

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

        if (loginError.isNotEmpty()) {
            Text(text = loginError, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { loginUser() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LOGIN")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Non hai un account? Registrati qui",
            color = Color.Blue,
            modifier = Modifier
                .clickable {
                    navController.navigate("registration")
                }
        )
    }
}
