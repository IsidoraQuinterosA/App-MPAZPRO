package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.data.local.ObjetivoAprendizaje
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class UnidadUiState(
    val objetivo: ObjetivoAprendizaje? = null,
    val aprendeVisto: Boolean = false,
    val nivelSuperado: Int = 0,
    val intentosUsados: Int = 0,
    val ultimoIntentoId: Long? = null
)

class UnidadViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as MpazApp

    // El id llega como argumento de la ruta "unidad/{objetivoId}"
    private val objetivoId: Int = savedStateHandle.get<Int>("objetivoId") ?: 0

    private val _state = MutableStateFlow(UnidadUiState())
    val state: StateFlow<UnidadUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val rut = app.usuarioRepository.sesion.first()?.rut ?: ""

            // combine junta varios Flow de Room: si cualquiera cambia, el estado se actualiza
            combine(
                app.contenidoRepository.objetivo(objetivoId),
                app.progresoRepository.progreso(rut, objetivoId),
                app.progresoRepository.intentos(rut, objetivoId)
            ) { objetivo, progreso, intentos ->
                UnidadUiState(
                    objetivo = objetivo,
                    aprendeVisto = progreso?.aprendeVisto ?: false,
                    nivelSuperado = progreso?.nivelSuperado ?: 0,
                    intentosUsados = intentos.size,
                    ultimoIntentoId = intentos.firstOrNull()?.id
                )
            }.collect { _state.value = it }
        }
    }
}
