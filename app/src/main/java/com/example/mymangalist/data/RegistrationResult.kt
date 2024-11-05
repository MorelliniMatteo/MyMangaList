package com.example.mymangalist.data

sealed class RegistrationResult {
    data class Success(val message: String) : RegistrationResult()
    data class Failure(val errorMessage: String) : RegistrationResult()
}
