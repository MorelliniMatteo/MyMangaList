package com.example.mymangalist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mymangalist.R
import com.example.mymangalist.data.MangaRepository
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.mymangalist.data.UserRepository
import com.example.mymangalist.ui.screens.MyMangaBottomBar  // Importa MyMangaBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    userRepository: UserRepository,
    mangaRepository: MangaRepository,
    username: String
) {
    var searchQuery by remember { mutableStateOf("") }
    val mangaList = listOf("Manga One", "Manga Two", "Manga Three")

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
                    IconButton(onClick = { /* Navigate to favorites */ }) {
                        Icon(painter = painterResource(id = R.drawable.ic_favorite), contentDescription = "Favorites")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Open filter */ }) {
                        Icon(painter = painterResource(id = R.drawable.ic_filter), contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = { MyMangaBottomBar(navController = navController, username = username) },  // Usa MyMangaBottomBar
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text("Search") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(mangaList) { manga ->
                        Text(manga, fontSize = 20.sp, modifier = Modifier.padding(8.dp))
                        Divider()
                    }
                }
            }
        }
    )
}
