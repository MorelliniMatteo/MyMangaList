package com.example.mymangalist.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mymangalist.R
import com.example.mymangalist.User
import com.example.mymangalist.data.MangaRepository
import com.example.mymangalist.data.UserRepository
import com.example.mymangalist.data.UserRepositoryInterface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavController,
    userRepository: UserRepository,
    mangaRepository: MangaRepository,
    context: Context,
    username: String
) {
    var users by remember { mutableStateOf<List<Pair<User, Int>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        userRepository.getAllUsers(object : UserRepositoryInterface.Callback<List<User>> {
            override fun onResult(userList: List<User>) {
                // Lista per tenere traccia dei risultati
                val tempResults = mutableListOf<Pair<User, Int>>()
                var completedCount = 0

                if (userList.isEmpty()) {
                    isLoading = false
                    return
                }

                // Per ogni utente, otteniamo il conteggio dei manga
                userList.forEach { user ->
                    mangaRepository.getMangaCountByUser(user.username, object : UserRepositoryInterface.Callback<Int> {
                        override fun onResult(count: Int) {
                            // Aggiungiamo solo gli utenti con almeno 1 manga
                            if (count > 0) {
                                tempResults.add(user to count)
                            }
                            completedCount++

                            // Quando abbiamo tutti i risultati, ordiniamo e aggiorniamo la UI
                            if (completedCount == userList.size) {
                                // Ordina prima per numero di manga (decrescente), poi per nome (in caso di parità)
                                users = tempResults.sortedWith(compareByDescending<Pair<User, Int>> { it.second }
                                    .thenBy { it.first.username })
                                isLoading = false
                            }
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
                        Text(
                            text = "Classifica Lettori",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Torna indietro",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (users.isEmpty()) {
                Text(
                    text = "Nessun utente trovato",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    var previousCount: Int? = null
                    var rank = 1 // Iniziamo la classifica da 1

                    itemsIndexed(users) { index, (user, count) ->
                        // Se il numero di manga è diverso rispetto al precedente, aggiorniamo la posizione
                        if (previousCount == null || previousCount != count) {
                            rank = index + 1 // Nuova posizione se il conteggio dei manga è diverso
                        }

                        LeaderboardItem(
                            position = rank,
                            user = user,
                            mangaCount = count
                        )

                        previousCount = count // Salviamo l'ultimo conteggio per il confronto
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(
    position: Int,
    user: User,
    mangaCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$position",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.width(40.dp)
            )

            AsyncImage(
                model = user.profilePictureUri,
                contentDescription = "Foto profilo di ${user.username}",
                placeholder = painterResource(id = R.drawable.ic_default_profile),
                error = painterResource(id = R.drawable.ic_default_profile),
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Manga letti: $mangaCount",
                    fontSize = 14.sp
                )
            }
        }
    }
}
