package com.example.mymangalist.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mymangalist.User

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUser(user: User?)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    fun login(username: String?, password: String?): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun findByUsername(username: String?): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUserByUsername(username: String?): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun findByEmail(email: String): User?
}
