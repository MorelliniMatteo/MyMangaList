import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mymangalist.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Name Application", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { /* Naviga ai preferiti */ }) {
                        Icon(painter = painterResource(id = R.drawable.ic_favorite), contentDescription = "Favorites")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Apre il filtro */ }) {
                        Icon(painter = painterResource(id = R.drawable.ic_filter), contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(56.dp),
                content = {
                    IconButton(onClick = { /* Naviga al profilo */ }, modifier = Modifier.weight(1f)) {
                        Icon(painter = painterResource(id = R.drawable.ic_profile), contentDescription = "Profile")
                    }
                    IconButton(onClick = { /* Aggiungi un nuovo manga */ }, modifier = Modifier.weight(1f)) {
                        Icon(painter = painterResource(id = R.drawable.ic_add), contentDescription = "Add Manga")
                    }
                    IconButton(onClick = { /* Naviga alle impostazioni */ }, modifier = Modifier.weight(1f)) {
                        Icon(painter = painterResource(id = R.drawable.ic_settings), contentDescription = "Settings")
                    }
                }
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
                // Barra di ricerca
                var searchQuery by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text("Search") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lista degli elementi (Manga)
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("Item One", fontSize = 20.sp, modifier = Modifier.padding(8.dp))
                    Divider()
                    Text("Item Two", fontSize = 20.sp, modifier = Modifier.padding(8.dp))
                    Divider()
                    Text("Item Three", fontSize = 20.sp, modifier = Modifier.padding(8.dp))
                }
            }
        }
    )
}
