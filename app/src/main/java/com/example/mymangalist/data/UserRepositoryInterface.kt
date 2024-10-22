package com.example.mymangalist.data

import com.example.mymangalist.User

interface UserRepositoryInterface {
    fun isUsernameTaken(username: String, callback: Callback<Boolean>)
    fun isEmailTaken(email: String, callback: Callback<Boolean>)
    fun registerUser(user: User)
    fun loginUser(username: String, password: String, callback: Callback<User?>)
    fun getUserByUsername(username: String, callback: Callback<User?>)

    interface Callback<T> {
        fun onResult(result: T)
    }
}
