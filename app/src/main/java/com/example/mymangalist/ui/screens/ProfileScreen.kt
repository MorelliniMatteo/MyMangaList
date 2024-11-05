package com.example.mymangalist.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import coil.compose.rememberAsyncImagePainter
import com.example.mymangalist.R
import com.example.mymangalist.User
import com.example.mymangalist.data.UserRepository
import com.example.mymangalist.data.MangaRepository
import com.example.mymangalist.data.UserRepositoryInterface
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    userRepository: UserRepository,
    mangaRepository: MangaRepository,
    username: String
) {
    var user by remember { mutableStateOf<User?>(null) }
    var mangaCount by remember { mutableStateOf(0) }
    var profilePictureUri by remember { mutableStateOf<String?>(null) }
    var location by remember { mutableStateOf("Not set") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(username) {
        userRepository.getUserByUsername(username, object : UserRepositoryInterface.Callback<User?> {
            override fun onResult(result: User?) {
                user = result
                profilePictureUri = result?.profilePictureUri
                location = result?.location ?: "Not set"
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
                title = { Text(text = "MyMangaList", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
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
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {
                            // Here, you can implement functionality to select a new image
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Username: ${user?.username}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Email: ${user?.email}", fontSize = 16.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Total Manga Read: $mangaCount", fontSize = 16.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Location: $location", fontSize = 16.sp)
                Button(
                    onClick = {
                        coroutineScope.launch {
                            // Update the user's location if permissions are granted
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Update Location")
                }
            }
        }
    )
}
