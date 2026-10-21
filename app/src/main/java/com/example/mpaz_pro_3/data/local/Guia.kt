package com.example.mpaz_pro_3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "guias": archivos (PDF, Word o imagen) que sube el docente para cada objetivo
@Entity(tableName = "guias")
data class Guia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val objetivoId: Int,
    val titulo: String,
    val nombreArchivo: String,
    val ruta: String,          // Dónde quedó guardada la copia dentro de la app
    val tipo: String           // Tipo de archivo, por ejemplo application/pdf
)
