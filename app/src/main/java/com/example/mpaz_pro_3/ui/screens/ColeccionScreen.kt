package com.example.mpaz_pro_3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.ui.components.PantallaConVolver
import com.example.mpaz_pro_3.ui.components.TarjetaPokemon
import com.example.mpaz_pro_3.ui.theme.TintaSuave
import com.example.mpaz_pro_3.ui.utils.esPantallaAncha
import com.example.mpaz_pro_3.viewmodel.ColeccionViewModel

// Pantalla "Mi colección": Pokémon ganados al superar los desafíos (datos de la PokeAPI)
@Composable
fun ColeccionScreen(
    onVolver: () -> Unit,
    viewModel: ColeccionViewModel = viewModel()
) {
    val recompensas by viewModel.recompensas.collectAsStateWithLifecycle()
    val columnas = if (esPantallaAncha()) 3 else 2

    PantallaConVolver(titulo = "Mi colección", onVolver = onVolver) {
        Text(
            "Ganas un Pokémon cada vez que superas los 3 niveles de desafíos de una asignatura.",
            color = TintaSuave
        )
        if (recompensas.isEmpty()) {
            Text("Todavía no tienes Pokémon. ¡Termina tus desafíos para ganar el primero!",
                style = MaterialTheme.typography.titleMedium)
        }
        recompensas.chunked(columnas).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fila.forEach { recompensa ->
                    Box(modifier = Modifier.weight(1f)) {
                        TarjetaPokemon(recompensa = recompensa, tamanoImagen = 100.dp)
                    }
                }
                // Espacios vacíos para que la última fila quede alineada
                repeat(columnas - fila.size) { Box(modifier = Modifier.weight(1f)) {} }
            }
        }
    }
}

