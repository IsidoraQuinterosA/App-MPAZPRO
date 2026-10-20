package com.example.mpaz_pro_3

import android.app.Application
import com.example.mpaz_pro_3.data.local.AppDatabase
import com.example.mpaz_pro_3.data.preferences.SesionDataStore
import com.example.mpaz_pro_3.data.repository.UsuarioRepository

// Se crea una sola vez al abrir la app.
// Aquí se crean la base de datos y el repositorio para que toda la app use los mismos.
class MpazApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    val usuarioRepository by lazy {
        UsuarioRepository(database.usuarioDao(), SesionDataStore(this))
    }
}
