package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.model.Sesion
import com.example.mpaz_pro_3.navigation.Rutas
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MpazApp).usuarioRepository

    // Sesión guardada en DataStore (null si nadie ha iniciado sesión)
    val sesion: StateFlow<Sesion?> = repository.sesion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Pantalla con la que parte la app. Es null mientras se lee DataStore.
    private val _rutaInicial = MutableStateFlow<String?>(null)
    val rutaInicial: StateFlow<String?> = _rutaInicial.asStateFlow()

    private val _cerrando = MutableStateFlow(false)
    val cerrando: StateFlow<Boolean> = _cerrando.asStateFlow()

    init {
        viewModelScope.launch {
            repository.cargarDatosIniciales()
            val sesionGuardada = repository.sesion.first()
            _rutaInicial.value = sesionGuardada?.let { Rutas.panelDe(it.rol) } ?: Rutas.ROLES
        }
    }

    fun cerrarSesion(alTerminar: () -> Unit) {
        viewModelScope.launch {
            _cerrando.value = true
            delay(500)
            repository.cerrarSesion()
            _cerrando.value = false
            alTerminar()
        }
    }
}
