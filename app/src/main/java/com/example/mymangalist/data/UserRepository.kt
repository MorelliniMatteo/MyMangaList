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

    override fun getUserByUsername(username: String, callback: UserRepositoryInterface.Callback<User?>) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = userDAO.getUserByUsername(username)
            withContext(Dispatchers.Main) {
                callback.onResult(user)
            }
        }
    }

    fun updateProfilePicture(username: String, pictureUri: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userDAO.updateProfilePicture(username, pictureUri)
        }
    }

    fun updateLocation(username: String, location: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userDAO.updateLocation(username, location)
        }
    }

    fun getAllUsers(callback: UserRepositoryInterface.Callback<List<User>>) {
        CoroutineScope(Dispatchers.IO).launch {
            val users = userDAO.getAllUsers()
            withContext(Dispatchers.Main) {
                callback.onResult(users)
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
                    callback.onResult(LoginResult.InvalidCredentials)
                }
            }
        }
    }
}
