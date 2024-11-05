package com.example.mymangalist.data

import android.app.Application
import com.example.mymangalist.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            val result = userDAO.getUserByUsername(username) != null
            withContext(Dispatchers.Main) {
                callback.onResult(result)
            }
        }
    }

    override fun isEmailTaken(email: String, callback: UserRepositoryInterface.Callback<Boolean>) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = userDAO.findByEmail(email) != null
            withContext(Dispatchers.Main) {
                callback.onResult(result)
            }
        }
    }

    // Assicurati che anche getUserByUsername utilizzi coroutine
    override fun getUserByUsername(username: String, callback: UserRepositoryInterface.Callback<User?>) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = userDAO.getUserByUsername(username)
            withContext(Dispatchers.Main) {
                callback.onResult(user)
            }
        }
    }

    // Metodo per aggiornare la foto del profilo
    fun updateProfilePicture(username: String, pictureUri: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userDAO.updateProfilePicture(username, pictureUri)
        }
    }

    // Metodo per aggiornare la posizione
    fun updateLocation(username: String, location: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userDAO.updateLocation(username, location)
        }
    }

    fun getUser(username: String, callback: UserRepositoryInterface.Callback<User?>) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = userDAO.getUserByUsername(username) // Supponendo che questa funzione ritorni User?
            withContext(Dispatchers.Main) {
                callback.onResult(user)
            }
        }
    }

    override fun loginUser(username: String, password: String, callback: UserRepositoryInterface.Callback<LoginResult>) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = userDAO.login(username, password)
            withContext(Dispatchers.Main) {
                if (user != null) {
                    callback.onResult(LoginResult.Success(user))
                } else {
                    // Puoi anche implementare controlli per InvalidCredentials o UserNotFound
                    callback.onResult(LoginResult.InvalidCredentials) // o LoginResult.UserNotFound
                }
            }
        }
    }



}

