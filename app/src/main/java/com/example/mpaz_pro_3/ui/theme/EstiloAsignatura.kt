package com.example.mpaz_pro_3.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Public
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mpaz_pro_3.model.Asignatura

// Color, ícono y nombre de cada asignatura a partir de su clave (LENGUAJE, MATEMATICA...)
fun colorAsignatura(clave: String): Color = when (clave) {
    Asignatura.LENGUAJE.name -> LenguajeColor
    Asignatura.MATEMATICA.name -> MatematicaColor
    Asignatura.CIENCIAS.name -> CienciasColor
    else -> HistoriaColor
}

fun iconoAsignatura(clave: String): ImageVector = when (clave) {
    Asignatura.LENGUAJE.name -> Icons.AutoMirrored.Filled.MenuBook
    Asignatura.MATEMATICA.name -> Icons.Filled.Calculate
    Asignatura.CIENCIAS.name -> Icons.Filled.Eco
    else -> Icons.Filled.Public
}

fun nombreAsignatura(clave: String): String =
    Asignatura.entries.find { it.name == clave }?.nombre ?: clave
