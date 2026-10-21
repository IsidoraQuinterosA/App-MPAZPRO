package com.example.mpaz_pro_3.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.ui.components.BotonCarga
import com.example.mpaz_pro_3.ui.components.EtiquetaEstado
import com.example.mpaz_pro_3.ui.components.ImagenArchivo
import com.example.mpaz_pro_3.ui.components.PantallaConVolver
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.Borde
import com.example.mpaz_pro_3.ui.theme.ErrorRojo
import com.example.mpaz_pro_3.ui.theme.ExitoFondo
import com.example.mpaz_pro_3.ui.theme.ExitoVerde
import com.example.mpaz_pro_3.ui.theme.Petroleo
import com.example.mpaz_pro_3.ui.theme.colorAsignatura
import com.example.mpaz_pro_3.ui.theme.iconoAsignatura
import com.example.mpaz_pro_3.ui.theme.nombreAsignatura
import com.example.mpaz_pro_3.viewmodel.AsignarViewModel

@Composable
fun AsignarScreen(
    onVolver: () -> Unit,
    viewModel: AsignarViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PantallaConVolver(titulo = "Asignar contenidos", onVolver = onVolver) {
        Text("1. Elige un objetivo de aprendizaje", style = MaterialTheme.typography.titleMedium)

        state.objetivos.forEach { objetivo ->
            val elegido = objetivo.id == state.seleccionadoId
            Card(
                onClick = { viewModel.seleccionar(objetivo) },
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(2.dp, if (elegido) Petroleo else Borde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    RadioButton(selected = elegido, onClick = { viewModel.seleccionar(objetivo) })
                    Icon(iconoAsignatura(objetivo.asignatura), contentDescription = null, tint = colorAsignatura(objetivo.asignatura))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(nombreAsignatura(objetivo.asignatura), style = MaterialTheme.typography.titleMedium)
                        Text(objetivo.unidad, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (objetivo.asignado) "Asignado · ${objetivo.maxIntentos} intento(s)" else "Sin asignar",
                            style = MaterialTheme.typography.labelMedium
                        )
                        // Si UTP pidió cambios, el docente ve la observación
                        if (objetivo.estado == "OBSERVADO" && objetivo.observacion.isNotEmpty()) {
                            Text("Observación UTP: ${objetivo.observacion}", color = ErrorRojo, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    EtiquetaEstado(objetivo.estado)
                }
            }
        }

        state.errores.objetivo?.let { error ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = ErrorRojo)
                Spacer(Modifier.width(6.dp))
                Text(error, color = ErrorRojo)
            }
        }

        Text("2. Intentos de la prueba final", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = state.intentos,
            onValueChange = viewModel::onIntentosChanged,
            label = { Text("Cantidad de intentos (1 a 5)") },
            leadingIcon = { Icon(Icons.Filled.Repeat, contentDescription = null) },
            trailingIcon = {
                if (state.errores.intentos != null) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", tint = ErrorRojo)
                }
            },
            isError = state.errores.intentos != null,
            supportingText = { state.errores.intentos?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Asignar al curso 3.º A", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Switch(checked = state.asignado, onCheckedChange = viewModel::onAsignadoChanged)
        }

        BotonCarga(texto = "Guardar asignación", cargando = state.guardando, onClick = viewModel::guardar)

        // Recurso nativo Galería: el selector de fotos de Android (no necesita permisos)
        val selectorImagen = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) viewModel.guardarImagen(uri)
        }

        Text("3. Imagen de apoyo para \"Aprende\"", style = MaterialTheme.typography.titleMedium)
        if (state.imagen.isNotEmpty()) {
            ImagenArchivo(
                ruta = state.imagen,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.large)
            )
        }
        OutlinedButton(
            onClick = {
                selectorImagen.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            enabled = !state.guardandoImagen,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (state.guardandoImagen) "Guardando imagen..." else "Elegir imagen de la galería")
        }

        AnimatedVisibility(visible = state.mensaje != null) {
            Card(colors = CardDefaults.cardColors(containerColor = ExitoFondo)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ExitoVerde)
                    Spacer(Modifier.width(8.dp))
                    Text(state.mensaje ?: "", color = ExitoVerde)
                }
            }
        }
    }
}
