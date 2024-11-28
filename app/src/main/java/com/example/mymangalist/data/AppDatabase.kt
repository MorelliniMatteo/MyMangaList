package com.example.mymangalist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mymangalist.User
import com.example.mymangalist.Manga

@Database(entities = [User::class, Manga::class], version = 7)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDAO
    abstract fun mangaDao(): MangaDAO
}
