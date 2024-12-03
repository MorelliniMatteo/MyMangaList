package com.example.mymangalist.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.example.mymangalist.data.MangaRepository
import com.example.mymangalist.data.UserRepositoryInterface
import com.example.mymangalist.Manga

// Classe per i dati del grafico
data class DatiCategoria(
    val categoria: String,
    val conteggio: Int,
    val percentuale: Float
)

@Composable
fun GraficoCategorieManga(
    mangaRepository: MangaRepository,
    username: String
) {
    var datiCategorie by remember { mutableStateOf(listOf<DatiCategoria>()) }

    LaunchedEffect(username) {
        mangaRepository.getMangasByUser(username, null, object : UserRepositoryInterface.Callback<List<Manga>> {
            override fun onResult(mangas: List<Manga>) {
                // Conta i manga per categoria
                val conteggioCategorie = mangas
                    .groupBy { it.category }
                    .mapValues { it.value.size }

                // Converte in dati per il grafico
                val datiGrafico = conteggioCategorie.map { (categoria, conteggio) ->
                    DatiCategoria(
                        categoria = categoria,
                        conteggio = conteggio,
                        percentuale = conteggio.toFloat() / mangas.size * 100
                    )
                }

                datiCategorie = datiGrafico
            }
        })
    }

    val colori = listOf(
        AndroidColor.rgb(31, 119, 180), // Blu
        AndroidColor.rgb(255, 127, 14), // Arancione
        AndroidColor.rgb(44, 160, 44),  // Verde
        AndroidColor.rgb(214, 39, 40),  // Rosso
        AndroidColor.rgb(148, 103, 189) // Viola
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Distribuzione Manga per Categoria",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                if (datiCategorie.isNotEmpty()) {
                    AndroidView(
                        factory = { context ->
                            PieChart(context).apply {
                                val entries = datiCategorie.map {
                                    PieEntry(it.percentuale, "${it.categoria} (${it.conteggio})")
                                }

                                val dataSet = PieDataSet(entries, "")
                                dataSet.colors = colori
                                dataSet.valueTextSize = 12f
                                dataSet.valueTextColor = AndroidColor.BLACK

                                val pieData = PieData(dataSet)
                                data = pieData

                                description.isEnabled = false
                                legend.isEnabled = true
                                setEntryLabelColor(AndroidColor.BLACK)

                                animateY(1400)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "Nessun dato disponibile",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
