package com.example.mymangalist.data

import android.app.Application
import android.util.Log
import com.example.mymangalist.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

class UserRepository(application: Application) : UserRepositoryInterface {
    private val userDAO: UserDAO

    init {
        val db: UserDatabase = UserDatabase.getDatabase(application)
        userDAO = db.userDAO()
    }

    override fun registerUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("UserRepository", "Debug Registrazione - Password originale: ${user.password}")
                val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
                Log.d("UserRepository", "Debug Registrazione - Password hashata: $hashedPassword")

                val userWithHashedPassword = user.copy(password = hashedPassword)
                userDAO.insertUser(userWithHashedPassword)

                // Verifica immediata dopo il salvataggio
                val savedUser = userDAO.getUserByUsername(user.username)
                Log.d("UserRepository", "Debug Registrazione - Password salvata nel DB: ${savedUser?.password}")

                // Test di verifica immediato
                val testVerify = BCrypt.checkpw(user.password, savedUser?.password)
                Log.d("UserRepository", "Debug Registrazione - Test verifica immediata: $testVerify")

            } catch (e: Exception) {
                Log.e("UserRepository", "Debug Registrazione - Errore: ${e.message}")
                Log.e("UserRepository", "Debug Registrazione - Stacktrace: ${e.stackTraceToString()}")
            }
        }
    }

    override fun loginUser(username: String, password: String, callback: UserRepositoryInterface.Callback<LoginResult>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("UserRepository", "Debug - Inizio processo di login")
                Log.d("UserRepository", "Debug - Username ricevuto: $username")
                Log.d("UserRepository", "Debug - Password ricevuta (non criptata): $password")

                val user = userDAO.getUserByUsername(username)
                Log.d("UserRepository", "Debug - Utente trovato nel DB: ${user != null}")

                withContext(Dispatchers.Main) {
                    if (user != null) {
                        Log.d("UserRepository", "Debug - Hash password nel DB: ${user.password}")
                        Log.d("UserRepository", "Debug - Hash lunghezza: ${user.password.length}")
                        Log.d("UserRepository", "Debug - Hash formato corretto: ${user.password.startsWith("\$2a\$")}")

                        try {
                            val matches = BCrypt.checkpw(password, user.password)
                            Log.d("UserRepository", "Debug - Risultato verifica BCrypt: $matches")

                            if (matches) {
                                Log.d("UserRepository", "Debug - Password verificata con successo")
                                callback.onResult(LoginResult.Success(user))
                            } else {
                                Log.d("UserRepository", "Debug - Password non corrispondente")
                                callback.onResult(LoginResult.InvalidCredentials)
                            }
                        } catch (e: Exception) {
                            Log.e("UserRepository", "Debug - Errore durante verifica BCrypt: ${e.message}")
                            Log.e("UserRepository", "Debug - Stacktrace: ${e.stackTraceToString()}")
                            callback.onResult(LoginResult.Failure(e.message ?: "Errore sconosciuto"))
                        }
                    } else {
                        Log.d("UserRepository", "Debug - Utente non trovato")
                        callback.onResult(LoginResult.UserNotFound)
                    }
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Debug - Errore generale: ${e.message}")
                Log.e("UserRepository", "Debug - Stacktrace: ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    callback.onResult(LoginResult.Failure(e.message ?: "Errore sconosciuto"))
                }
            }
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
}