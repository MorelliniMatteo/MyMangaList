package com.example.mymangalist.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mymangalist.Manga
import com.example.mymangalist.R
import com.example.mymangalist.data.MangaRepository
import com.example.mymangalist.ui.components.MyMangaBottomBar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import saveBitmapAsUri
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
    var profilePictureUri by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var purchaseLocation by remember { mutableStateOf("Non impostata") }
    var errorMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasShownLocationRequest by remember {
        mutableStateOf(context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            .getBoolean("location_permission_requested", false))
    }

    val categories = listOf("Action", "Adventure", "Comedy", "Fantasy", "Romance", "Horror")

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showLocationDialog = true
        } else {
            Toast.makeText(context, "Permesso posizione negato", Toast.LENGTH_SHORT).show()
        }
        // Salva che il permesso è stato richiesto
        context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("location_permission_requested", true)
            .apply()
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                // Copia l'immagine nella directory dell'app
                val permanentUri = copyImageToAppStorage(context, it)
                profilePictureUri = permanentUri?.toString()
            }
        }
    )

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let {
                val savedUri = saveBitmapAsUri(context, it)
                profilePictureUri = savedUri?.toString()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aggiungi Manga",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
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

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titolo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = date,
                onValueChange = { },
                label = { Text("Data") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker(context) { newDate -> date = newDate } }) {
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
                label = { Text("Prezzo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Category Selection
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { },
                    label = { Text("Seleziona categoria") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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

            Image(
                painter = if (profilePictureUri != null) {
                    rememberAsyncImagePainter(profilePictureUri)
                } else {
                    painterResource(id = R.drawable.ic_add_manga)
                },
                contentDescription = "Selected Image",
                modifier = Modifier
                    .size(120.dp)
                    .clickable { showDialog = true }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrizione") },
                modifier = Modifier.fillMaxWidth()
            )

            // Purchase Location TextField with Icon
            OutlinedTextField(
                value = purchaseLocation,
                onValueChange = { purchaseLocation = it },
                label = { Text("Luogo dell'acquisto") },
                trailingIcon = {
                    IconButton(onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PermissionChecker.PERMISSION_GRANTED
                        ) {
                            showLocationDialog = true
                        } else if (!hasShownLocationRequest) {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            hasShownLocationRequest = true
                        } else {
                            Toast.makeText(
                                context,
                                "Permesso posizione necessario. Abilitalo dalle impostazioni.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location),
                            contentDescription = "Location Icon"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (title.isBlank() || price.isBlank() || date.isBlank() || category == "-") {
                        errorMessage = "Tutti i campi eccetto la URL dell'immagine sono richiesti."
                        return@Button
                    }

                    val priceValue = price.toDoubleOrNull()
                    if (priceValue == null || priceValue <= 0) {
                        errorMessage = "Invalid price."
                        return@Button
                    }

                    coroutineScope.launch {
                        try {
                            val manga = Manga(
                                title = title,
                                price = priceValue,
                                date = date, // Data di acquisto scelta dall'utente.
                                insertedDate = System.currentTimeMillis(), // Data corrente per `insertedDate`.
                                category = category,
                                imageUrl = profilePictureUri ?: "",
                                description = description,
                                purchaseLocation = purchaseLocation,
                                userId = userId
                            )
                            mangaRepository.addManga(manga)
                            onMangaAdded()
                        } catch (e: Exception) {
                            errorMessage = "Errore. Prova di nuovo."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aggiungi Manga")
            }
        }
    }

    if (showDialog) {
        showImageSelectionDialog(
            pickImageLauncher = pickImageLauncher,
            takePictureLauncher = takePictureLauncher,
            onDismiss = { showDialog = false }
        )
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Seleziona metodo di salvataggio posizione") },
            text = {
                Column {
                    Text("Come vuoi salvare la posizione?")
                }
            },
            confirmButton = {
                Button(onClick = {
                    fetchLocation(context) { newLocation ->
                        purchaseLocation = newLocation
                        showLocationDialog = false
                    }
                }) {
                    Text("Salva come Città")
                }
            },
            dismissButton = {
                Button(onClick = {
                    fetchCoordinates(context) { newCoordinates ->
                        purchaseLocation = newCoordinates
                        showLocationDialog = false
                    }
                }) {
                    Text("Salva come Coordinate")
                }
            }
        )
    }
}

private fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected("$year-${month + 1}-$dayOfMonth")
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.show()
}

private fun fetchLocation(
    context: Context,
    onLocationUpdated: (String) -> Unit
) {
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                val city = addresses?.firstOrNull()?.locality ?: "Città sconosciuta"
                onLocationUpdated(city)
                Toast.makeText(context, "Posizione aggiornata: $city", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(context, "Impossibile ottenere la posizione", Toast.LENGTH_SHORT).show()
        }
    } catch (e: SecurityException) {
        Toast.makeText(context, "Permesso negato per accedere alla posizione", Toast.LENGTH_SHORT).show()
    }
}

private fun fetchCoordinates(
    context: Context,
    onCoordinatesUpdated: (String) -> Unit
) {
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val coordinates = "Lat: ${it.latitude}, Lon: ${it.longitude}"
                onCoordinatesUpdated(coordinates)
                Toast.makeText(context, "Coordinate aggiornate: $coordinates", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(context, "Impossibile ottenere le coordinate", Toast.LENGTH_SHORT).show()
        }
    } catch (e: SecurityException) {
        Toast.makeText(context, "Permesso negato per accedere alla posizione", Toast.LENGTH_SHORT).show()
    }
}
