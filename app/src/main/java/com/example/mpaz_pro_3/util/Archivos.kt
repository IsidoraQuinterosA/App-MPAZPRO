package com.example.mpaz_pro_3.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.core.content.FileProvider
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

    // Lee el nombre y el tamaño (en bytes) de un archivo elegido con el selector
    fun nombreYTamano(context: Context, uri: Uri): Pair<String, Long> {
        var nombre = "archivo"
        var tamano = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val columnaNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val columnaTamano = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (columnaNombre >= 0) nombre = cursor.getString(columnaNombre)
                if (columnaTamano >= 0) tamano = cursor.getLong(columnaTamano)
            }
        }
        return Pair(nombre, tamano)
    }

    fun tipo(context: Context, uri: Uri): String =
        context.contentResolver.getType(uri) ?: "application/octet-stream"

    // Copia el archivo elegido a la carpeta interna "guias" de la app
    fun guardarArchivo(context: Context, uri: Uri, nombre: String): String? {
        val carpeta = File(context.filesDir, "guias").apply { mkdirs() }
        val destino = File(carpeta, "${System.currentTimeMillis()}_$nombre")
        val entrada = context.contentResolver.openInputStream(uri) ?: return null
        entrada.use { input ->
            FileOutputStream(destino).use { output -> input.copyTo(output) }
        }
        return destino.absolutePath
    }

    // Abre un archivo guardado con otra app del teléfono (por ejemplo, un lector de PDF)
    fun abrir(context: Context, ruta: String, tipo: String) {
        val archivo = File(ruta)
        if (!archivo.exists()) {
            Toast.makeText(context, "El archivo ya no existe", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, tipo)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Abrir con"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No hay una app para abrir este archivo", Toast.LENGTH_SHORT).show()
        }
    }
}
