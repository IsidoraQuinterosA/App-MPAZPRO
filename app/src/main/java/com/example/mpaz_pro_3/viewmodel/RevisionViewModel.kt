package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.data.local.Contenido
import com.example.mpaz_pro_3.data.local.ObjetivoAprendizaje
import com.example.mpaz_pro_3.data.local.Pregunta
import com.example.mpaz_pro_3.util.Validaciones
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RevisionUiState(
    val objetivo: ObjetivoAprendizaje? = null,
    val contenido: Contenido? = null,
    val preguntas: List<Pregunta> = emptyList(),
    val observacion: String = "",
    val error: String? = null,
    val procesando: Boolean = false,
    val mensaje: String? = null     // Cuando tiene valor, la revisión terminó
)

class RevisionViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as MpazApp
    private val objetivoId: Int = savedStateHandle.get<Int>("objetivoId") ?: 0

    private val _state = MutableStateFlow(RevisionUiState())
    val state: StateFlow<RevisionUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                app.contenidoRepository.objetivo(objetivoId),
                app.contenidoRepository.contenidos(objetivoId),
                app.contenidoRepository.desafios(objetivoId),
                app.contenidoRepository.preguntasPrueba(objetivoId)
            ) { objetivo, contenidos, desafios, prueba ->
                Triple(objetivo, contenidos.firstOrNull(), desafios + prueba)
            }.collect { (objetivo, contenido, preguntas) ->
                _state.update { it.copy(objetivo = objetivo, contenido = contenido, preguntas = preguntas) }
            }
        }
    }

    fun onObservacionChanged(texto: String) {
        _state.update { it.copy(observacion = texto, error = null) }
    }

    fun aprobar() {
        val objetivo = _state.value.objetivo ?: return
        if (_state.value.procesando) return
        viewModelScope.launch {
            _state.update { it.copy(procesando = true) }
            delay(600)
            app.contenidoRepository.actualizarObjetivo(objetivo.copy(estado = "APROBADO", observacion = ""))
            _state.update { it.copy(procesando = false, mensaje = "Contenido aprobado") }
        }
    }

    fun pedirCambios() {
        val actual = _state.value
        val objetivo = actual.objetivo ?: return
        if (actual.procesando) return

        val error = Validaciones.validarObservacion(actual.observacion)
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(procesando = true) }
            delay(600)
            // Un contenido con observaciones deja de estar disponible para los estudiantes
            app.contenidoRepository.actualizarObjetivo(
                objetivo.copy(estado = "OBSERVADO", observacion = actual.observacion.trim(), asignado = false)
            )
            _state.update { it.copy(procesando = false, mensaje = "Observaciones enviadas al docente") }
        }
    }
}
