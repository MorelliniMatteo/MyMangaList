package com.example.MyMangaList

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mymangalist.Database.UserRepository


class RegistrationActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var signInTextView: TextView

    private lateinit var userRepository: UserRepository

    private val CHANNEL_ID = "registration_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_page)

        // Inizializza il repository
        userRepository = UserRepository(application)

        // Collega i componenti UI
        usernameEditText = findViewById(R.id.username_field)
        emailEditText = findViewById(R.id.email_field)
        passwordEditText = findViewById(R.id.password_registration)
        confirmPasswordEditText = findViewById(R.id.confirm_pass)
        registerButton = findViewById(R.id.buttonSignUp)
        signInTextView = findViewById(R.id.textViewSignIn)

        registerButton.setOnClickListener { registerUser() }

        // Crea il canale di notifica
        createNotificationChannel()

        // Imposta il testo cliccabile per la registrazione
        setUpSignInTextView()
    }

    private fun setUpSignInTextView() {
        val signInText = "Do you already have an account? Sign in"
        val spannableString = SpannableString(signInText)

        val signInStart = signInText.indexOf("Sign in")
        val signInEnd = signInStart + "Sign in".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Avvia la LoginActivity
                val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
                startActivity(intent)
                finish() // Chiudi questa activity
            }
        }

        spannableString.setSpan(clickableSpan, signInStart, signInEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(android.R.color.holo_blue_light)),
            signInStart, signInEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        signInTextView.text = spannableString
        signInTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun registerUser() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        // Controlla se i campi sono vuoti
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se le password coincidono
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Controlla se l'username è già preso
        userRepository.isUsernameTaken(username) { isTaken ->
            runOnUiThread {
                if (isTaken) {
                    Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show()
                } else {
                    // Controlla se l'email è già presa
                    userRepository.isEmailTaken(email) { isEmailTaken ->
                        if (isEmailTaken) {
                            Toast.makeText(this, "Email already taken", Toast.LENGTH_SHORT).show()
                        } else {
                            // Registra il nuovo utente
                            val newUser = User(username, email, password)
                            userRepository.registerUser(newUser)

                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                            sendRegistrationSuccessNotification()

                            // Avvia la HomeActivity
                            val intent = Intent(this@RegistrationActivity, HomeActivity::class.java)
                            startActivity(intent)

                            // Chiudi l'activity di registrazione
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        // Crea un canale di notifica per Android 8.0 e versioni successive
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Registration Channel"
            val description = "Channel for registration notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendRegistrationSuccessNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_registration_not) // L'icona della notifica
            .setContentTitle("Registration Successful")
            .setContentText("You have successfully registered to MyMangaList.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(1, builder.build())
    }
}
