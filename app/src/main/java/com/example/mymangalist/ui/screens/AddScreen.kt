package com.example.mymangalist.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mymangalist.Manga
import com.example.mymangalist.R
import com.example.mymangalist.data.MangaRepository
import com.example.mymangalist.ui.screens.MyMangaBottomBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    navController: NavController,
    userId: String,
    mangaRepository: MangaRepository,
    onMangaAdded: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var purchaseLocation by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Lista delle categorie
    val categories = listOf("Action", "Adventure", "Drama", "Fantasy", "Romance", "Horror")
    var expanded by remember { mutableStateOf(false) } // Stato per aprire/chiudere il menu a tendina
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Manga") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            MyMangaBottomBar(navController = navController, username = userId)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Input Fields
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = price,
                onValueChange = { newPrice ->
                    if (newPrice.isEmpty() || newPrice.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
                        price = newPrice
                    }
                },
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Categoria con menu a tendina
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedCategory,
                    onValueChange = { newCategory ->
                        selectedCategory = newCategory
                        category = newCategory
                    },
                    label = { Text("Select Category") },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                // Qui aggiungiamo la gestione corretta dell'espansione del menu
                if (expanded) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { categoryOption ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedCategory = categoryOption
                                    category = categoryOption
                                    expanded = false
                                },
                                text = { Text(categoryOption) }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = purchaseLocation,
                onValueChange = { purchaseLocation = it },
                label = { Text("Purchase Location") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        navController.navigate("map") {
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "purchaseLocation", purchaseLocation
                            )
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_map),
                            contentDescription = "Open Map"
                        )
                    }
                }
            )

            // Save Button
            Button(
                onClick = {
                    if (title.isBlank() || price.isBlank() || date.isBlank() || category.isBlank()) {
                        errorMessage = "All fields except image URL are required."
                        return@Button
                    }

                    val priceValue = price.toDoubleOrNull()
                    if (priceValue == null || priceValue <= 0) {
                        errorMessage = "Invalid price."
                        return@Button
                    }

                    val regexDate = Regex("\\d{4}-\\d{2}-\\d{2}")
                    if (!regexDate.matches(date)) {
                        errorMessage = "Invalid date format. Use YYYY-MM-DD."
                        return@Button
                    }

                    errorMessage = ""
                    isSaving = true

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val manga = Manga(
                                title = title,
                                price = priceValue,
                                date = date,
                                category = category,
                                imageUrl = imageUrl,
                                description = description,
                                purchaseLocation = purchaseLocation,
                                userId = userId
                            )
                            mangaRepository.addManga(manga)
                            isSaving = false
                            onMangaAdded()
                        } catch (e: Exception) {
                            isSaving = false
                            errorMessage = "Error saving manga. Please try again."
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Add Manga", color = Color.White)
                }
            }
        }
    }
}
