package com.example.mymangalist

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "Users",
    primaryKeys = ["username"],
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    val username: String, // PrimaryKey univoca
    val email: String,
    val password: String,
    val profilePictureUri: String? = null, // Uri della foto profilo
    val location: String? = null           // Posizione dell'utente
)
