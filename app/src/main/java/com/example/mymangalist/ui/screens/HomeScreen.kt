package com.example.mymangalist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mymangalist.R
import com.example.mymangalist.Manga
import com.example.mymangalist.User
import com.example.mymangalist.data.MangaRepository
import com.example.mymangalist.data.UserRepository
import com.example.mymangalist.data.UserRepositoryInterface
import com.example.mymangalist.ui.components.MyMangaBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    userRepository: UserRepository,
    mangaRepository: MangaRepository,
    username: String,
    filter: String = "",
    onMangaClick: (Manga) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var mangaList by remember { mutableStateOf<List<Manga>>(emptyList()) }
    var userId by remember { mutableStateOf("") }
    var showFavorites by remember { mutableStateOf(false) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val filter = currentBackStackEntry?.arguments?.getString("filter") ?: ""

    // Metodo per aggiornare la lista in base alla ricerca
    fun updateMangaList() {
        if (userId.isNotEmpty()) {
            if (searchQuery.isNotEmpty()) {
                mangaRepository.searchMangasByTitle(userId, searchQuery, object : UserRepositoryInterface.Callback<List<Manga>> {
                    override fun onResult(result: List<Manga>) {
                        mangaList = result.sortedByDescending { it.insertedDate }
                    }
                })
            } else if (showFavorites) {
                mangaRepository.getFavouriteMangasByUser(userId, object : UserRepositoryInterface.Callback<List<Manga>> {
                    override fun onResult(result: List<Manga>) {
                        mangaList = result.sortedByDescending { it.insertedDate }
                    }
                })
            } else {
                mangaRepository.getMangasByUser(userId, filter, object : UserRepositoryInterface.Callback<List<Manga>> {
                    override fun onResult(result: List<Manga>) {
                        mangaList = result.sortedByDescending { it.insertedDate }
                    }
                })
            }
        }
    }

    // Carica i manga inizialmente
    LaunchedEffect(Unit) {
        userRepository.getUserByUsername(username, object : UserRepositoryInterface.Callback<User?> {
            override fun onResult(result: User?) {
                result?.let {
                    userId = it.username
                    updateMangaList()
                }
            }
        })
    }

    // Aggiorna la lista quando cambia il filtro, preferiti o la ricerca
    LaunchedEffect(searchQuery, showFavorites, filter) {
        if (userId.isNotEmpty()) {
            updateMangaList()
        } else {
            println("User ID is empty; waiting for user data to load.")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "MyMangaList", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showFavorites = !showFavorites }) {
                        Icon(
                            painter = painterResource(
                                id = if (showFavorites) R.drawable.ic_favorite_filled else R.drawable.ic_favorite
                            ),
                            contentDescription = if (showFavorites) "Favorites filled" else "Favorites",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("filter_screen/$username") }) {
                        Icon(
                            painter = painterResource(
                                id = if (filter.isNotEmpty()) R.drawable.ic_filter_filled else R.drawable.ic_filter
                            ),
                            contentDescription = "Filter",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = { MyMangaBottomBar(navController = navController, username = username) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text("Search manga...") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(mangaList) { manga ->
                        MangaCard(
                            manga = manga,
                            onDetailsClick = { selectedManga -> onMangaClick(selectedManga) },
                            mangaRepository = mangaRepository,
                            userId = userId
                        )
                    }
                }
            }
        }
    )
}
