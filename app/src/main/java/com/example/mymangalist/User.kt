package com.example.mymangalist

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "Users",
    primaryKeys = ["username"],
    indices = [Index(value = ["email"], unique = true)])
data class User(
    val username: String, // PrimaryKey univoca
    val email: String,    // Email univoca
    val password: String
)
