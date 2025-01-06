package com.example.mymangalist.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.mymangalist.R
import androidx.compose.ui.unit.dp

@Composable
fun MyMangaBottomBar(navController: NavController, username: String) {
    BottomAppBar(
        modifier = Modifier.height(56.dp),
        content = {
            IconButton(onClick = { navController.navigate("home/$username") {
                // Pop up fino alla home per evitare stack di navigazione
                popUpTo("home/$username") { inclusive = true }
            }}, modifier = Modifier.weight(1f)) {
                Icon(painter = painterResource(id = R.drawable.ic_home), contentDescription = "Home")
            }
            IconButton(onClick = { navController.navigate("profile/$username") }, modifier = Modifier.weight(1f)) {
                Icon(painter = painterResource(id = R.drawable.ic_profile), contentDescription = "Profile")
            }
            IconButton(onClick = { navController.navigate("add_manga/$username") }, modifier = Modifier.weight(1f)) {
                Icon(painter = painterResource(id = R.drawable.ic_add), contentDescription = "Add Manga")
            }
            IconButton(onClick = { navController.navigate("settings/$username") }, modifier = Modifier.weight(1f)) {
                Icon(painter = painterResource(id = R.drawable.ic_settings), contentDescription = "Settings")
            }
        }
    )
}