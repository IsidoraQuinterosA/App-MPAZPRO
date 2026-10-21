package com.example.mpaz_pro_3.data.local

import androidx.room.Entity

// Tabla "progreso": avance de cada estudiante en cada objetivo
@Entity(tableName = "progreso", primaryKeys = ["rutEstudiante", "objetivoId"])
data class Progreso(
    val rutEstudiante: String,
    val objetivoId: Int,
    val aprendeVisto: Boolean = false,  // Terminó la sección "Aprende"
    val nivelSuperado: Int = 0          // Último nivel de desafío superado (0 a 3)
)
