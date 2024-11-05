package com.example.mymangalist.data

import android.app.Application
import com.example.mymangalist.data.MangaDAO
import com.example.mymangalist.data.UserDatabase
import com.example.mymangalist.Manga
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MangaRepository(application: Application) : MangaRepositoryInterface {
    private val mangaDAO: MangaDAO

    init {
        val db: UserDatabase = UserDatabase.getDatabase(application)
        mangaDAO = db.mangaDAO()
    }

    override fun addManga(manga: Manga) { // Aggiungi 'override' qui
        CoroutineScope(Dispatchers.IO).launch {
            mangaDAO.insertManga(manga)
        }
    }

    override fun getMangasByUser(userId: String, callback: (List<Manga>) -> Unit) { // Aggiungi 'override' qui
        CoroutineScope(Dispatchers.IO).launch {
            val mangas = mangaDAO.getMangasByUser(userId)
            callback(mangas)
        }
    }

    fun getMangaCountByUser(userId: String, callback: UserRepositoryInterface.Callback<Int>) {
        CoroutineScope(Dispatchers.IO).launch {
            val count = mangaDAO.getCountByUsername(userId)  // Usa userId come parametro
            callback.onResult(count)
        }
    }
}
