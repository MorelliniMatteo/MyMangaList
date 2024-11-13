package com.example.mymangalist.ui.screens

import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    onLocationSelected: (String, LatLng) -> Unit
) {
    val defaultLocation = LatLng(41.9028, 12.4964) // Roma come posizione iniziale
    var selectedLocation by remember { mutableStateOf(defaultLocation) }
    var markers by remember { mutableStateOf<List<MarkerOptions>>(emptyList()) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(defaultLocation, 10f, 0f, 0f)
    }
    val context = LocalContext.current

    // Inizializzazione Places API
    val placesClient = remember { Places.createClient(context) }

    // Funzione per cercare luoghi
    fun searchPlace(queryText: String) {
        if (queryText.isNotBlank()) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(queryText)
                .setCountry("IT") // Filtra i risultati per l'Italia
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    val predictions = response.autocompletePredictions
                    val searchResults = predictions.map { it.getFullText(null).toString() }
                }
                .addOnFailureListener { exception ->
                    println("Places API Error: ${exception.message}")
                }
        }
    }

    // Funzione per recuperare coordinate LatLng da un placeId
    fun fetchLatLng(placeId: String) {
        val request = FetchPlaceRequest.builder(
            placeId,
            listOf(Place.Field.LAT_LNG, Place.Field.NAME)
        ).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                place.latLng?.let {
                    selectedLocation = it
                    cameraPositionState.position = CameraPosition(it, 12f, 0f, 0f)
                    markers = listOf(MarkerOptions().position(it).title(place.name))
                }
            }
            .addOnFailureListener { exception ->
                println("Error fetching place details: ${exception.message}")
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Location") },
                actions = {
                    TextButton(onClick = {
                        val cityName = getCityName(context, selectedLocation)
                        // Passiamo il nome della città e la latitudine/longitudine a AddScreen
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "purchaseLocation",
                            cityName
                        )
                        navController.popBackStack() // Torniamo alla schermata AddScreen
                    }) {
                        Text("Select")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mappa
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        selectedLocation = latLng
                        markers = listOf(MarkerOptions().position(latLng).title("Selected Location"))
                    }
                ) {
                    markers.forEach { markerOptions ->
                        Marker(
                            state = MarkerState(position = markerOptions.position),
                            title = markerOptions.title
                        )
                    }
                }
            }
        }
    }
}

/**
 * Funzione per ottenere il nome della città dato un LatLng.
 */
fun getCityName(context: Context, latLng: LatLng): String {
    return try {
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        addresses?.firstOrNull()?.locality ?: "Unknown Location"
    } catch (e: Exception) {
        "Unknown Location"
    }
}