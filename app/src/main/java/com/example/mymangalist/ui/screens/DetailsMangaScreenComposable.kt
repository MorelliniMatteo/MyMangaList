package com.example.mymangalist.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mymangalist.Manga
import com.example.mymangalist.data.MangaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun DetailsMangaScreenComposable(
    mangaId: String,
    mangaRepository: MangaRepository,
    navController: NavController, // Parametro aggiunto
    username: String,             // Parametro aggiunto
    onBackClick: () -> Unit
) {
    val viewModel: DetailsMangaViewModel = viewModel(
        factory = DetailsMangaViewModelFactory(mangaId, mangaRepository)
    )
    val manga by viewModel.manga.collectAsState()

    manga?.let {
        DetailsMangaScreen(
            manga = it,
            onBackClick = onBackClick,
            navController = navController, // Passaggio del NavController
            username = username            // Passaggio del nome utente
        )
    }
}

class DetailsMangaViewModel(
    mangaId: String,
    private val mangaRepository: MangaRepository
) : ViewModel() {
    private val _manga = MutableStateFlow<Manga?>(null)
    val manga: StateFlow<Manga?> = _manga

    init {
        fetchMangaDetails(mangaId)
    }

    private fun fetchMangaDetails(mangaId: String) {
        viewModelScope.launch {
            _manga.value = mangaRepository.getMangaById(mangaId)
        }
    }
}

class DetailsMangaViewModelFactory(
    private val mangaId: String,
    private val mangaRepository: MangaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsMangaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailsMangaViewModel(mangaId, mangaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
