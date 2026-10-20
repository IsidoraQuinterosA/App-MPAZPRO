package com.example.mpaz_pro_3.model

// Mensaje de error de cada campo. null = el campo está correcto
data class LoginErrores(
    val rut: String? = null,
    val clave: String? = null
)
