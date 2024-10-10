// UserRepositoryInterface.kt
package com.example.mymangalist.Database

import com.example.mymangalist.User

interface UserRepositoryInterface {
    fun registerUser(user: User)
    fun isUsernameTaken(username: String, callback: UserRepository.Callback<Boolean>)
    fun isEmailTaken(email: String, callback: UserRepository.Callback<Boolean>)
    fun loginUser(username: String, password: String, callback: UserRepository.Callback<User?>)
    fun getUserByUsername(username: String, callback: UserRepository.Callback<User?>)
}
