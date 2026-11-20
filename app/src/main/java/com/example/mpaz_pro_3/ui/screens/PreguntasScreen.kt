package com.example.mpaz_pro_3.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.data.local.Pregunta
import com.example.mpaz_pro_3.ui.components.BotonCarga
import com.example.mpaz_pro_3.ui.components.PantallaConVolver
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.ErrorRojo
import com.example.mpaz_pro_3.ui.theme.ExitoFondo
import com.example.mpaz_pro_3.ui.theme.ExitoVerde
import com.example.mpaz_pro_3.ui.theme.Petroleo
import com.example.mpaz_pro_3.ui.theme.TintaSuave
import com.example.mpaz_pro_3.ui.theme.nombreAsignatura
import com.example.mpaz_pro_3.viewmodel.PreguntasViewModel

// CRUD de preguntas del docente: crear, ver, editar y eliminar (microservicio ms-contenidos)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreguntasScreen(
    onVolver: () -> Unit,
    viewModel: PreguntasViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var porEliminar by remember { mutableStateOf<Pregunta?>(null) }

    PantallaConVolver(titulo = "Preguntas", onVolver = onVolver) {
        Text("Objetivo", style = MaterialTheme.typography.titleMedium)
        state.objetivos.forEach { objetivo ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = objetivo.id == state.seleccionadoId,
                    onClick = { viewModel.seleccionarObjetivo(objetivo.id) }
                )
                Text("${nombreAsignatura(objetivo.asignatura)} · ${objetivo.unidad}")
            }
        }

        // Mensajes de éxito o de error del servidor
        AnimatedVisibility(visible = state.mensaje != null) {
            Card(colors = CardDefaults.cardColors(containerColor = ExitoFondo)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ExitoVerde)
                    Spacer(Modifier.width(8.dp))
                    Text(state.mensaje ?: "", color = ExitoVerde)
                }
            }
        }
        MensajeErrorCampo(state.errorServidor)

        if (!state.formularioAbierto) {
            OutlinedButton(onClick = viewModel::nuevaPregunta, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nueva pregunta")
            }
        }

        // Formulario para crear o editar (aparece con animación)
        AnimatedVisibility(
            visible = state.formularioAbierto,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        if (state.editandoId == 0) "Nueva pregunta" else "Editar pregunta",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Tipo", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.tipo == "DESAFIO",
                            onClick = { viewModel.onTipoChanged("DESAFIO") },
                            label = { Text("Desafío") }
                        )
                        FilterChip(
                            selected = state.tipo == "PRUEBA",
                            onClick = { viewModel.onTipoChanged("PRUEBA") },
                            label = { Text("Prueba final") }
                        )
                    }

                    if (state.tipo == "DESAFIO") {
                        Text("Nivel de dificultad", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1 to "Fácil", 2 to "Medio", 3 to "Difícil").forEach { (nivel, nombre) ->
                                FilterChip(
                                    selected = state.nivel == nivel,
                                    onClick = { viewModel.onNivelChanged(nivel) },
                                    label = { Text(nombre) }
                                )
                            }
                        }
                        MensajeErrorCampo(state.errores.nivel)
                    }

                    OutlinedTextField(
                        value = state.enunciado,
                        onValueChange = viewModel::onEnunciadoChanged,
                        label = { Text("Pregunta") },
                        leadingIcon = { Icon(Icons.Filled.QuestionMark, contentDescription = null) },
                        trailingIcon = {
                            if (state.errores.enunciado != null) {
                                Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", tint = ErrorRojo)
                            }
                        },
                        isError = state.errores.enunciado != null,
                        supportingText = { state.errores.enunciado?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Alternativas (marca la correcta)", style = MaterialTheme.typography.labelLarge)
                    val opciones = listOf(state.opcionA, state.opcionB, state.opcionC)
                    val errores = listOf(state.errores.opcionA, state.errores.opcionB, state.errores.opcionC)
                    opciones.forEachIndexed { i, texto ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = state.correcta == i,
                                onClick = { viewModel.onCorrectaChanged(i) }
                            )
                            OutlinedTextField(
                                value = texto,
                                onValueChange = { viewModel.onOpcionChanged(i, it) },
                                label = { Text("Alternativa ${"ABC"[i]}") },
                                trailingIcon = {
                                    if (errores[i] != null) {
                                        Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", tint = ErrorRojo)
                                    }
                                },
                                isError = errores[i] != null,
                                supportingText = { errores[i]?.let { Text(it) } },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    MensajeErrorCampo(state.errores.correcta)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = viewModel::cerrarFormulario,
                            enabled = !state.guardando,
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancelar") }
                        BotonCarga(
                            texto = "Guardar",
                            cargando = state.guardando,
                            icono = Icons.Filled.Save,
                            onClick = viewModel::guardar,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Text("Preguntas guardadas (${state.preguntas.size})", style = MaterialTheme.typography.titleMedium)
        if (state.preguntas.isEmpty()) {
            Text("Este objetivo aún no tiene preguntas.", color = TintaSuave)
        }
        state.preguntas.forEach { pregunta ->
            val opciones = listOf(pregunta.opcionA, pregunta.opcionB, pregunta.opcionC)
            Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (pregunta.tipo == "DESAFIO") "Desafío · nivel ${pregunta.nivel}" else "Prueba final",
                            style = MaterialTheme.typography.labelMedium,
                            color = Petroleo
                        )
                        Text(pregunta.enunciado, style = MaterialTheme.typography.titleMedium)
                        Text("Correcta: ${opciones.getOrElse(pregunta.correcta) { "" }}", color = ExitoVerde)
                    }
                    IconButton(onClick = { viewModel.editarPregunta(pregunta) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Petroleo)
                    }
                    IconButton(onClick = { porEliminar = pregunta }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = ErrorRojo)
                    }
                }
            }
        }
    }

    porEliminar?.let { pregunta ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar pregunta?") },
            text = { Text("\"${pregunta.enunciado}\" se eliminará del servidor.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(pregunta)
                    porEliminar = null
                }) { Text("Eliminar", color = ErrorRojo) }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}
