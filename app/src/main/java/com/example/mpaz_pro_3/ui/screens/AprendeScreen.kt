package com.example.mpaz_pro_3.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.ui.components.BotonCarga
import com.example.mpaz_pro_3.ui.components.PantallaConVolver
import com.example.mpaz_pro_3.ui.theme.AmarilloClaro
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.CelesteClaro
import com.example.mpaz_pro_3.ui.theme.ExitoFondo
import com.example.mpaz_pro_3.ui.theme.ExitoVerde
import com.example.mpaz_pro_3.ui.theme.TintaSuave
import com.example.mpaz_pro_3.ui.theme.colorAsignatura
import com.example.mpaz_pro_3.viewmodel.AprendeViewModel

@Composable
fun AprendeScreen(
    onVolver: () -> Unit,
    viewModel: AprendeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val contenido = state.contenido

    PantallaConVolver(
        titulo = "Aprende",
        onVolver = onVolver,
        color = colorAsignatura(state.asignatura)
    ) {
        if (contenido == null) {
            Text("Cargando contenido...", color = TintaSuave)
            return@PantallaConVolver
        }

        Text(contenido.titulo, style = MaterialTheme.typography.headlineSmall)

        // Espacio para la imagen de apoyo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(MaterialTheme.shapes.large)
                .background(CelesteClaro)
        ) {
            Icon(Icons.Filled.Image, contentDescription = "Imagen de apoyo", modifier = Modifier.size(56.dp), tint = TintaSuave)
        }

        Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
            Text(
                contenido.explicacion,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        Card(colors = CardDefaults.cardColors(containerColor = AmarilloClaro)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ejemplo", style = MaterialTheme.typography.labelLarge)
                Text(contenido.ejemplo, style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Antes de terminar se ve el botón; después, un mensaje que confirma que terminó
        AnimatedVisibility(visible = !state.terminado) {
            BotonCarga(
                texto = "¡Terminé de aprender!",
                cargando = state.guardando,
                onClick = viewModel::terminar
            )
        }

        AnimatedVisibility(visible = state.terminado, enter = scaleIn() + fadeIn()) {
            Card(colors = CardDefaults.cardColors(containerColor = ExitoFondo)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ExitoVerde, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("¡Muy bien! Terminaste Aprende.", style = MaterialTheme.typography.titleMedium, color = ExitoVerde)
                    }
                    Text("Ahora sigue con los desafíos.", modifier = Modifier.padding(top = 4.dp))
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                        Text("Volver a la unidad")
                    }
                }
            }
        }
    }
}
