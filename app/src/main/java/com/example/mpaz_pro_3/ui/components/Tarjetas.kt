package com.example.mpaz_pro_3.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.Petroleo
import com.example.mpaz_pro_3.ui.theme.TintaSuave

// Tarjeta que se achica un poco al presionarla (animación de retroalimentación)
@Composable
fun TarjetaOpcion(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorFondo: Color = Blanco,
    colorTexto: Color = Color.Unspecified,
    colorIcono: Color = Petroleo
) {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(if (presionado) 0.95f else 1f, label = "escalaTarjeta")

    Card(
        onClick = onClick,
        interactionSource = interaccion,
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .scale(escala)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = colorTexto)
                Text(
                    descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (colorTexto == Color.Unspecified) TintaSuave else colorTexto
                )
            }
        }
    }
}

// Tarjeta de color para cada asignatura
@Composable
fun TarjetaAsignatura(
    nombre: String,
    color: Color,
    icono: ImageVector,
    onClick: () -> Unit
) {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(if (presionado) 0.95f else 1f, label = "escalaAsignatura")

    Card(
        onClick = onClick,
        interactionSource = interaccion,
        colors = CardDefaults.cardColors(containerColor = color, contentColor = Blanco),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .scale(escala)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(44.dp))
            Spacer(Modifier.weight(1f))
            Text(nombre, style = MaterialTheme.typography.titleLarge)
            Text("Unidad 1", style = MaterialTheme.typography.labelMedium)
        }
    }
}
