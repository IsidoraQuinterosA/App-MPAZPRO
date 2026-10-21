package com.example.mpaz_pro_3.navigation

import com.example.mpaz_pro_3.model.Rol

object Rutas {
    const val ROLES = "roles"
    const val LOGIN = "login/{rol}"
    const val ESTUDIANTE = "estudiante"
    const val DOCENTE = "docente"
    const val UTP = "utp"

    // Pantallas del estudiante (reciben el id del objetivo)
    const val UNIDAD = "unidad/{objetivoId}"

    fun unidad(objetivoId: Int) = "unidad/$objetivoId"

    fun login(rol: Rol) = "login/${rol.name}"

    fun panelDe(rol: Rol) = when (rol) {
        Rol.ESTUDIANTE -> ESTUDIANTE
        Rol.DOCENTE -> DOCENTE
        Rol.UTP -> UTP
    }
}
