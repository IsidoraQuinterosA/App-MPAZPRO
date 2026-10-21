package com.example.mpaz_pro_3.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.ui.components.BotonCarga
import com.example.mpaz_pro_3.ui.components.EstadoOpcion
import com.example.mpaz_pro_3.ui.components.OpcionRespuesta
import com.example.mpaz_pro_3.ui.components.PantallaConVolver
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.ErrorRojo
import com.example.mpaz_pro_3.ui.theme.TintaSuave
import com.example.mpaz_pro_3.ui.theme.colorAsignatura
import com.example.mpaz_pro_3.viewmodel.PruebaViewModel

@Composable
fun PruebaScreen(
    onVolver: () -> Unit,
    onTerminada: (intentoId: Long) -> Unit,
    viewModel: PruebaViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val color = colorAsignatura(state.asignatura)
    var confirmar by rememberSaveable { mutableStateOf(false) }

    // Al guardar el intento, se navega a la pantalla de resultados
    LaunchedEffect(state.intentoGuardado) {
        state.intentoGuardado?.let { id ->
            viewModel.onNavegado()
            onTerminada(id)
        }
    }

    PantallaConVolver(titulo = "Prueba final", onVolver = onVolver, color = color) {
        if (!state.cargado) {
            Text("Cargando prueba...", color = TintaSuave)
            return@PantallaConVolver
        }
        if (state.sinIntentos) {
            Text("Ya usaste tus ${state.maxIntentos} intentos de esta prueba.", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Volver a la unidad") }
            return@PantallaConVolver
        }

        val total = state.preguntas.size
        val avance by animateFloatAsState((state.indice + 1) / total.toFloat(), label = "avancePrueba")

        Text(
            "Intento ${state.intentosUsados + 1} de ${state.maxIntentos} · Pregunta ${state.indice + 1} de $total",
            style = MaterialTheme.typography.titleMedium
        )
        LinearProgressIndicator(
            progress = { avance },
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        )

        // AnimatedContent desliza la pregunta al avanzar o retroceder
        AnimatedContent(
            targetState = state.indice,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "preguntaPrueba"
        ) { indice ->
            val pregunta = state.preguntas[indice]
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = color)) {
                    Text(
                        pregunta.enunciado,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Blanco,
                        modifier = Modifier.padding(20.dp)
                    )
                }
                listOf(pregunta.opcionA, pregunta.opcionB, pregunta.opcionC).forEachIndexed { i, texto ->
                    OpcionRespuesta(
                        letra = "ABC"[i].toString(),
                        texto = texto,
                        estado = if (state.elegidas[indice] == i) EstadoOpcion.SELECCIONADA else EstadoOpcion.NORMAL,
                        onClick = { viewModel.elegir(i) }
                    )
                }
            }
        }

        // Mensaje de validación
        AnimatedVisibility(visible = state.error != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = ErrorRojo)
                Spacer(Modifier.width(6.dp))
                Text(state.error ?: "", color = ErrorRojo)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = viewModel::anterior,
                enabled = state.indice > 0 && !state.enviando,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) { Text("Anterior") }

            if (state.esUltima) {
                BotonCarga(
                    texto = "Entregar",
                    cargando = state.enviando,
                    onClick = { confirmar = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                Button(
                    onClick = viewModel::siguiente,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) { Text("Siguiente") }
            }
        }
        Text("Verás tus respuestas correctas e incorrectas al entregar.", color = TintaSuave)
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("¿Entregar la prueba?") },
            text = { Text("Después de entregar no podrás cambiar tus respuestas.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmar = false
                    viewModel.entregar()
                }) { Text("Entregar") }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Revisar") } }
        )
    }
}
