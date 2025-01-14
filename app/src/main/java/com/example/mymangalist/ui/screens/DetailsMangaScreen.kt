package com.example.mymangalist.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mymangalist.Manga
import com.example.mymangalist.R
import com.example.mymangalist.ui.components.MyMangaBottomBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsMangaScreen(
    manga: Manga,
    onBackClick: () -> Unit,
    navController: NavController, // Parametro aggiunto
    username: String,             // Parametro aggiunto
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dettagli Manga",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            MyMangaBottomBar(navController = navController, username = username) // Barra inferiore
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Spazio tra la top bar e il contenuto
            Spacer(modifier = Modifier.height(16.dp))

            // Card per il contenuto del Manga
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Immagine del Manga
                    val imagePainter = rememberAsyncImagePainter(
                        model = manga.imageUrl.takeIf { it.isNotEmpty() }
                            ?: R.drawable.manga_default
                    )
                    Image(
                        painter = imagePainter,
                        contentDescription = "Manga Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp)) // Arrotondamento degli angoli
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Titolo
                    Text(
                        text = manga.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black // Colore del testo in nero
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Data di acquisto e luogo
                    Text(
                        text = "Data di acquisto: ${manga.date}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black // Colore del testo in nero
                    )
                    Text(
                        text = "Luogo: ${manga.purchaseLocation}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black // Colore del testo in nero
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prezzo di acquisto
                    Text(
                        text = "Prezzo: €${manga.price}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary // Colore del prezzo
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Categoria
                    Text(
                        text = "Categoria: ${manga.category}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black // Colore del testo in nero
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Descrizione
                    Text(
                        text = manga.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black // Colore del testo in nero
                    )
                }
            }
        }
    }
}
