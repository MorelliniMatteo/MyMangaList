package com.example.MyMangaList

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
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

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var userRepository: UserRepository

    private val CHANNEL_ID = "login_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        userRepository = UserRepository(application)

        usernameEditText = findViewById(R.id.username_field)
        passwordEditText = findViewById(R.id.password_registration) // Assicurati che l'ID sia corretto
        loginButton = findViewById(R.id.buttonSignIn)

        val signUpTextView: TextView = findViewById(R.id.textViewSignUp)

        // Crea il canale di notifica per il login
        createNotificationChannel()

        // Testo completo con "sign-up" cliccabile
        val text = "Don't have an account? sign-up"
        val spannableString = SpannableString(text)

        val signUpStart = text.indexOf("sign-up")
        val signUpEnd = signUpStart + "sign-up".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
                startActivity(intent)
            }
        }

        spannableString.setSpan(clickableSpan, signUpStart, signUpEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(android.R.color.holo_blue_light)),
            signUpStart, signUpEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        signUpTextView.text = spannableString
        signUpTextView.movementMethod = LinkMovementMethod.getInstance()

        loginButton.setOnClickListener { loginUser() }
    }

    private fun loginUser() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
            return
        }

        UserRepository.getUserByUsername(username) { user ->
            runOnUiThread {
                if (user == null) {
                    Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show()
                } else if (user.password == password) {
                    // Credenziali corrette, invia la notifica e naviga verso la home
                    sendLoginSuccessNotification()
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Login Channel"
            val description = "Channel for login notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendLoginSuccessNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_registration_not) // Usa un'icona appropriata
            .setContentTitle("Login Successful")
            .setContentText("You have successfully logged in to MyMangaList.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(2, builder.build())
    }
}
