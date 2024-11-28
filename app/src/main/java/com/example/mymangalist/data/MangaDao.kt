package com.example.mymangalist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT * FROM mangas WHERE id = :mangaId LIMIT 1")
    fun getMangaById(mangaId: String): Manga?

    // Nuova query per aggiornare il campo favourite
    @Query("UPDATE mangas SET favourite = :isFavourite WHERE id = :mangaId AND userId = :userId")
    fun updateFavouriteStatus(mangaId: Long, userId: String, isFavourite: Boolean)

    // Nuova query per ottenere i manga preferiti di un utente
    @Query("SELECT * FROM mangas WHERE userId = :userId AND favourite = 1")
    fun getFavouriteMangasByUser(userId: String): List<Manga>
}
