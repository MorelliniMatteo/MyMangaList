package com.example.mymangalist.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.mymangalist.Manga
import com.example.mymangalist.R
import com.example.mymangalist.data.MangaRepository

@Composable
fun MangaCard(
    manga: Manga,
    onDetailsClick: (Manga) -> Unit,
    onDeleteClick: (Manga) -> Unit,
    modifier: Modifier = Modifier,
    mangaRepository: MangaRepository,
    userId: String
) {
    var isStarred by remember(manga.id) {
        mutableStateOf(manga.favourite)
    }
    var showDeleteConfirmation by remember { mutableStateOf(false) } // Stato per mostrare il popup

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp)) {
                val imagePainter = rememberAsyncImagePainter(
                    model = manga.imageUrl.takeIf { it.isNotEmpty() }
                        ?: R.drawable.manga_default
                )
                Image(
                    painter = imagePainter,
                    contentDescription = "Manga Image",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = manga.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Categoria: ${manga.category}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = { onDetailsClick(manga) }) {
                            Text("Vedi dettagli")
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isStarred) R.drawable.ic_favorite_filled else R.drawable.ic_favorite
                                ),
                                contentDescription = if (isStarred) "Favourite filled" else "Favourite",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        val newState = !isStarred
                                        isStarred = newState
                                        mangaRepository.updateFavouriteStatus(manga.id, userId, newState)
                                    }
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cestino),
                                contentDescription = "Delete",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        showDeleteConfirmation = true // Mostra il popup
                                    },
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }

    // Popup di conferma eliminazione
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(text = "Conferma Eliminazione")
            },
            text = {
                Text("Sei sicuro di voler eliminare definitivamente questo manga?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    onDeleteClick(manga) // Elimina il manga
                }) {
                    Text("Sì", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false // Chiudi il popup senza eliminare
                }) {
                    Text("Annulla")
                }
            }
        )
    }
}
