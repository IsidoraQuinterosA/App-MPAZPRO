package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.data.local.Pregunta
import com.example.mpaz_pro_3.data.local.Progreso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DesafiosUiState(
    val preguntas: List<Pregunta> = emptyList(),
    val asignatura: String = "",
    val nivelActual: Int = 1,          // 1, 2 o 3. Si es 4, ya terminó los desafíos
    val seleccion: Int? = null,        // Alternativa elegida
    val esCorrecta: Boolean? = null,   // null = todavía no responde
    val cargado: Boolean = false
) {
    val pregunta: Pregunta? get() = preguntas.find { it.nivel == nivelActual }
}

class DesafiosViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as MpazApp
    private val objetivoId: Int = savedStateHandle.get<Int>("objetivoId") ?: 0
    private var rut = ""

    private val _state = MutableStateFlow(DesafiosUiState())
    val state: StateFlow<DesafiosUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            rut = app.usuarioRepository.sesion.first()?.rut ?: ""
            val progreso = app.progresoRepository.progreso(rut, objetivoId).first()
            val preguntas = app.contenidoRepository.desafios(objetivoId).first()
            val objetivo = app.contenidoRepository.objetivo(objetivoId).first()

            _state.update {
                it.copy(
                    preguntas = preguntas,
                    asignatura = objetivo?.asignatura ?: "",
                    // Parte en el nivel siguiente al último superado
                    nivelActual = minOf((progreso?.nivelSuperado ?: 0) + 1, 4),
                    cargado = true
                )
            }
        }
    }

    fun elegir(opcion: Int) {
        val actual = _state.value
        val pregunta = actual.pregunta ?: return
        if (actual.esCorrecta == true) return  // Ya respondió bien este nivel

        val correcta = opcion == pregunta.correcta
        _state.update { it.copy(seleccion = opcion, esCorrecta = correcta) }

        if (correcta) {
            // Guarda en Room que superó este nivel
            viewModelScope.launch {
                val progreso = app.progresoRepository.progreso(rut, objetivoId).first()
                    ?: Progreso(rutEstudiante = rut, objetivoId = objetivoId)
                if (progreso.nivelSuperado < actual.nivelActual) {
                    app.progresoRepository.guardarProgreso(progreso.copy(nivelSuperado = actual.nivelActual))
                }
            }
        }
    }

    fun siguienteNivel() {
        _state.update { it.copy(nivelActual = it.nivelActual + 1, seleccion = null, esCorrecta = null) }
    }
}
