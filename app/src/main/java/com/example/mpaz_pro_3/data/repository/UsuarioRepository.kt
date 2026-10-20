package com.example.mpaz_pro_3.data.repository

import com.example.mpaz_pro_3.data.local.DatosIniciales
import com.example.mpaz_pro_3.data.local.UsuarioDao
import com.example.mpaz_pro_3.data.preferences.SesionDataStore
import com.example.mpaz_pro_3.model.Rol
import com.example.mpaz_pro_3.model.Sesion
import kotlinx.coroutines.flow.Flow

// El ViewModel le pide los datos al repositorio, nunca directo a Room o DataStore
class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val sesionDataStore: SesionDataStore
) {
    val sesion: Flow<Sesion?> = sesionDataStore.sesion

    suspend fun cargarDatosIniciales() {
        if (usuarioDao.contar() == 0) {
            usuarioDao.insertar(DatosIniciales.usuarios)
        }
    }

    // Devuelve la sesión si los datos son correctos, o null si no lo son
    suspend fun iniciarSesion(rut: String, clave: String, rol: Rol): Sesion? {
        cargarDatosIniciales()
        val usuario = usuarioDao.buscar(rut, clave, rol.name) ?: return null
        val sesion = Sesion(usuario.rut, usuario.nombre, rol)
        sesionDataStore.guardar(sesion)
        return sesion
    }

    suspend fun cerrarSesion() {
        sesionDataStore.borrar()
    }
}
