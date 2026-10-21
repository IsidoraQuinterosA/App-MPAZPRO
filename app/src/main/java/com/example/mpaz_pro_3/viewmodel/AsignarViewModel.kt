package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.data.local.ObjetivoAprendizaje
import com.example.mpaz_pro_3.util.Archivos
import com.example.mpaz_pro_3.util.Validaciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class AsignarErrores(
    val objetivo: String? = null,
    val intentos: String? = null
)

data class AsignarUiState(
    val objetivos: List<ObjetivoAprendizaje> = emptyList(),
    val seleccionadoId: Int? = null,
    val intentos: String = "",
    val asignado: Boolean = true,
    val errores: AsignarErrores = AsignarErrores(),
    val guardando: Boolean = false,
    val imagen: String = "",               // Imagen de apoyo del contenido elegido
    val guardandoImagen: Boolean = false,
    val mensaje: String? = null
) {
    val seleccionado: ObjetivoAprendizaje? get() = objetivos.find { it.id == seleccionadoId }
}

class AsignarViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MpazApp

    private val _state = MutableStateFlow(AsignarUiState())
    val state: StateFlow<AsignarUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            app.contenidoRepository.objetivos.collect { lista ->
                _state.update { it.copy(objetivos = lista) }
            }
        }
    }

    fun seleccionar(objetivo: ObjetivoAprendizaje) {
        _state.update {
            it.copy(
                seleccionadoId = objetivo.id,
                intentos = objetivo.maxIntentos.toString(),
                asignado = objetivo.asignado || objetivo.estado == "APROBADO",
                errores = AsignarErrores(),
                mensaje = null
            )
        }
        // Carga la imagen de apoyo que ya tenga el contenido de este objetivo
        viewModelScope.launch {
            val contenido = app.contenidoRepository.contenidos(objetivo.id).first().firstOrNull()
            _state.update { it.copy(imagen = contenido?.imagen ?: "") }
        }
    }

    // Recurso nativo Galería: guarda la imagen elegida como apoyo visual de "Aprende"
    fun guardarImagen(uri: Uri) {
        val objetivoId = _state.value.seleccionadoId
        if (objetivoId == null) {
            _state.update { it.copy(errores = it.errores.copy(objetivo = "Selecciona un objetivo antes de elegir la imagen")) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(guardandoImagen = true, mensaje = null) }
            // Copiar el archivo se hace fuera del hilo principal
            val ruta = withContext(Dispatchers.IO) { Archivos.guardarImagen(app, uri) }
            val contenido = app.contenidoRepository.contenidos(objetivoId).first().firstOrNull()

            if (ruta == null || contenido == null) {
                _state.update { it.copy(guardandoImagen = false, mensaje = "No se pudo guardar la imagen") }
                return@launch
            }
            if (contenido.imagen.isNotEmpty()) File(contenido.imagen).delete()
            app.contenidoRepository.actualizarContenido(contenido.copy(imagen = ruta))
            _state.update { it.copy(guardandoImagen = false, imagen = ruta, mensaje = "Imagen de apoyo guardada") }
        }
    }

    fun onIntentosChanged(texto: String) {
        _state.update {
            it.copy(intentos = texto.filter { c -> c.isDigit() }.take(2),
                errores = it.errores.copy(intentos = null), mensaje = null)
        }
    }

    fun onAsignadoChanged(valor: Boolean) {
        _state.update { it.copy(asignado = valor, mensaje = null) }
    }

    fun guardar() {
        val actual = _state.value
        if (actual.guardando) return

        val errores = AsignarErrores(
            objetivo = Validaciones.validarObjetivo(actual.seleccionado?.estado),
            intentos = Validaciones.validarIntentos(actual.intentos)
        )
        _state.update { it.copy(errores = errores) }
        val objetivo = actual.seleccionado
        if (errores.objetivo != null || errores.intentos != null || objetivo == null) return

        viewModelScope.launch {
            _state.update { it.copy(guardando = true) }
            delay(600)
            val intentos = actual.intentos.toInt()
            app.contenidoRepository.actualizarObjetivo(
                objetivo.copy(asignado = actual.asignado, maxIntentos = intentos)
            )
            val mensaje = if (actual.asignado) "Objetivo asignado al curso con $intentos intento(s)"
            else "Se quitó la asignación del objetivo"
            _state.update { it.copy(guardando = false, mensaje = mensaje) }
        }
    }
}
