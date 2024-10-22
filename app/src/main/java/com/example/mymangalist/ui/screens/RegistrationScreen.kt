package com.example.mymangalist.ui.screens

import android.widget.Toast
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
            override fun onResult(result: Boolean) {
                CoroutineScope(Dispatchers.Main).launch {
                    if (result) {
                        Toast.makeText(context, "Username already taken", Toast.LENGTH_SHORT).show()
                    } else {
                        userRepository.isEmailTaken(email, object : UserRepositoryInterface.Callback<Boolean> {
                            override fun onResult(result: Boolean) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    if (result) {
                                        Toast.makeText(context, "Email already taken", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val newUser = User(username, email, password)
                                        userRepository.registerUser(newUser)
                                        Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()

                                        // Naviga alla schermata home
                                        navController.navigate("home")
                                    }
                                }
                            }
                        })
                    }
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
