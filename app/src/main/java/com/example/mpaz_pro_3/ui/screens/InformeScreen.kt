package com.example.mpaz_pro_3.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.ui.components.BotonCarga
import com.example.mpaz_pro_3.ui.components.PantallaConVolver
import com.example.mpaz_pro_3.ui.theme.AmarilloClaro
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.ErrorFondo
import com.example.mpaz_pro_3.ui.theme.ErrorRojo
import com.example.mpaz_pro_3.ui.theme.ExitoFondo
import com.example.mpaz_pro_3.ui.theme.ExitoVerde
import com.example.mpaz_pro_3.ui.theme.HistoriaColor
import com.example.mpaz_pro_3.ui.theme.TintaSuave
import com.example.mpaz_pro_3.ui.theme.nombreAsignatura
import com.example.mpaz_pro_3.viewmodel.InformeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InformeScreen(
    onVolver: () -> Unit,
    viewModel: InformeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val objetivo = state.objetivo

    // Recurso nativo Archivos: el docente elige dónde guardar el archivo
    val selectorDestino = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) viewModel.exportar(uri)
    }

    PantallaConVolver(titulo = "Informe pedagógico", onVolver = onVolver) {
        Card(colors = CardDefaults.cardColors(containerColor = AmarilloClaro)) {
            Row(modifier = Modifier.padding(12.dp)) {
                Icon(Icons.Filled.Info, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Uso exclusivo docente. Este informe no entrega diagnósticos ni decisiones académicas y debe ser validado antes de usarse.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text("Objetivo", style = MaterialTheme.typography.titleMedium)
        state.objetivos.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = item.id == state.seleccionadoId, onClick = { viewModel.seleccionar(item.id) })
                Text("${nombreAsignatura(item.asignatura)} · ${item.unidad}")
            }
        }

        if (objetivo == null) return@PantallaConVolver

        if (state.totalIntentos == 0) {
            Text("Aún no hay pruebas entregadas para este objetivo.", color = TintaSuave)
            return@PantallaConVolver
        }

        // Resumen
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Resumen("Estudiantes", "${state.estudiantes}", Modifier.weight(1f))
            Resumen("Intentos", "${state.totalIntentos}", Modifier.weight(1f))
            Resumen("Logro", "${state.promedio}%", Modifier.weight(1f))
        }

        Text("Respuestas correctas por pregunta", style = MaterialTheme.typography.titleMedium)
        state.preguntas.forEach { pregunta ->
            val avance by animateFloatAsState(pregunta.porcentaje / 100f, label = "barraPregunta")
            Column {
                Text("${pregunta.numero}. ${pregunta.enunciado} (${pregunta.porcentaje}%)", style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(
                    progress = { avance },
                    color = if (pregunta.porcentaje >= 60) ExitoVerde else HistoriaColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = ExitoFondo)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Fortalezas", style = MaterialTheme.typography.titleMedium, color = ExitoVerde)
                if (state.fortalezas.isEmpty()) Text("Ninguna pregunta superó el 60%.")
                state.fortalezas.forEach { Text("- Pregunta ${it.numero}: ${it.enunciado}") }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = ErrorFondo)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Aspectos a reforzar", style = MaterialTheme.typography.titleMedium, color = ErrorRojo)
                if (state.aReforzar.isEmpty()) Text("Todas las preguntas superaron el 60%.")
                state.aReforzar.forEach { Text("- Pregunta ${it.numero}: ${it.enunciado}") }
            }
        }

        Text("Resultados por estudiante", style = MaterialTheme.typography.titleMedium)
        Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
            Column(modifier = Modifier.padding(12.dp)) {
                state.porEstudiante.forEachIndexed { i, estudiante ->
                    if (i > 0) HorizontalDivider()
                    Row(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(estudiante.rut, modifier = Modifier.weight(1f))
                        Text("Mejor: ${estudiante.mejorPuntaje} · ${estudiante.intentos} intento(s)")
                    }
                }
            }
        }

        // Validación del informe
        Text("Validación", style = MaterialTheme.typography.titleMedium)
        if (objetivo.informeValidado) {
            val fecha = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(objetivo.fechaValidacion))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ExitoVerde)
                Spacer(Modifier.width(8.dp))
                Text("Informe validado el $fecha", color = ExitoVerde)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.revisado, onCheckedChange = viewModel::onRevisadoChanged)
                Text("Revisé el informe y confirmo que está correcto")
            }
            MensajeErrorCampo(state.errorRevisado)
            BotonCarga(texto = "Validar informe", cargando = state.validando, onClick = viewModel::validar)
        }

        OutlinedButton(
            onClick = { selectorDestino.launch("informe_${objetivo.asignatura.lowercase()}.csv") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Descargar informe (CSV)")
        }

        AnimatedVisibility(visible = state.mensaje != null) {
            Text(state.mensaje ?: "", color = ExitoVerde, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun Resumen(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Blanco), modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)) {
            Text(valor, style = MaterialTheme.typography.headlineSmall)
            Text(titulo, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}
