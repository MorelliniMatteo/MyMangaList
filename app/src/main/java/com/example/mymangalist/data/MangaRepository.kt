package com.example.mymangalist.data

import android.app.Application
import com.example.mymangalist.Manga
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MangaRepository(application: Application) : MangaRepositoryInterface {
    private val mangaDAO: MangaDAO

    init {
        val db = UserDatabase.getDatabase(application)
        mangaDAO = db.mangaDAO()
    }

    override fun addManga(manga: Manga) {
        CoroutineScope(Dispatchers.IO).launch {
            mangaDAO.insertManga(manga)
        }
    }

    override fun getMangasByUser(
        userId: String,
        filter: String?,
        callback: UserRepositoryInterface.Callback<List<Manga>>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val mangas = mangaDAO.getMangasByUser(userId)
            println("Mangas from DB: $mangas")  // Log per verificare i manga estratti dal DB

            // Applicare il filtro se presente
            val filteredMangas = if (filter.isNullOrEmpty()) {
                mangas // Nessun filtro, restituisce tutti i manga
            } else {
                mangas.filter { it.category.equals(filter, ignoreCase = true) }
            }

            withContext(Dispatchers.Main) {
                println("Filtered mangas: $filteredMangas")  // Log per verificare i manga filtrati
                callback.onResult(filteredMangas)
            }
        }
    }

    override fun getMangaCountByUser(userId: String, callback: UserRepositoryInterface.Callback<Int>) {
        CoroutineScope(Dispatchers.IO).launch {
            val count = mangaDAO.getCountByUsername(userId)
            withContext(Dispatchers.Main) {
                callback.onResult(count)
            }
        }
    }

    override suspend fun getAllMangasSortedByDate(): List<Manga> = withContext(Dispatchers.IO) {
        mangaDAO.getAllMangas().sortedByDescending { it.insertedDate }
    }

    override suspend fun getMangaById(mangaId: String): Manga? = withContext(Dispatchers.IO) {
        mangaDAO.getMangaById(mangaId)
    }

    // Nuova funzione per aggiornare lo stato di favourite
    override fun updateFavouriteStatus(mangaId: Long, userId: String, isFavourite: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            mangaDAO.updateFavouriteStatus(mangaId, userId, isFavourite)
        }
    }

    // Nuova funzione per ottenere i manga preferiti di un utente
    override fun getFavouriteMangasByUser(
        userId: String,
        callback: UserRepositoryInterface.Callback<List<Manga>>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val favouriteMangas = mangaDAO.getFavouriteMangasByUser(userId)
            withContext(Dispatchers.Main) {
                callback.onResult(favouriteMangas)
            }
        }
    }

    override fun searchMangasByTitle(userId: String, query: String, callback: UserRepositoryInterface.Callback<List<Manga>>) {
        CoroutineScope(Dispatchers.IO).launch {
            val filteredMangas = mangaDAO.searchByTitle(userId, query)
            withContext(Dispatchers.Main) {
                callback.onResult(filteredMangas)
            }
        }
    }
}
