package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Una fila del resultado: la pregunta, lo que respondió y la respuesta correcta
data class ResultadoItem(
    val enunciado: String,
    val respuesta: String,
    val correcta: String,
    val esCorrecta: Boolean
)

data class ResultadoUiState(
    val asignatura: String = "",
    val correctas: Int = 0,
    val total: Int = 0,
    val items: List<ResultadoItem> = emptyList(),
    val cargado: Boolean = false
)

class ResultadoViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as MpazApp
    private val intentoId: Long = savedStateHandle.get<Long>("intentoId") ?: 0L

    private val _state = MutableStateFlow(ResultadoUiState())
    val state: StateFlow<ResultadoUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val intento = app.progresoRepository.intento(intentoId).first() ?: return@launch
            val preguntas = app.contenidoRepository.preguntasPrueba(intento.objetivoId).first()
            val respuestas = app.progresoRepository.respuestas(intentoId).first()
            val objetivo = app.contenidoRepository.objetivo(intento.objetivoId).first()

            val items = respuestas.mapNotNull { respuesta ->
                val pregunta = preguntas.find { it.id == respuesta.preguntaId } ?: return@mapNotNull null
                val opciones = listOf(pregunta.opcionA, pregunta.opcionB, pregunta.opcionC)
                ResultadoItem(
                    enunciado = pregunta.enunciado,
                    respuesta = opciones.getOrElse(respuesta.elegida) { "Sin respuesta" },
                    correcta = opciones[pregunta.correcta],
                    esCorrecta = respuesta.esCorrecta
                )
            }

            _state.value = ResultadoUiState(
                asignatura = objetivo?.asignatura ?: "",
                correctas = intento.correctas,
                total = intento.total,
                items = items,
                cargado = true
            )
        }
    }
}
