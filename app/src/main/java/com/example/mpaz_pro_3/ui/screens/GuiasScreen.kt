package com.example.mpaz_pro_3.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.ui.components.BotonCarga
import com.example.mpaz_pro_3.ui.components.PantallaConVolver
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.ErrorRojo
import com.example.mpaz_pro_3.ui.theme.ExitoFondo
import com.example.mpaz_pro_3.ui.theme.ExitoVerde
import com.example.mpaz_pro_3.ui.theme.Petroleo
import com.example.mpaz_pro_3.ui.theme.TintaSuave
import com.example.mpaz_pro_3.ui.theme.nombreAsignatura
import com.example.mpaz_pro_3.util.Archivos
import com.example.mpaz_pro_3.viewmodel.GuiasViewModel

@Composable
fun GuiasScreen(
    onVolver: () -> Unit,
    viewModel: GuiasViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Recurso nativo Archivos: selector de documentos de Android (Storage Access Framework)
    val selectorArchivo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.onArchivoElegido(uri)
    }

    PantallaConVolver(titulo = "Subir guías", onVolver = onVolver) {
        Text("1. ¿Para qué objetivo es la guía?", style = MaterialTheme.typography.titleMedium)
        state.objetivos.forEach { objetivo ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = state.seleccionadoId == objetivo.id,
                    onClick = { viewModel.seleccionar(objetivo.id) }
                )
                Text("${nombreAsignatura(objetivo.asignatura)} · ${objetivo.unidad}")
            }
        }
        MensajeErrorCampo(state.errores.objetivo)

        Text("2. Datos de la guía", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = state.titulo,
            onValueChange = viewModel::onTituloChanged,
            label = { Text("Título de la guía") },
            leadingIcon = { Icon(Icons.Filled.Title, contentDescription = null) },
            trailingIcon = {
                if (state.errores.titulo != null) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", tint = ErrorRojo)
                }
            },
            isError = state.errores.titulo != null,
            supportingText = { state.errores.titulo?.let { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = {
                selectorArchivo.launch(
                    arrayOf(
                        "application/pdf",
                        "image/*",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.AttachFile, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(state.archivoNombre ?: "Elegir archivo (PDF, Word o imagen)")
        }
        MensajeErrorCampo(state.errores.archivo)

        BotonCarga(texto = "Subir guía", cargando = state.guardando, onClick = viewModel::guardar)

        AnimatedVisibility(visible = state.mensaje != null) {
            Card(colors = CardDefaults.cardColors(containerColor = ExitoFondo)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ExitoVerde)
                    Spacer(Modifier.width(8.dp))
                    Text(state.mensaje ?: "", color = ExitoVerde)
                }
            }
        }

        Text("Guías subidas", style = MaterialTheme.typography.titleMedium)
        if (state.guias.isEmpty()) {
            Text("Todavía no hay guías.", color = TintaSuave)
        }
        state.guias.forEach { guia ->
            val objetivo = state.objetivos.find { it.id == guia.objetivoId }
            Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = Petroleo)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(guia.titulo, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${objetivo?.let { nombreAsignatura(it.asignatura) } ?: ""} · ${guia.nombreArchivo}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TintaSuave
                        )
                    }
                    IconButton(onClick = { Archivos.abrir(context, guia.ruta, guia.tipo) }) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Abrir")
                    }
                    IconButton(onClick = { viewModel.eliminar(guia) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = ErrorRojo)
                    }
                }
            }
        }
    }
}

// Mensaje de error con ícono, para los campos que no son de texto
@Composable
fun MensajeErrorCampo(error: String?) {
    if (error != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = ErrorRojo)
            Spacer(Modifier.width(6.dp))
            Text(error, color = ErrorRojo)
        }
    }
}
