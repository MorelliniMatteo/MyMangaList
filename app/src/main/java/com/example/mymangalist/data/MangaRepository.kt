package com.example.mymangalist.data

import android.app.Application
import com.example.mymangalist.Manga
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MangaRepository(application: Application) : MangaRepositoryInterface {
    private val mangaDAO: MangaDAO

    init {
        val db: UserDatabase = UserDatabase.getDatabase(application)
        mangaDAO = db.mangaDAO()
    }

    override fun addManga(manga: Manga) {
        CoroutineScope(Dispatchers.IO).launch {
            mangaDAO.insertManga(manga)
        }
    }

    override fun getMangasByUser(userId: String, callback: UserRepositoryInterface.Callback<List<Manga>>) {
        CoroutineScope(Dispatchers.IO).launch {
            val mangas = mangaDAO.getMangasByUser(userId)
            withContext(Dispatchers.Main) {
                callback.onResult(mangas)
            }
        }
    }

    fun getMangaCountByUser(userId: String, callback: UserRepositoryInterface.Callback<Int>) {
        CoroutineScope(Dispatchers.IO).launch {
            val count = mangaDAO.getCountByUsername(userId)
            callback.onResult(count)
        }
    }

    fun getAllMangasSortedByDate(callback: (List<Manga>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val mangas = mangaDAO.getAllMangas().sortedByDescending { it.insertedDate }
            callback(mangas)
        }
    }
}
