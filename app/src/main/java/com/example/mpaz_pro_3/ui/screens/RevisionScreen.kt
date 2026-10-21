package com.example.mpaz_pro_3.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.ui.components.BotonCarga
import com.example.mpaz_pro_3.ui.components.EtiquetaEstado
import com.example.mpaz_pro_3.ui.components.PantallaConVolver
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.ErrorFondo
import com.example.mpaz_pro_3.ui.theme.ErrorRojo
import com.example.mpaz_pro_3.ui.theme.ExitoFondo
import com.example.mpaz_pro_3.ui.theme.ExitoVerde
import com.example.mpaz_pro_3.ui.theme.TintaSuave
import com.example.mpaz_pro_3.ui.theme.colorAsignatura
import com.example.mpaz_pro_3.ui.theme.nombreAsignatura
import com.example.mpaz_pro_3.viewmodel.RevisionViewModel
import kotlinx.coroutines.delay

@Composable
fun RevisionScreen(
    onVolver: () -> Unit,
    viewModel: RevisionViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val objetivo = state.objetivo

    // Después de aprobar u observar, se muestra el mensaje y se vuelve a la lista
    LaunchedEffect(state.mensaje) {
        if (state.mensaje != null) {
            delay(1200)
            onVolver()
        }
    }

    PantallaConVolver(
        titulo = "Revisar contenido",
        onVolver = onVolver,
        color = objetivo?.let { colorAsignatura(it.asignatura) } ?: colorAsignatura("")
    ) {
        if (objetivo == null) {
            Text("Cargando...", color = TintaSuave)
            return@PantallaConVolver
        }

        Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(nombreAsignatura(objetivo.asignatura), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    EtiquetaEstado(objetivo.estado)
                }
                Text(objetivo.unidad, style = MaterialTheme.typography.titleMedium)
                Text("Objetivo: ${objetivo.descripcion}")
                Text("Código: ${objetivo.codigo}", color = TintaSuave)
                if (objetivo.observacion.isNotEmpty()) {
                    Text("Última observación: ${objetivo.observacion}", color = ErrorRojo)
                }
            }
        }

        state.contenido?.let { contenido ->
            Text("Contenido de \"Aprende\"", style = MaterialTheme.typography.titleMedium)
            Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(contenido.titulo, style = MaterialTheme.typography.titleMedium)
                    Text(contenido.explicacion)
                    Spacer(Modifier.height(6.dp))
                    Text("Ejemplo: ${contenido.ejemplo}", color = TintaSuave)
                }
            }
        }

        Text("Preguntas", style = MaterialTheme.typography.titleMedium)
        state.preguntas.forEach { pregunta ->
            val opciones = listOf(pregunta.opcionA, pregunta.opcionB, pregunta.opcionC)
            Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        if (pregunta.tipo == "DESAFIO") "Desafío nivel ${pregunta.nivel}" else "Prueba final",
                        style = MaterialTheme.typography.labelMedium,
                        color = TintaSuave
                    )
                    Text(pregunta.enunciado, style = MaterialTheme.typography.titleMedium)
                    Text("Alternativas: ${opciones.joinToString(" / ")}")
                    Text("Correcta: ${opciones[pregunta.correcta]}", color = ExitoVerde)
                }
            }
        }

        Text("Tu revisión", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = state.observacion,
            onValueChange = viewModel::onObservacionChanged,
            label = { Text("Observaciones") },
            leadingIcon = { Icon(Icons.Filled.EditNote, contentDescription = null) },
            trailingIcon = {
                if (state.error != null) Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", tint = ErrorRojo)
            },
            isError = state.error != null,
            supportingText = { Text(state.error ?: "Obligatorias solo si pides cambios") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = viewModel::pedirCambios,
                enabled = !state.procesando,
                border = BorderStroke(2.dp, ErrorRojo),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRojo),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) { Text("Pedir cambios") }

            BotonCarga(
                texto = "Aprobar",
                cargando = state.procesando,
                onClick = viewModel::aprobar,
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedVisibility(visible = state.mensaje != null, enter = scaleIn() + fadeIn()) {
            val aprobado = state.mensaje == "Contenido aprobado"
            Card(colors = CardDefaults.cardColors(containerColor = if (aprobado) ExitoFondo else ErrorFondo)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = if (aprobado) ExitoVerde else ErrorRojo)
                    Spacer(Modifier.width(8.dp))
                    Text(state.mensaje ?: "")
                }
            }
        }
    }
}
