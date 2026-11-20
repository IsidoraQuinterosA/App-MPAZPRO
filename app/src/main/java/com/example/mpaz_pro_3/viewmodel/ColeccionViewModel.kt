package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.data.local.Recompensa
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Colección de Pokémon ganados por el estudiante que inició sesión
class ColeccionViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MpazApp

    private val _recompensas = MutableStateFlow<List<Recompensa>>(emptyList())
    val recompensas: StateFlow<List<Recompensa>> = _recompensas.asStateFlow()

    init {
        viewModelScope.launch {
            val rut = app.usuarioRepository.sesion.first()?.rut ?: ""
            app.recompensaRepository.coleccion(rut).collect { _recompensas.value = it }
        }
    }
}
