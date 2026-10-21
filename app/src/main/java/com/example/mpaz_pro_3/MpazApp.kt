package com.example.mpaz_pro_3

import android.app.Application
import com.example.mpaz_pro_3.data.local.AppDatabase
import com.example.mpaz_pro_3.data.preferences.SesionDataStore
import com.example.mpaz_pro_3.data.repository.ContenidoRepository
import com.example.mpaz_pro_3.data.repository.ProgresoRepository
import com.example.mpaz_pro_3.data.repository.UsuarioRepository

// Se crea una sola vez al abrir la app.
// Aquí se crean la base de datos y los repositorios para que toda la app use los mismos.
class MpazApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    val usuarioRepository by lazy {
        UsuarioRepository(database.usuarioDao(), SesionDataStore(this))
    }

    val contenidoRepository by lazy { ContenidoRepository(database.contenidoDao()) }

    val progresoRepository by lazy { ProgresoRepository(database.progresoDao()) }
}
