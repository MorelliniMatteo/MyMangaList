package com.example.mymangalist.Database

import com.example.mymangalist.User

// Classe MockUserRepository
class MockUserRepository : UserRepositoryInterface {

    override fun registerUser(user: User) {
        // Non fare nulla, questa è una mock implementation
    }

    override fun isUsernameTaken(username: String, callback: UserRepository.Callback<Boolean>) {
        // Simula che il nome utente non sia già preso
        callback.onResult(false)  // Modifica questo valore per simulare comportamenti diversi
    }

    override fun isEmailTaken(email: String, callback: UserRepository.Callback<Boolean>) {
        // Simula che l'email non sia già presa
        callback.onResult(false)  // Modifica questo valore per simulare comportamenti diversi
    }

    override fun loginUser(username: String, password: String, callback: UserRepository.Callback<User?>) {
        callback.onResult(null) // Simula il login fallito
    }

    override fun getUserByUsername(username: String, callback: UserRepository.Callback<User?>) {
        callback.onResult(null) // Simula che l'utente non venga trovato
    }
}
