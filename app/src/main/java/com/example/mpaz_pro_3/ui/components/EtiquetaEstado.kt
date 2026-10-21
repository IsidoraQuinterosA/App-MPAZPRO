package com.example.mpaz_pro_3.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mpaz_pro_3.ui.theme.ErrorFondo
import com.example.mpaz_pro_3.ui.theme.ErrorRojo
import com.example.mpaz_pro_3.ui.theme.ExitoFondo
import com.example.mpaz_pro_3.ui.theme.ExitoVerde

// Etiqueta de color con el estado de revisión UTP de un objetivo
@Composable
fun EtiquetaEstado(estado: String) {
    val (texto, fondo, color) = when (estado) {
        "APROBADO" -> Triple("Aprobado", ExitoFondo, ExitoVerde)
        "OBSERVADO" -> Triple("Con observaciones", ErrorFondo, ErrorRojo)
        else -> Triple("Pendiente", Color(0xFFFEF3C7), Color(0xFF92400E))
    }
    Surface(color = fondo, shape = RoundedCornerShape(50)) {
        Text(
            text = texto,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
