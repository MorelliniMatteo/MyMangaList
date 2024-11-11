package com.example.mymangalist.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mymangalist.R
import com.example.mymangalist.User
import com.example.mymangalist.data.UserRepository
import com.example.mymangalist.data.MangaRepository
import com.example.mymangalist.data.UserRepositoryInterface
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.OutputStream
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

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLocation(context, userRepository, username) { newLocation ->
                location = newLocation // Aggiorna la UI con la nuova posizione
            }
        } else {
            Toast.makeText(context, "Permesso posizione negato", Toast.LENGTH_SHORT).show()
        }
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
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "MyMangaList", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back")
                    }
                }
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
                Image(
                    painter = if (profilePictureUri != null) {
                        rememberAsyncImagePainter(profilePictureUri)
                    } else {
                        painterResource(id = R.drawable.ic_default_profile)
                    },
                    contentDescription = "Immagine del profilo",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { showDialog = true }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Username: ${user?.username}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Email: ${user?.email}", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Totale Manga Letture: $mangaCount", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Posizione: $location", fontSize = 16.sp)

                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PermissionChecker.PERMISSION_GRANTED) {
                            fetchLocation(context, userRepository, username) { newLocation ->
                                location = newLocation // Aggiorna la UI
                            }
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Aggiorna Posizione")
                }
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

private fun fetchLocation(
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

fun saveBitmapAsUri(context: Context, bitmap: Bitmap): Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }

    val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        context.contentResolver.openOutputStream(it)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
    }
    return uri
}
