package com.example.mpaz_pro_3.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

// Muestra una imagen guardada dentro de la app. Devuelve false si no se pudo leer.
@Composable
fun ImagenArchivo(ruta: String, modifier: Modifier = Modifier): Boolean {
    val bitmap = remember(ruta) { BitmapFactory.decodeFile(ruta)?.asImageBitmap() }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Imagen de apoyo",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
    return bitmap != null
}
