package com.example.mpaz_pro_3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.ui.components.PantallaConVolver
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.Petroleo
import com.example.mpaz_pro_3.ui.theme.TintaSuave
import com.example.mpaz_pro_3.ui.theme.colorAsignatura
import com.example.mpaz_pro_3.ui.theme.nombreAsignatura
import com.example.mpaz_pro_3.util.Archivos
import com.example.mpaz_pro_3.viewmodel.UnidadViewModel

@Composable
fun UnidadScreen(
    onVolver: () -> Unit,
    onAprende: () -> Unit,
    onDesafios: () -> Unit,
    onPrueba: () -> Unit,
    onVerResultado: (Long) -> Unit,
    viewModel: UnidadViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val objetivo = state.objetivo
    val color = objetivo?.let { colorAsignatura(it.asignatura) } ?: Petroleo

    PantallaConVolver(
        titulo = objetivo?.let { nombreAsignatura(it.asignatura) } ?: "",
        onVolver = onVolver,
        color = color
    ) {
        if (objetivo == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            return@PantallaConVolver
        }

        val desafiosListos = state.nivelSuperado >= 3
        val quedanIntentos = state.intentosUsados < objetivo.maxIntentos

        // Información del objetivo de aprendizaje
        Card(colors = CardDefaults.cardColors(containerColor = color)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(objetivo.unidad, style = MaterialTheme.typography.titleLarge, color = Blanco)
                Spacer(Modifier.size(6.dp))
                Text("Objetivo: ${objetivo.descripcion}", color = Blanco)
                Text("Código: ${objetivo.codigo}", style = MaterialTheme.typography.labelMedium, color = Blanco)
            }
        }

        Text("Tu camino", style = MaterialTheme.typography.titleMedium)

        PasoCard(
            numero = 1,
            titulo = "Aprende",
            descripcion = "Lee la explicación y mira el ejemplo",
            estado = if (state.aprendeVisto) "Listo" else "Empezar",
            completado = state.aprendeVisto,
            habilitado = true,
            color = color,
            onClick = onAprende
        )
        PasoCard(
            numero = 2,
            titulo = "Desafíos",
            descripcion = "3 niveles, de fácil a difícil",
            estado = if (desafiosListos) "Listo" else "Nivel ${state.nivelSuperado + 1} de 3",
            completado = desafiosListos,
            habilitado = true,
            color = color,
            onClick = onDesafios
        )
        PasoCard(
            numero = 3,
            titulo = "Prueba final",
            descripcion = "Intentos usados: ${state.intentosUsados} de ${objetivo.maxIntentos}",
            estado = when {
                !desafiosListos -> "Bloqueada"
                !quedanIntentos -> "Sin intentos"
                else -> "Empezar"
            },
            completado = state.intentosUsados > 0,
            habilitado = desafiosListos && quedanIntentos,
            color = color,
            onClick = onPrueba
        )

        if (!desafiosListos) {
            Text(
                "Supera los 3 niveles de desafíos para desbloquear la prueba final.",
                style = MaterialTheme.typography.bodyMedium,
                color = TintaSuave
            )
        }

        // Guías que subió el docente para este objetivo
        if (state.guias.isNotEmpty()) {
            Text("Guías de tu profesor", style = MaterialTheme.typography.titleMedium)
            state.guias.forEach { guia ->
                Card(
                    onClick = { Archivos.abrir(context, guia.ruta, guia.tipo) },
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = color)
                        Spacer(Modifier.width(10.dp))
                        Text(guia.titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text("Abrir", color = color, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        state.ultimoIntentoId?.let { id ->
            OutlinedButton(onClick = { onVerResultado(id) }, modifier = Modifier.fillMaxWidth()) {
                Text("Ver mi último resultado")
            }
        }
    }
}

// Tarjeta de cada paso del camino. Si no está habilitada se ve más clara y no responde.
@Composable
private fun PasoCard(
    numero: Int,
    titulo: String,
    descripcion: String,
    estado: String,
    completado: Boolean,
    habilitado: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = habilitado,
        colors = CardDefaults.cardColors(containerColor = Blanco, disabledContainerColor = Blanco),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (habilitado) 1f else 0.5f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color)
            ) {
                if (completado) {
                    Icon(Icons.Filled.Check, contentDescription = "Completado", tint = Blanco)
                } else {
                    Text("$numero", color = Blanco, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(descripcion, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
            }
            Text(estado, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}
