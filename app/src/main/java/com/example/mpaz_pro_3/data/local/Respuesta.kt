package com.example.mpaz_pro_3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "respuestas": lo que respondió el estudiante en cada pregunta de un intento
@Entity(tableName = "respuestas")
data class Respuesta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val intentoId: Long,
    val preguntaId: Int,
    val elegida: Int,          // 0 = A, 1 = B, 2 = C
    val esCorrecta: Boolean
)
