package com.example.mymangalist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mymangalist.User

@Dao
interface UserDAO {

    // Funzione di inserimento con parametro non-null
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Cambia qui se necessario
    suspend fun insertUser(user: User)

    // Query per il login, username e password non-null
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    // Trova un utente per username
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    // Trova un utente per email (email già non-null)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("UPDATE users SET profilePictureUri = :pictureUri WHERE username = :username")
    suspend fun updateProfilePicture(username: String, pictureUri: String)

    @Query("UPDATE users SET location = :location WHERE username = :username")
    suspend fun updateLocation(username: String, location: String)
}
