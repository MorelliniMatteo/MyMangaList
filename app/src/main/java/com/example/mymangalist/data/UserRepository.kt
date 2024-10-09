package com.example.mymangalist.Database

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRepository(application: Application?) {
    private val userDAO: UserDAO

    init {
        val db: UserDatabase = UserDatabase.getDatabase(application!!)
        userDAO = db.userDAO()
    }

    // Registrazione utente, eseguita in background usando coroutines
    fun registerUser(user: User?) {
        CoroutineScope(Dispatchers.IO).launch {
            userDAO.insertUser(user)
        }
    }

    // Verifica se un nome utente esiste
    fun isUsernameTaken(username: String?, callback: Callback<Boolean?>) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = userDAO.findByUsername(username) != null
            callback.onResult(result)
        }
    }

    // Login utente
    fun loginUser(username: String?, password: String?, callback: Callback<User?>) {
        CoroutineScope(Dispatchers.IO).launch {
            val user: User? = userDAO.login(username, password)
            callback.onResult(user)
        }
    }

    // Ottieni utente tramite username
    fun getUserByUsername(username: String?, callback: Callback<User?>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val user: User? = userDAO.getUserByUsername(username)
            callback?.onResult(user)
        }
    }

    interface Callback<T> {
        fun onResult(result: T)
    }
}
