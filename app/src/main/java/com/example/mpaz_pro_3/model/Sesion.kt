package com.example.mpaz_pro_3.model

// Datos de la persona que inició sesión (se guardan con DataStore)
data class Sesion(
    val rut: String,
    val nombre: String,
    val rol: Rol
)
