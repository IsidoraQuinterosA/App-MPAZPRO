package com.example.mpaz_pro_3.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

// Funciones para guardar archivos elegidos por el usuario dentro de la app
object Archivos {

    // Copia una imagen de la galería a la carpeta interna de la app.
    // La achica a un máximo de 1280 px para que no ocupe mucho espacio.
    fun guardarImagen(context: Context, uri: Uri): String? {
        val resolver = context.contentResolver

        // 1. Leer solo el tamaño de la imagen
        val tamano = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, tamano) }

        // 2. Calcular cuánto hay que reducirla
        var escala = 1
        while (tamano.outWidth / escala > 1280 || tamano.outHeight / escala > 1280) {
            escala *= 2
        }

        // 3. Leer la imagen reducida y guardarla como JPG
        val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opciones)
        } ?: return null

        val carpeta = File(context.filesDir, "imagenes").apply { mkdirs() }
        val archivo = File(carpeta, "imagen_${System.currentTimeMillis()}.jpg")
        FileOutputStream(archivo).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        return archivo.absolutePath
    }
}
