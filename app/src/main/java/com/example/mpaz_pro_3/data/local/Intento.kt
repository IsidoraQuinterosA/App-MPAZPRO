package com.example.mpaz_pro_3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "intentos": cada vez que un estudiante entrega la prueba final
@Entity(tableName = "intentos")
data class Intento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rutEstudiante: String,
    val objetivoId: Int,
    val correctas: Int,
    val total: Int,
    val fecha: Long = System.currentTimeMillis()
)
