package com.example.mymangalist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mymangalist.Manga

@Dao
interface MangaDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertManga(manga: Manga)

    @Query("SELECT * FROM mangas WHERE userId = :userId")
    suspend fun getMangasByUser(userId: String): List<Manga>

    @Query("SELECT COUNT(*) FROM mangas WHERE userId = :userId")
    suspend fun getCountByUsername(userId: String): Int

    @Query("SELECT * FROM mangas")
    suspend fun getAllMangas(): List<Manga>

    @Query("SELECT * FROM mangas WHERE id = :mangaId LIMIT 1")
    suspend fun getMangaById(mangaId: String): Manga?

    @Query("UPDATE mangas SET favourite = :isFavourite WHERE id = :mangaId AND userId = :userId")
    suspend fun updateFavouriteStatus(mangaId: Long, userId: String, isFavourite: Boolean)

    @Query("SELECT * FROM mangas WHERE userId = :userId AND favourite = 1")
    suspend fun getFavouriteMangasByUser(userId: String): List<Manga>

    @Query("SELECT * FROM mangas WHERE userId = :userId AND title LIKE :query || '%' ORDER BY insertedDate DESC")
    suspend fun searchByTitle(userId: String, query: String): List<Manga>

    @Query("DELETE FROM mangas WHERE id = :mangaId")
    suspend fun deleteManga(mangaId: String)
}
