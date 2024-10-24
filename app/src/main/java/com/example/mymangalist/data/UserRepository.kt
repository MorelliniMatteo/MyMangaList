package com.example.mymangalist.data

import android.app.Application
import com.example.mymangalist.Database.UserDAO
import com.example.mymangalist.Database.UserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.mymangalist.User

class UserRepository(application: Application) : UserRepositoryInterface {
    private val userDAO: UserDAO

    init {
        val db: UserDatabase = UserDatabase.getDatabase(application)
        userDAO = db.userDAO()
    }

    override fun registerUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            userDAO.insertUser(user)
        }
    }

    override fun isUsernameTaken(username: String, callback: UserRepositoryInterface.Callback<Boolean>) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = userDAO.findByUsername(username) != null
            callback.onResult(result)
        }
    }

    override fun isEmailTaken(email: String, callback: UserRepositoryInterface.Callback<Boolean>) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = userDAO.findByEmail(email) != null
            callback.onResult(result)
        }
    }

    override fun loginUser(username: String, password: String, callback: UserRepositoryInterface.Callback<LoginResult>) {
        CoroutineScope(Dispatchers.IO).launch {
            // Qui puoi eseguire il login e restituire un LoginResult
            val user: User? = userDAO.login(username, password)
            if (user != null) {
                // Login riuscito
                callback.onResult(LoginResult.Success(user))
            } else {
                // Se l'utente non è trovato, puoi restituire un LoginResult specifico
                callback.onResult(LoginResult.InvalidCredentials)
            }
        }
    }

    override fun getUserByUsername(username: String, callback: UserRepositoryInterface.Callback<User?>) {
        CoroutineScope(Dispatchers.IO).launch {
            val user: User? = userDAO.getUserByUsername(username)
            callback.onResult(user)
        }
    }
}
