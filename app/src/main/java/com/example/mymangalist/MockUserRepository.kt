package com.example.mymangalist

import com.example.mymangalist.data.UserRepositoryInterface
import com.example.mymangalist.data.LoginResult // Assicurati di importare LoginResult

// Classe MockUserRepository
class MockUserRepository : UserRepositoryInterface {

    override fun registerUser(user: User) {
        // Non fare nulla, questa è una mock implementation
    }

    override fun isUsernameTaken(username: String, callback: UserRepositoryInterface.Callback<Boolean>) {
        // Simula che il nome utente non sia già preso
        callback.onResult(false)  // Modifica questo valore per simulare comportamenti diversi
    }

    override fun isEmailTaken(email: String, callback: UserRepositoryInterface.Callback<Boolean>) {
        // Simula che l'email non sia già presa
        callback.onResult(false)  // Modifica questo valore per simulare comportamenti diversi
    }

    override fun loginUser(username: String, password: String, callback: UserRepositoryInterface.Callback<LoginResult>) {
        // Simula il login fallito
        callback.onResult(LoginResult.InvalidCredentials) // Simula un login fallito
    }

    override fun getUserByUsername(username: String, callback: UserRepositoryInterface.Callback<User?>) {
        callback.onResult(null) // Simula che l'utente non venga trovato
    }
}
