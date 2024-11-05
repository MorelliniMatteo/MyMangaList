package com.example.mymangalist.data

import com.example.mymangalist.User

// Definisci un risultato di login
sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Failure(val errorMessage: String) : LoginResult() // Gestione degli errori
    object InvalidCredentials : LoginResult() // Errori specifici
    object UserNotFound : LoginResult() // Errore quando l'utente non è trovato
    // Puoi considerare di rimuovere 'Error' se non hai bisogno di gestire errori generali
}
