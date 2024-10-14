package com.example.MyMangaList

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mymangalist.Database.UserRepository
import com.example.mymangalist.Database.UserRepositoryInterface
import com.example.mymangalist.R
import com.example.mymangalist.ui.home.HomeActivity

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp


class LoginActivity : ComponentActivity() {
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepository = UserRepository(application)  // Inizializza il repository
        setContent {
            LoginScreen(userRepository)
        }
    }
}

@Composable
fun LoginScreen(userRepository: UserRepositoryInterface) {
    val context = LocalContext.current

    // State per i campi di input
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Funzione per gestire il login con username e password
    fun loginUser() {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        userRepository.loginUser(username, password, object : UserRepository.Callback<com.example.mymangalist.User?> {
            override fun onResult(user: com.example.mymangalist.User?) {
                if (user != null) {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, HomeActivity::class.java)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Invalid username or password", Toast.LENGTH_SHORT).show()
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { loginUser() },
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
                    val intent = Intent(context, RegistrationActivity::class.java)
                    context.startActivity(intent)
                }
        )
    }
}
