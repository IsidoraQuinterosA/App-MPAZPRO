package com.example.mpaz_pro_3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "preguntas": se usan en los desafíos y en la prueba final
@Entity(tableName = "preguntas")
data class Pregunta(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val objetivoId: Int,
    val tipo: String,          // DESAFIO o PRUEBA
    val nivel: Int,            // Dificultad del desafío: 1, 2 o 3 (en la prueba es 0)
    val enunciado: String,
    val opcionA: String,
    val opcionB: String,
    val opcionC: String,
    val correcta: Int          // 0 = A, 1 = B, 2 = C
)
