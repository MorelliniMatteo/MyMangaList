package com.example.mymangalist.data

import com.example.mymangalist.User

// Definisci un risultato di login
sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    object InvalidCredentials : LoginResult()
    object UserNotFound : LoginResult()
    object Error : LoginResult() // Altri errori generali
}
