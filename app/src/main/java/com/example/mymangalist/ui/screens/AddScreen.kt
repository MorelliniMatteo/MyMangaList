package com.example.mymangalist.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
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
import android.widget.DatePicker
import androidx.compose.ui.platform.LocalContext
import java.util.*

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
    var category by remember { mutableStateOf("-") }
    var imageUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var purchaseLocation by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showCategoryError by remember { mutableStateOf(false) }

    // Lista delle categorie
    val categories = listOf("Action", "Adventure", "Comedy", "Fantasy", "Romance", "Horror")
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // Update the date state here, e.g.,
                date = "$dayOfMonth-${month + 1}-$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

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
        snackbarHost = {
            SnackbarHost(
                hostState = SnackbarHostState()
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

            // Campo di selezione data
            OutlinedTextField(
                value = date,
                onValueChange = { /* ... */ },
                label = { Text("Date") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = "Select Date"
                        )
                    }
                },
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

            // Categoria con menu a tendina
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { },
                    label = { Text("Select Category") },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { categoryOption ->
                        DropdownMenuItem(
                            onClick = {
                                category = categoryOption
                                expanded = false
                            },
                            text = { Text(categoryOption) }
                        )
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
                    if (title.isBlank() || price.isBlank() || date.isBlank() || category == "-") {
                        if (category == "-") {
                            showCategoryError = true
                        } else {
                            errorMessage = "All fields except image URL are required."
                        }
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

            // Popup per la categoria non selezionata
            if (showCategoryError) {
                SnackbarHost(
                    hostState = SnackbarHostState().apply {
                        currentSnackbarData?.dismiss()
                    }
                )
            }
        }
    }
}



