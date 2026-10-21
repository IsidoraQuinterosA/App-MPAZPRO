package com.example.mpaz_pro_3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "objetivos": un objetivo de aprendizaje (OA) por unidad y asignatura
@Entity(tableName = "objetivos")
data class ObjetivoAprendizaje(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val asignatura: String,                 // LENGUAJE, MATEMATICA, CIENCIAS o HISTORIA
    val unidad: String,
    val codigo: String,                     // Código oficial del OA
    val descripcion: String,
    val estado: String = "PENDIENTE",       // Revisión UTP: PENDIENTE, APROBADO u OBSERVADO
    val observacion: String = "",           // Comentario de UTP al pedir cambios
    val asignado: Boolean = false,          // true si el docente lo asignó al curso
    val maxIntentos: Int = 2,               // Intentos de la prueba final (lo define el docente)
    val informeValidado: Boolean = false,   // El docente validó el informe pedagógico
    val fechaValidacion: Long = 0
)
