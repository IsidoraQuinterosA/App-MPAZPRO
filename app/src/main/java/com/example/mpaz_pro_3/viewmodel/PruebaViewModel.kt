package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.data.local.Pregunta
import com.example.mpaz_pro_3.util.Validaciones
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PruebaUiState(
    val preguntas: List<Pregunta> = emptyList(),
    val asignatura: String = "",
    val indice: Int = 0,                  // Pregunta que se está mostrando
    val elegidas: List<Int?> = emptyList(),
    val error: String? = null,
    val intentosUsados: Int = 0,
    val maxIntentos: Int = 0,
    val enviando: Boolean = false,
    val intentoGuardado: Long? = null,    // Cuando tiene valor, se navega al resultado
    val cargado: Boolean = false
) {
    val sinIntentos: Boolean get() = intentosUsados >= maxIntentos
    val esUltima: Boolean get() = indice == preguntas.size - 1
}

class PruebaViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as MpazApp
    private val objetivoId: Int = savedStateHandle.get<Int>("objetivoId") ?: 0
    private var rut = ""

    private val _state = MutableStateFlow(PruebaUiState())
    val state: StateFlow<PruebaUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            rut = app.usuarioRepository.sesion.first()?.rut ?: ""
            val preguntas = app.contenidoRepository.preguntasPrueba(objetivoId).first()
            val objetivo = app.contenidoRepository.objetivo(objetivoId).first()
            val intentos = app.progresoRepository.intentos(rut, objetivoId).first()

            _state.update {
                it.copy(
                    preguntas = preguntas,
                    elegidas = List(preguntas.size) { null },
                    asignatura = objetivo?.asignatura ?: "",
                    maxIntentos = objetivo?.maxIntentos ?: 0,
                    intentosUsados = intentos.size,
                    cargado = true
                )
            }
        }
    }

    fun elegir(opcion: Int) {
        _state.update {
            val nuevas = it.elegidas.toMutableList()
            nuevas[it.indice] = opcion
            it.copy(elegidas = nuevas, error = null)
        }
    }

    fun siguiente() {
        val actual = _state.value
        val error = Validaciones.validarRespuesta(actual.elegidas[actual.indice])
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }
        _state.update { it.copy(indice = it.indice + 1, error = null) }
    }

    fun anterior() {
        _state.update { it.copy(indice = maxOf(0, it.indice - 1), error = null) }
    }

    fun entregar() {
        val actual = _state.value
        if (actual.enviando || actual.sinIntentos) return

        val error = Validaciones.validarPruebaCompleta(actual.elegidas)
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(enviando = true) }
            delay(600)
            val id = app.progresoRepository.guardarIntento(
                rut = rut,
                objetivoId = objetivoId,
                preguntas = actual.preguntas,
                elegidas = actual.elegidas.map { it ?: -1 }
            )
            _state.update { it.copy(enviando = false, intentoGuardado = id) }
        }
    }

    fun onNavegado() {
        _state.update { it.copy(intentoGuardado = null) }
    }
}
