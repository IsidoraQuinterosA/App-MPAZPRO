package com.example.mpaz_pro_3.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.ui.components.EstadoOpcion
import com.example.mpaz_pro_3.ui.components.OpcionRespuesta
import com.example.mpaz_pro_3.ui.components.PantallaConVolver
import com.example.mpaz_pro_3.ui.theme.Amarillo
import com.example.mpaz_pro_3.ui.theme.AmarilloClaro
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.ErrorRojo
import com.example.mpaz_pro_3.ui.theme.ExitoVerde
import com.example.mpaz_pro_3.ui.theme.TintaSuave
import com.example.mpaz_pro_3.ui.theme.colorAsignatura
import com.example.mpaz_pro_3.viewmodel.DesafiosViewModel

@Composable
fun DesafiosScreen(
    onVolver: () -> Unit,
    viewModel: DesafiosViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val color = colorAsignatura(state.asignatura)

    PantallaConVolver(titulo = "Desafíos", onVolver = onVolver, color = color) {
        if (!state.cargado) {
            Text("Cargando desafíos...", color = TintaSuave)
            return@PantallaConVolver
        }

        // Terminó los 3 niveles
        if (state.nivelActual > 3) {
            Card(colors = CardDefaults.cardColors(containerColor = AmarilloClaro)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Amarillo, modifier = Modifier.size(72.dp))
                    Text("¡Superaste los 3 niveles!", style = MaterialTheme.typography.headlineSmall)
                    Text("Ya puedes dar la prueba final.", modifier = Modifier.padding(vertical = 8.dp))
                    Button(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Volver a la unidad") }
                }
            }
            return@PantallaConVolver
        }

        val pregunta = state.pregunta ?: return@PantallaConVolver
        val dificultad = listOf("Fácil", "Medio", "Difícil")[state.nivelActual - 1]

        // La barra de avance se mueve con animación al pasar de nivel
        val avance by animateFloatAsState((state.nivelActual - 1) / 3f, label = "avanceDesafios")
        Text("Nivel ${state.nivelActual} de 3 · $dificultad", style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(
            progress = { avance },
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        )

        Card(colors = CardDefaults.cardColors(containerColor = color)) {
            Text(
                pregunta.enunciado,
                style = MaterialTheme.typography.headlineSmall,
                color = Blanco,
                modifier = Modifier.padding(20.dp)
            )
        }

        val opciones = listOf(pregunta.opcionA, pregunta.opcionB, pregunta.opcionC)
        opciones.forEachIndexed { i, texto ->
            val estado = when {
                state.seleccion != i -> EstadoOpcion.NORMAL
                state.esCorrecta == true -> EstadoOpcion.CORRECTA
                else -> EstadoOpcion.INCORRECTA
            }
            OpcionRespuesta(
                letra = "ABC"[i].toString(),
                texto = texto,
                estado = estado,
                onClick = { viewModel.elegir(i) },
                habilitada = state.esCorrecta != true
            )
        }

        // Retroalimentación inmediata
        AnimatedVisibility(visible = state.esCorrecta == true, enter = scaleIn() + fadeIn()) {
            Text("¡Correcto! Muy bien.", style = MaterialTheme.typography.titleMedium, color = ExitoVerde)
        }
        AnimatedVisibility(visible = state.esCorrecta == false, enter = fadeIn()) {
            Text("Casi... ¡Inténtalo otra vez!", style = MaterialTheme.typography.titleMedium, color = ErrorRojo)
        }

        if (state.esCorrecta == true) {
            Button(onClick = viewModel::siguienteNivel, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.nivelActual < 3) "Ir al nivel ${state.nivelActual + 1}" else "Terminar desafíos")
            }
        }
    }
}
