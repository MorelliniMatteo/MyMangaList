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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.example.mymangalist.Database.MockUserRepository
import com.example.mymangalist.Database.UserRepository
import com.example.mymangalist.Database.UserRepositoryInterface
import com.example.mymangalist.R
import com.example.mymangalist.User
import com.example.mymangalist.ui.home.HomeActivity
import androidx.compose.material3.*

class RegistrationActivity : ComponentActivity() {
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepository = UserRepository(application)  // Inizializza il repository
        setContent {
            RegistrationScreen(userRepository)
        }
    }
}

@Composable
fun RegistrationScreen(userRepository: UserRepositoryInterface) {  // Cambia il tipo qui
    val context = LocalContext.current

    // State per i campi di input
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

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

        userRepository.isUsernameTaken(username, object : UserRepository.Callback<Boolean> {
            override fun onResult(isTaken: Boolean) {
                if (isTaken) {
                    Toast.makeText(context, "Username already taken", Toast.LENGTH_SHORT).show()
                } else {
                    userRepository.isEmailTaken(email, object : UserRepository.Callback<Boolean> {
                        override fun onResult(isEmailTaken: Boolean) {
                            if (isEmailTaken) {
                                Toast.makeText(context, "Email already taken", Toast.LENGTH_SHORT).show()
                            } else {
                                val newUser = User(username, email, password)
                                userRepository.registerUser(newUser)
                                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()

                                val intent = Intent(context, HomeActivity::class.java)
                                context.startActivity(intent)
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
            Text("REGISTRATI")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Do you already have an account? Sign in",
            color = Color.Blue,
            modifier = Modifier
                .clickable {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                }
        )
    }
}

