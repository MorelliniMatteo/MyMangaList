package com.example.mymangalist.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mymangalist.R
import com.example.mymangalist.User
import com.example.mymangalist.data.UserRepository
import com.example.mymangalist.data.MangaRepository
import com.example.mymangalist.data.UserRepositoryInterface
import com.example.mymangalist.ui.components.MyMangaBottomBar
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import com.example.mymangalist.utils.saveBitmapAsUri
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    userRepository: UserRepository,
    mangaRepository: MangaRepository,
    username: String,
    context: Context
) {
    var user by remember { mutableStateOf<User?>(null) }
    var mangaCount by remember { mutableStateOf(0) }
    var profilePictureUri by remember { mutableStateOf<String?>(null) }
    var location by remember { mutableStateOf("Non impostata") }
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var hasShownLocationRequest by remember {
        mutableStateOf(context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            .getBoolean("location_permission_requested", false))
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLocation(context, userRepository, username) { newLocation ->
                location = newLocation
            }
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
                profilePictureUri = it.toString()
                coroutineScope.launch {
                    userRepository.updateProfilePicture(username, it.toString())
                }
            }
        }
    )

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let {
                val savedUri = saveBitmapAsUri(context, it)
                savedUri?.let { uri ->
                    profilePictureUri = uri.toString()
                    coroutineScope.launch {
                        userRepository.updateProfilePicture(username, uri.toString())
                    }
                }
            }
        }
    )

    LaunchedEffect(username) {
        userRepository.getUserByUsername(username, object : UserRepositoryInterface.Callback<User?> {
            override fun onResult(result: User?) {
                user = result
                profilePictureUri = result?.profilePictureUri
                location = result?.location ?: "Non impostata"
            }
        })

        mangaRepository.getMangaCountByUser(username, object : UserRepositoryInterface.Callback<Int> {
            override fun onResult(result: Int) {
                mangaCount = result
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(), // Per far sì che occupi tutta la larghezza disponibile
                        horizontalArrangement = Arrangement.Center // Per centrare il contenuto
                    ) {
                        Text(
                            text = "Profilo Utente",
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
        bottomBar = { MyMangaBottomBar(navController = navController, username = username) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // User Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Image
                        Image(
                            painter = if (profilePictureUri != null) {
                                rememberAsyncImagePainter(profilePictureUri)
                            } else {
                                painterResource(id = R.drawable.ic_default_profile)
                            },
                            contentDescription = "Immagine del profilo",
                            modifier = Modifier
                                .fillMaxWidth() // Per far sì che l'immagine sia larga quanto la card
                                .height(150.dp) // Imposta un'altezza fissa per l'immagine
                                .clip(MaterialTheme.shapes.medium) // Usa angoli arrotondati
                                .clickable { showDialog = true }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // User Info
                        Text(
                            text = user?.username ?: "Username non disponibile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user?.email ?: "Email non disponibile",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Totale Manga Letti: $mangaCount",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Posizione: $location",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Update Location Button
                        Button(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    fetchLocation(context, userRepository, username) { newLocation ->
                                        location = newLocation
                                    }
                                } else if (!hasShownLocationRequest) {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    hasShownLocationRequest = true
                                } else {
                                    Toast.makeText(context, "Permesso posizione necessario. Abilitalo dalle impostazioni.", Toast.LENGTH_LONG).show()
                                }
                            },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Aggiorna Posizione")
                        }
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                // Grafico delle categorie di manga
                GraficoCategorieManga(
                    mangaRepository = mangaRepository,
                    username = username
                )
            }
        }
    )

    if (showDialog) {
        showImageSelectionDialog(
            pickImageLauncher = pickImageLauncher,
            takePictureLauncher = takePictureLauncher,
            onDismiss = { showDialog = false }
        )
    }
}

fun fetchLocation(
    context: Context,
    userRepository: UserRepository,
    username: String,
    onLocationUpdated: (String) -> Unit
) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                val city = addresses?.firstOrNull()?.locality ?: "Posizione sconosciuta"
                val newLocation = "$city (Lat: ${it.latitude}, Lon: ${it.longitude})"

                userRepository.updateLocation(username, newLocation)
                onLocationUpdated(newLocation) // Aggiorna la UI
                Toast.makeText(context, "Posizione aggiornata", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(context, "Impossibile ottenere la posizione", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Permesso posizione non disponibile", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun showImageSelectionDialog(
    pickImageLauncher: ActivityResultLauncher<String>,
    takePictureLauncher: ActivityResultLauncher<Void?>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Seleziona Immagine") },
        text = { Text("Scegli un'opzione per aggiornare l'immagine del profilo.") },
        confirmButton = {
            Button(onClick = {
                pickImageLauncher.launch("image/*")
                onDismiss()
            }) {
                Text("Scegli dalla Galleria")
            }
        },
        dismissButton = {
            Button(onClick = {
                takePictureLauncher.launch(null)
                onDismiss()
            }) {
                Text("Scatta una Foto")
            }
        }
    )
}
