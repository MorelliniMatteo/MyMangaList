package com.example.mymangalist.data

import com.example.mymangalist.Manga

interface MangaRepositoryInterface {
    fun addManga(manga: Manga)
    fun getMangasByUser(userId: String, filter: String?, callback: UserRepositoryInterface.Callback<List<Manga>>)
    fun getMangaCountByUser(userId: String, callback: UserRepositoryInterface.Callback<Int>)
    suspend fun getAllMangasSortedByDate(): List<Manga>
    suspend fun getMangaById(mangaId: String): Manga?

    // Nuova funzione per aggiornare lo stato di favourite
    fun updateFavouriteStatus(mangaId: Long, userId: String, isFavourite: Boolean)

    fun searchMangasByTitle(userId: String, query: String, callback: UserRepositoryInterface.Callback<List<Manga>>)

    // Nuova funzione per ottenere i manga preferiti di un utente
    fun getFavouriteMangasByUser(userId: String, callback: UserRepositoryInterface.Callback<List<Manga>>)
}
