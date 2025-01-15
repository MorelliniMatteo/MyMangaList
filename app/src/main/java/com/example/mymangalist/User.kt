package com.example.mymangalist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "Users",
    primaryKeys = ["username"],
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    val username: String,
    val email: String,
    @ColumnInfo(name = "password", typeAffinity = ColumnInfo.TEXT)
    val password: String,
    val profilePictureUri: String? = null,
    val location: String? = null
)