package com.example.mymangalist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mymangalist.Manga

@Dao
interface MangaDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertManga(manga: Manga)

    @Query("SELECT * FROM mangas WHERE userId = :userId")
    fun getMangasByUser(userId: String): List<Manga>

    @Query("SELECT COUNT(*) FROM mangas WHERE userId = :userId")
    fun getCountByUsername(userId: String): Int

    @Query("SELECT * FROM mangas")
    fun getAllMangas(): List<Manga>

    // Aggiungi il metodo per ottenere un manga tramite ID
    @Query("SELECT * FROM mangas WHERE id = :mangaId LIMIT 1")
    fun getMangaById(mangaId: String): Manga?
}
