package com.example.mpaz_pro_3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "contenidos": la explicación que el estudiante ve en "Aprende"
@Entity(tableName = "contenidos")
data class Contenido(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val objetivoId: Int,       // A qué objetivo pertenece
    val titulo: String,
    val explicacion: String,
    val ejemplo: String,
    val imagen: String = ""    // Ruta de la imagen de apoyo elegida por el docente
)
