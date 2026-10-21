package com.example.mpaz_pro_3.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.Borde
import com.example.mpaz_pro_3.ui.theme.CelesteClaro
import com.example.mpaz_pro_3.ui.theme.ErrorFondo
import com.example.mpaz_pro_3.ui.theme.ErrorRojo
import com.example.mpaz_pro_3.ui.theme.ExitoFondo
import com.example.mpaz_pro_3.ui.theme.ExitoVerde
import com.example.mpaz_pro_3.ui.theme.Petroleo

enum class EstadoOpcion { NORMAL, SELECCIONADA, CORRECTA, INCORRECTA }

// Botón de una alternativa (A, B o C). El color cambia con animación según el estado.
@Composable
fun OpcionRespuesta(
    letra: String,
    texto: String,
    estado: EstadoOpcion,
    onClick: () -> Unit,
    habilitada: Boolean = true
) {
    val fondo by animateColorAsState(
        targetValue = when (estado) {
            EstadoOpcion.CORRECTA -> ExitoFondo
            EstadoOpcion.INCORRECTA -> ErrorFondo
            EstadoOpcion.SELECCIONADA -> CelesteClaro
            EstadoOpcion.NORMAL -> Blanco
        },
        label = "fondoOpcion"
    )
    val borde by animateColorAsState(
        targetValue = when (estado) {
            EstadoOpcion.CORRECTA -> ExitoVerde
            EstadoOpcion.INCORRECTA -> ErrorRojo
            EstadoOpcion.SELECCIONADA -> Petroleo
            EstadoOpcion.NORMAL -> Borde
        },
        label = "bordeOpcion"
    )

    Card(
        onClick = onClick,
        enabled = habilitada,
        colors = CardDefaults.cardColors(containerColor = fondo, disabledContainerColor = fondo),
        border = BorderStroke(3.dp, borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Text(letra, style = MaterialTheme.typography.titleLarge, color = Petroleo)
            Spacer(Modifier.width(14.dp))
            Text(texto, style = MaterialTheme.typography.titleMedium)
        }
    }
}
