package com.example.mpaz_pro_3.model

data class LoginUiState(
    val rut: String = "",
    val clave: String = "",              // PIN para estudiantes, contraseña para adultos
    val mostrarClave: Boolean = false,
    val errores: LoginErrores = LoginErrores(),
    val mensajeError: String? = null,    // Error general, por ejemplo datos incorrectos
    val cargando: Boolean = false,
    val ingresoExitoso: Boolean = false
)
