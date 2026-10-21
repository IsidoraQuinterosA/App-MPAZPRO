package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.data.local.ObjetivoAprendizaje
import com.example.mpaz_pro_3.util.Validaciones
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PreguntaInforme(val numero: Int, val enunciado: String, val porcentaje: Int)

data class EstudianteInforme(val rut: String, val mejorPuntaje: String, val intentos: Int)

data class InformeUiState(
    val objetivos: List<ObjetivoAprendizaje> = emptyList(),
    val seleccionadoId: Int? = null,
    val estudiantes: Int = 0,
    val totalIntentos: Int = 0,
    val promedio: Int = 0,                         // % de respuestas correctas
    val preguntas: List<PreguntaInforme> = emptyList(),
    val porEstudiante: List<EstudianteInforme> = emptyList(),
    val revisado: Boolean = false,
    val errorRevisado: String? = null,
    val validando: Boolean = false,
    val mensaje: String? = null
) {
    val objetivo: ObjetivoAprendizaje? get() = objetivos.find { it.id == seleccionadoId }

    // Fortaleza: 60% o más del curso respondió bien. A reforzar: menos del 60%.
    val fortalezas: List<PreguntaInforme> get() = preguntas.filter { it.porcentaje >= 60 }
    val aReforzar: List<PreguntaInforme> get() = preguntas.filter { it.porcentaje < 60 }
}

class InformeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MpazApp
    private var trabajoInforme: Job? = null

    private val _state = MutableStateFlow(InformeUiState())
    val state: StateFlow<InformeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            app.contenidoRepository.objetivos.collect { lista ->
                _state.update { it.copy(objetivos = lista) }
                if (_state.value.seleccionadoId == null && lista.isNotEmpty()) {
                    seleccionar(lista.first().id)
                }
            }
        }
    }

    // Calcula el informe del objetivo elegido a partir de los intentos y respuestas guardados
    fun seleccionar(objetivoId: Int) {
        _state.update { it.copy(seleccionadoId = objetivoId, revisado = false, errorRevisado = null, mensaje = null) }

        trabajoInforme?.cancel()
        trabajoInforme = viewModelScope.launch {
            combine(
                app.progresoRepository.intentosDelObjetivo(objetivoId),
                app.progresoRepository.respuestasDelObjetivo(objetivoId),
                app.contenidoRepository.preguntasPrueba(objetivoId)
            ) { intentos, respuestas, preguntas ->

                val preguntasInforme = preguntas.mapIndexed { i, pregunta ->
                    val delaPregunta = respuestas.filter { it.preguntaId == pregunta.id }
                    val correctas = delaPregunta.count { it.esCorrecta }
                    val porcentaje = if (delaPregunta.isEmpty()) 0 else correctas * 100 / delaPregunta.size
                    PreguntaInforme(i + 1, pregunta.enunciado, porcentaje)
                }

                val porEstudiante = intentos.groupBy { it.rutEstudiante }.map { (rut, suyos) ->
                    val mejor = suyos.maxBy { it.correctas }
                    EstudianteInforme(rut, "${mejor.correctas} de ${mejor.total}", suyos.size)
                }

                val totalCorrectas = intentos.sumOf { it.correctas }
                val totalPreguntas = intentos.sumOf { it.total }

                Triple(preguntasInforme, porEstudiante, Pair(intentos.size, if (totalPreguntas == 0) 0 else totalCorrectas * 100 / totalPreguntas))
            }.collect { (preguntasInforme, porEstudiante, totales) ->
                _state.update {
                    it.copy(
                        preguntas = preguntasInforme,
                        porEstudiante = porEstudiante,
                        estudiantes = porEstudiante.size,
                        totalIntentos = totales.first,
                        promedio = totales.second
                    )
                }
            }
        }
    }

    fun onRevisadoChanged(valor: Boolean) {
        _state.update { it.copy(revisado = valor, errorRevisado = null) }
    }

    // El informe debe ser validado por el docente antes de usarse (lo pide el caso)
    fun validar() {
        val actual = _state.value
        val objetivo = actual.objetivo ?: return
        if (actual.validando) return

        val error = if (actual.totalIntentos == 0) "No hay resultados para validar"
        else Validaciones.validarConfirmacion(actual.revisado)
        if (error != null) {
            _state.update { it.copy(errorRevisado = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(validando = true) }
            delay(600)
            app.contenidoRepository.actualizarObjetivo(
                objetivo.copy(informeValidado = true, fechaValidacion = System.currentTimeMillis())
            )
            _state.update { it.copy(validando = false, mensaje = "Informe validado") }
        }
    }
}
