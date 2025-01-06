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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.location.Geocoder
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult

@Composable
fun LoginScreen(navController: NavController, userRepository: UserRepositoryInterface) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }
    var location: Location? by remember { mutableStateOf(null) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        var hasNotificationPermission = isGranted
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Login Notifications"
            val descriptionText = "Channel for login notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("login_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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
            if (!hasNotificationPermission) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onPermissionGranted()
            }
        } else {
            onPermissionGranted()
        }
    }

    fun sendLoginNotification(username: String, location: Location?) {
        if (!hasNotificationPermission) {
            return
        }

        val city = getCityFromLocation(location)
        val locationText = if (city != null) {
            "Login avvenuto con successo da $city (${location?.latitude}, ${location?.longitude})"
        } else {
            "Login avvenuto con successo da coordinate (${location?.latitude}, ${location?.longitude})"
        }

        val builder = NotificationCompat.Builder(context, "login_channel")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Login avvenuto con successo")
            .setContentText(locationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(locationText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(2, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "Errore nell'invio della notifica", Toast.LENGTH_SHORT).show()
        }
    }

    fun loginUser(location: Location?) {
        if (username.isEmpty() || password.isEmpty()) {
            loginError = "Tutti i campi devono essere riempiti"
            return
        }

        userRepository.loginUser(username, password, object : UserRepositoryInterface.Callback<LoginResult> {
            override fun onResult(result: LoginResult) {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        when (result) {
                            is LoginResult.Success -> {
                                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                requestNotificationPermission {
                                    sendLoginNotification(username, location)
                                }
                                navController.navigate("home/${username}") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                            is LoginResult.InvalidCredentials -> {
                                loginError = "Username o Password errati"
                            }
                            is LoginResult.UserNotFound -> {
                                loginError = "Utente non trovato"
                            }
                            is LoginResult.Failure -> {
                                loginError = result.errorMessage
                            }
                        }
                    }
                }
            }
        })


    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Login Notifications"
            val descriptionText = "Channel for login notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("login_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getLocationAndLogin() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(context as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1002
            )
            return
        }

        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            location = if (task.isSuccessful && task.result != null) task.result else null
            loginUser(location)
        }
    }

    DisposableEffect(context) {
        val activity = context as Activity
        val permissionCallback = { requestCode: Int, permissions: Array<String>, grantResults: IntArray ->
            if (requestCode == 1003) {
                hasNotificationPermission = grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
            }
        }

        onDispose {
            // Cleanup if needed
        }
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
            onClick = { getLocationAndLogin() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LOGIN")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Non hai un account? Registrati cliccando qui",
            color = Color.Blue,
            modifier = Modifier
                .clickable {
                    navController.navigate("registration")
                }
        )
    }
}


