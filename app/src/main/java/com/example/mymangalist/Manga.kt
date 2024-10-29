package com.example.mymangalist

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "mangas",
    foreignKeys = [
        ForeignKey(entity = User::class,
            parentColumns = ["username"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE)
    ]
)
data class Manga(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val price: Double,
    val date: String,
    val category: String,
    val imageUrl: String,
    val description: String,
    val purchaseLocation: String,
    val userId: String // Questo dovrebbe essere il username dell'utente
)
