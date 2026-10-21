package com.example.mpaz_pro_3.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.ui.components.PantallaConVolver
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.ErrorFondo
import com.example.mpaz_pro_3.ui.theme.ErrorRojo
import com.example.mpaz_pro_3.ui.theme.ExitoFondo
import com.example.mpaz_pro_3.ui.theme.ExitoVerde
import com.example.mpaz_pro_3.ui.theme.TintaSuave
import com.example.mpaz_pro_3.ui.theme.colorAsignatura
import com.example.mpaz_pro_3.viewmodel.ResultadoViewModel

@Composable
fun ResultadoScreen(
    onVolver: () -> Unit,
    viewModel: ResultadoViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val color = colorAsignatura(state.asignatura)

    // El puntaje aparece con un pequeño rebote
    var mostrar by remember { mutableStateOf(false) }
    LaunchedEffect(state.cargado) { if (state.cargado) mostrar = true }
    val escala by animateFloatAsState(
        targetValue = if (mostrar) 1f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "escalaPuntaje"
    )

    PantallaConVolver(titulo = "Resultados", onVolver = onVolver, color = color) {
        if (!state.cargado) {
            Text("Cargando resultados...", color = TintaSuave)
            return@PantallaConVolver
        }

        val mensaje = when {
            state.correctas == state.total -> "¡Excelente! Respondiste todo bien."
            state.correctas * 2 >= state.total -> "¡Muy bien! Revisa las que te faltaron."
            else -> "¡Sigue practicando, tú puedes!"
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = color),
            modifier = Modifier.scale(escala)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("¡Prueba terminada!", color = Blanco, style = MaterialTheme.typography.titleMedium)
                Text("${state.correctas} de ${state.total}", color = Blanco, style = MaterialTheme.typography.displaySmall)
                Text(mensaje, color = Blanco)
            }
        }

        Text("Revisa tus respuestas", style = MaterialTheme.typography.titleMedium)

        state.items.forEachIndexed { i, item ->
            Card(colors = CardDefaults.cardColors(containerColor = if (item.esCorrecta) ExitoFondo else ErrorFondo)) {
                Row(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = if (item.esCorrecta) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                        contentDescription = if (item.esCorrecta) "Correcta" else "Incorrecta",
                        tint = if (item.esCorrecta) ExitoVerde else ErrorRojo
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("${i + 1}. ${item.enunciado}", style = MaterialTheme.typography.titleMedium)
                        Text("Tu respuesta: ${item.respuesta}")
                        if (!item.esCorrecta) {
                            Text("Respuesta correcta: ${item.correcta}", color = ExitoVerde)
                        }
                    }
                }
            }
        }

        Button(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("Volver a la unidad")
        }
    }
}
