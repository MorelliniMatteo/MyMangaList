package com.example.mymangalist

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.MyMangaList.LoginActivity
import com.example.MyMangaList.RegistrationActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ottieni le SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("MyMangaListPrefs", MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

        // Se è la prima volta che si apre l'app, avvia la RegistrationActivity
        if (isFirstRun) {
            // Imposta isFirstRun a false per le prossime aperture
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()

            // Avvia la schermata di registrazione
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        } else {
            // Avvia la schermata di login se non è la prima volta
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Termina la MainActivity
        finish()
    }
}
