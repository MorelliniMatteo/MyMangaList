import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import android.content.Context.MODE_PRIVATE
import org.mindrot.jbcrypt.BCrypt

@Composable
fun RegistrationScreen(navController: NavController, userRepository: UserRepositoryInterface) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var hasShownNotificationRequest by remember {
        mutableStateOf(sharedPrefs.getBoolean("notification_permission_requested", false))
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
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
            Toast.makeText(context, "Permesso notifiche negato", Toast.LENGTH_SHORT).show()
        }
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Registrazione notifiche"
            val descriptionText = "Canale notifiche di registrazione"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("welcome_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendWelcomeNotification(username: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            return
        }

        createNotificationChannel()

        val builder = NotificationCompat.Builder(context, "welcome_channel")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Registrazione riuscita")
            .setContentText("Ciao $username, benvenuto in MyMangaList!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(context).notify(1, builder.build())
    }

    fun registerUser() {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(context, "Tutti i campi devono essere riempiti", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(context, "Le password non coincidono", Toast.LENGTH_SHORT).show()
            return
        }

        userRepository.isUsernameTaken(username, object : UserRepositoryInterface.Callback<Boolean> {
            override fun onResult(isTaken: Boolean) {
                if (isTaken) {
                    Toast.makeText(context, "Username già in uso", Toast.LENGTH_SHORT).show()
                } else {
                    userRepository.isEmailTaken(email, object : UserRepositoryInterface.Callback<Boolean> {
                        override fun onResult(isTaken: Boolean) {
                            if (isTaken) {
                                Toast.makeText(context, "Email già in uso", Toast.LENGTH_SHORT).show()
                            } else {
                                // Passa la password non criptata al repository
                                val newUser = User(username, email, password)
                                userRepository.registerUser(newUser)
                                Toast.makeText(context, "Registrazione avvenuta con successo", Toast.LENGTH_SHORT).show()

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (!hasNotificationPermission && !hasShownNotificationRequest) {
                                        if (ActivityCompat.shouldShowRequestPermissionRationale(context as ComponentActivity, Manifest.permission.POST_NOTIFICATIONS)) {
                                            Toast.makeText(context, "Permesso notifiche necessario", Toast.LENGTH_SHORT).show()
                                        }
                                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        sharedPrefs.edit().putBoolean("notification_permission_requested", true).apply()
                                        hasShownNotificationRequest = true
                                    } else {
                                        sendWelcomeNotification(username)
                                    }
                                } else {
                                    sendWelcomeNotification(username)
                                }
                                navController.navigate("home/${newUser.username}")
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

        Text(text = "Registrati a MyMangaList", style = MaterialTheme.typography.headlineSmall)

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
            label = { Text("Conferma Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { registerUser() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("REGISTRATI")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Possiedi giá un account? Esegui il login qui",
            color = Color.Blue,
            modifier = Modifier
                .clickable {
                    navController.navigate("login")
                }
        )
    }
}
