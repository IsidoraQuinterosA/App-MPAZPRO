package com.example.mpaz_pro_3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "usuarios" de la base de datos local
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val rut: String,   // Formato: 25418732-1
    val nombre: String,
    val rol: String,               // ESTUDIANTE, DOCENTE o UTP
    val clave: String              // PIN o contraseña
)
