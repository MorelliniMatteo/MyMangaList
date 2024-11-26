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
import com.example.mymangalist.R
import com.example.mymangalist.Manga
import com.example.mymangalist.User
import com.example.mymangalist.data.MangaRepository
import com.example.mymangalist.data.UserRepository
import com.example.mymangalist.data.UserRepositoryInterface
import com.example.mymangalist.ui.screens.MyMangaBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    userRepository: UserRepository,
    mangaRepository: MangaRepository,
    username: String,
    onMangaClick: (Manga) -> Unit // Aggiungi questo parametro
) {
    var searchQuery by remember { mutableStateOf("") }
    var mangaList by remember { mutableStateOf<List<Manga>>(emptyList()) }

    // Ottieni l'utente loggato
    var userId by remember { mutableStateOf("") }

    // Carica l'ID dell'utente e i manga corrispondenti
    LaunchedEffect(Unit) {
        userRepository.getUserByUsername(username, object : UserRepositoryInterface.Callback<User?> {
            override fun onResult(user: User?) {
                user?.let {
                    userId = it.username
                    mangaRepository.getMangasByUser(userId, object : UserRepositoryInterface.Callback<List<Manga>> {
                        override fun onResult(mangas: List<Manga>) {
                            mangaList = mangas
                        }
                    })
                }
            }
        })
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
                    IconButton(onClick = { /* Naviga ai preferiti */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_favorite),
                            contentDescription = "Favorites"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Filtra i manga */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter),
                            contentDescription = "Filter"
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
                // Barra di ricerca
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text("Cerca manga...") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lista dei manga filtrati
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(mangaList.filter { it.title.contains(searchQuery, ignoreCase = true) }) { manga ->
                        MangaCard(
                            manga = manga,
                            onDetailsClick = { selectedManga ->
                                // Quando un manga viene cliccato, chiama onMangaClick
                                onMangaClick(selectedManga)
                            }
                        )
                    }
                }
            }
        }
    )
}
