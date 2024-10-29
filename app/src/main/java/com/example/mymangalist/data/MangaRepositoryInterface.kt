package com.example.mymangalist.data

import com.example.mymangalist.Manga

interface MangaRepositoryInterface {
    fun addManga(manga: Manga)
    fun getMangasByUser(userId: String, callback: (List<Manga>) -> Unit)
}
