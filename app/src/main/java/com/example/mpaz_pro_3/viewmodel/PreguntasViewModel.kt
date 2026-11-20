package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.data.local.ObjetivoAprendizaje
import com.example.mpaz_pro_3.data.local.Pregunta
import com.example.mpaz_pro_3.data.remote.ApiConfig
import com.example.mpaz_pro_3.util.Validaciones
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Error de cada campo del formulario. null = el campo está correcto
data class PreguntaErrores(
    val enunciado: String? = null,
    val opcionA: String? = null,
    val opcionB: String? = null,
    val opcionC: String? = null,
    val correcta: String? = null,
    val nivel: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(enunciado, opcionA, opcionB, opcionC, correcta, nivel).any { it != null }
}

data class PreguntasUiState(
    val objetivos: List<ObjetivoAprendizaje> = emptyList(),
    val seleccionadoId: Int? = null,
    val preguntas: List<Pregunta> = emptyList(),
    // Formulario
    val formularioAbierto: Boolean = false,
    val editandoId: Int = 0,              // 0 = pregunta nueva
    val tipo: String = "DESAFIO",
    val nivel: Int = 1,
    val enunciado: String = "",
    val opcionA: String = "",
    val opcionB: String = "",
    val opcionC: String = "",
    val correcta: Int? = null,
    val errores: PreguntaErrores = PreguntaErrores(),
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val errorServidor: String? = null
)

// CRUD de preguntas: el docente crea, edita y elimina preguntas en el microservicio ms-contenidos
class PreguntasViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MpazApp
    private var trabajoPreguntas: Job? = null

    private val _state = MutableStateFlow(PreguntasUiState())
    val state: StateFlow<PreguntasUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            app.contenidoRepository.objetivos.collect { lista ->
                _state.update { it.copy(objetivos = lista) }
                if (_state.value.seleccionadoId == null && lista.isNotEmpty()) {
                    seleccionarObjetivo(lista.first().id)
                }
            }
        }
    }

    // Read: muestra las preguntas guardadas del objetivo elegido
    fun seleccionarObjetivo(objetivoId: Int) {
        _state.update { it.copy(seleccionadoId = objetivoId, formularioAbierto = false, mensaje = null, errorServidor = null) }
        trabajoPreguntas?.cancel()
        trabajoPreguntas = viewModelScope.launch {
            app.contenidoRepository.todasLasPreguntas(objetivoId).collect { lista ->
                _state.update { it.copy(preguntas = lista) }
            }
        }
    }

    fun nuevaPregunta() {
        _state.update {
            it.copy(
                formularioAbierto = true, editandoId = 0, tipo = "DESAFIO", nivel = 1,
                enunciado = "", opcionA = "", opcionB = "", opcionC = "", correcta = null,
                errores = PreguntaErrores(), mensaje = null, errorServidor = null
            )
        }
    }

    fun editarPregunta(pregunta: Pregunta) {
        _state.update {
            it.copy(
                formularioAbierto = true, editandoId = pregunta.id, tipo = pregunta.tipo,
                nivel = if (pregunta.tipo == "DESAFIO") pregunta.nivel else 1,
                enunciado = pregunta.enunciado, opcionA = pregunta.opcionA, opcionB = pregunta.opcionB,
                opcionC = pregunta.opcionC, correcta = pregunta.correcta,
                errores = PreguntaErrores(), mensaje = null, errorServidor = null
            )
        }
    }

    fun cerrarFormulario() {
        _state.update { it.copy(formularioAbierto = false, errores = PreguntaErrores()) }
    }

    fun onTipoChanged(tipo: String) {
        _state.update { it.copy(tipo = tipo, errores = it.errores.copy(nivel = null)) }
    }

    fun onNivelChanged(nivel: Int) {
        _state.update { it.copy(nivel = nivel, errores = it.errores.copy(nivel = null)) }
    }

    fun onEnunciadoChanged(texto: String) {
        _state.update { it.copy(enunciado = texto, errores = it.errores.copy(enunciado = null)) }
    }

    // indice: 0 = A, 1 = B, 2 = C
    fun onOpcionChanged(indice: Int, texto: String) {
        _state.update {
            when (indice) {
                0 -> it.copy(opcionA = texto, errores = it.errores.copy(opcionA = null))
                1 -> it.copy(opcionB = texto, errores = it.errores.copy(opcionB = null))
                else -> it.copy(opcionC = texto, errores = it.errores.copy(opcionC = null))
            }
        }
    }

    fun onCorrectaChanged(indice: Int) {
        _state.update { it.copy(correcta = indice, errores = it.errores.copy(correcta = null)) }
    }

    private fun validar(): Boolean {
        val s = _state.value
        val repetidas = Validaciones.validarOpcionesDistintas(s.opcionA, s.opcionB, s.opcionC)
        val errores = PreguntaErrores(
            enunciado = Validaciones.validarEnunciado(s.enunciado),
            opcionA = Validaciones.validarOpcion(s.opcionA),
            opcionB = Validaciones.validarOpcion(s.opcionB),
            opcionC = Validaciones.validarOpcion(s.opcionC) ?: repetidas,
            correcta = Validaciones.validarCorrecta(s.correcta),
            nivel = Validaciones.validarNivel(s.tipo, s.nivel)
        )
        _state.update { it.copy(errores = errores) }
        return !errores.hayErrores
    }

    // Create (POST) o Update (PUT) según si se está editando
    fun guardar() {
        val s = _state.value
        val objetivoId = s.seleccionadoId ?: return
        if (s.guardando || !validar()) return

        val pregunta = Pregunta(
            id = s.editandoId,
            objetivoId = objetivoId,
            tipo = s.tipo,
            nivel = if (s.tipo == "PRUEBA") 0 else s.nivel,
            enunciado = s.enunciado.trim(),
            opcionA = s.opcionA.trim(),
            opcionB = s.opcionB.trim(),
            opcionC = s.opcionC.trim(),
            correcta = s.correcta ?: 0
        )

        viewModelScope.launch {
            _state.update { it.copy(guardando = true, errorServidor = null) }
            try {
                app.contenidoRepository.guardarPregunta(pregunta)
                val mensaje = if (s.editandoId == 0) "Pregunta creada" else "Pregunta actualizada"
                _state.update { it.copy(guardando = false, formularioAbierto = false, mensaje = mensaje) }
            } catch (e: Exception) {
                _state.update { it.copy(guardando = false, errorServidor = ApiConfig.MENSAJE_SIN_SERVIDOR) }
            }
        }
    }

    // Delete (DELETE)
    fun eliminar(pregunta: Pregunta) {
        viewModelScope.launch {
            try {
                app.contenidoRepository.eliminarPregunta(pregunta)
                _state.update { it.copy(mensaje = "Pregunta eliminada") }
            } catch (e: Exception) {
                _state.update { it.copy(errorServidor = ApiConfig.MENSAJE_SIN_SERVIDOR) }
            }
        }
    }
}
