package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.data.local.Contenido
import com.example.mpaz_pro_3.data.local.Progreso
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AprendeUiState(
    val contenido: Contenido? = null,
    val asignatura: String = "",
    val guardando: Boolean = false,
    val terminado: Boolean = false
)

class AprendeViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as MpazApp
    private val objetivoId: Int = savedStateHandle.get<Int>("objetivoId") ?: 0

    private val _state = MutableStateFlow(AprendeUiState())
    val state: StateFlow<AprendeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                app.contenidoRepository.contenidos(objetivoId),
                app.contenidoRepository.objetivo(objetivoId)
            ) { contenidos, objetivo ->
                Pair(contenidos.firstOrNull(), objetivo?.asignatura ?: "")
            }.collect { (contenido, asignatura) ->
                _state.update { it.copy(contenido = contenido, asignatura = asignatura) }
            }
        }
    }

    // Marca la sección "Aprende" como terminada en el progreso del estudiante
    fun terminar() {
        if (_state.value.guardando) return
        viewModelScope.launch {
            _state.update { it.copy(guardando = true) }
            delay(600)
            val rut = app.usuarioRepository.sesion.first()?.rut ?: ""
            val actual = app.progresoRepository.progreso(rut, objetivoId).first()
                ?: Progreso(rutEstudiante = rut, objetivoId = objetivoId)
            app.progresoRepository.guardarProgreso(actual.copy(aprendeVisto = true))
            _state.update { it.copy(guardando = false, terminado = true) }
        }
    }
}
