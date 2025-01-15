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
import java.io.File
import java.util.Locale
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import saveBitmapAsUri

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
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var location by remember { mutableStateOf("Non impostata") }
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val permanentUri = copyImageToAppStorage(context, it)
                profilePictureUri = permanentUri
                coroutineScope.launch {
                    userRepository.updateProfilePicture(username, permanentUri.toString())
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
                    val permanentUri = copyImageToAppStorage(context, uri)
                    profilePictureUri = permanentUri
                    coroutineScope.launch {
                        userRepository.updateProfilePicture(username, permanentUri.toString())
                    }
                }
            }
        }
    )

    LaunchedEffect(username) {
        userRepository.getUserByUsername(username, object : UserRepositoryInterface.Callback<User?> {
            override fun onResult(result: User?) {
                user = result
                result?.profilePictureUri?.let { uriString ->
                    try {
                        profilePictureUri = Uri.parse(uriString)
                    } catch (e: Exception) {
                        // Gestione errore parsing URI
                    }
                }
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
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
                actions = {
                    IconButton(onClick = { navController.navigate("leaderboard_screen/$username") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_classifica),
                            contentDescription = "Classifica",
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
                        Image(
                            painter = if (profilePictureUri != null) {
                                rememberAsyncImagePainter(profilePictureUri)
                            } else {
                                painterResource(id = R.drawable.ic_default_profile)
                            },
                            contentDescription = "Immagine del profilo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { showDialog = true }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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

                        Button(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                                        loc?.let {
                                            val geocoder = Geocoder(context, Locale.getDefault())
                                            val address = geocoder.getFromLocation(
                                                loc.latitude,
                                                loc.longitude,
                                                1
                                            )?.firstOrNull()?.locality ?: "Città sconosciuta"
                                            location = address
                                            coroutineScope.launch {
                                                userRepository.updateLocation(username, address)
                                            }
                                        } ?: run {
                                            Toast.makeText(
                                                context,
                                                "Impossibile ottenere la posizione",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    ActivityCompat.requestPermissions(
                                        context as android.app.Activity,
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                        1
                                    )
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

@Composable
fun showImageSelectionDialog(
    pickImageLauncher: ActivityResultLauncher<String>,
    takePictureLauncher: ActivityResultLauncher<Void?>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text("Seleziona immagine profilo")
        },
        text = {
            Column {
                Text(
                    text = "Scegli un'opzione per aggiornare l'immagine del profilo:",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                takePictureLauncher.launch(null)
                onDismiss()
            }) {
                Text("Scatta una foto")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                pickImageLauncher.launch("image/*")
                onDismiss()
            }) {
                Text("Scegli dalla galleria")
            }
        }
    )
}

fun copyImageToAppStorage(context: Context, uri: Uri): Uri {
    val inputStream = context.contentResolver.openInputStream(uri)
    val fileName = "profile_${System.currentTimeMillis()}.jpg"
    val outputFile = File(context.filesDir, fileName)

    inputStream?.use { input ->
        outputFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return Uri.fromFile(outputFile)
}
