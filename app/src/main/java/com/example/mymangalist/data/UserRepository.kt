package com.example.mymangalist.Database

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.mymangalist.User

class UserRepository(application: Application) : UserRepositoryInterface {  // Implementa l'interfaccia
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

    override fun isUsernameTaken(username: String, callback: UserRepository.Callback<Boolean>) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = userDAO.findByUsername(username) != null
            callback.onResult(result)
        }
    }

    override fun isEmailTaken(email: String, callback: UserRepository.Callback<Boolean>) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = userDAO.findByEmail(email) != null
            callback.onResult(result)
        }
    }

    override fun loginUser(username: String, password: String, callback: UserRepository.Callback<User?>) {
        CoroutineScope(Dispatchers.IO).launch {
            val user: User? = userDAO.login(username, password)
            callback.onResult(user)
        }
    }

    override fun getUserByUsername(username: String, callback: UserRepository.Callback<User?>) {
        CoroutineScope(Dispatchers.IO).launch {
            val user: User? = userDAO.getUserByUsername(username)
            callback.onResult(user)
        }
    }

    interface Callback<T> {
        fun onResult(result: T)
    }
}
