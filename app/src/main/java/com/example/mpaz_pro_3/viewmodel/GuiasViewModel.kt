package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.data.local.Guia
import com.example.mpaz_pro_3.data.local.ObjetivoAprendizaje
import com.example.mpaz_pro_3.util.Archivos
import com.example.mpaz_pro_3.util.Validaciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GuiasErrores(
    val objetivo: String? = null,
    val titulo: String? = null,
    val archivo: String? = null
)

data class GuiasUiState(
    val objetivos: List<ObjetivoAprendizaje> = emptyList(),
    val guias: List<Guia> = emptyList(),
    val seleccionadoId: Int? = null,
    val titulo: String = "",
    val archivoUri: String? = null,
    val archivoNombre: String? = null,
    val archivoTamano: Long = 0,
    val archivoTipo: String = "",
    val errores: GuiasErrores = GuiasErrores(),
    val guardando: Boolean = false,
    val mensaje: String? = null
)

class GuiasViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MpazApp

    private val _state = MutableStateFlow(GuiasUiState())
    val state: StateFlow<GuiasUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(app.contenidoRepository.objetivos, app.guiaRepository.todas) { objetivos, guias ->
                Pair(objetivos, guias)
            }.collect { (objetivos, guias) ->
                _state.update { it.copy(objetivos = objetivos, guias = guias) }
            }
        }
    }

    fun seleccionar(objetivoId: Int) {
        _state.update { it.copy(seleccionadoId = objetivoId, errores = it.errores.copy(objetivo = null), mensaje = null) }
    }

    fun onTituloChanged(titulo: String) {
        _state.update { it.copy(titulo = titulo, errores = it.errores.copy(titulo = null), mensaje = null) }
    }

    // Recurso nativo Archivos: se recibe el archivo elegido con el selector de Android
    fun onArchivoElegido(uri: Uri) {
        // Mantiene el permiso de lectura aunque se gire la pantalla antes de subirlo
        try {
            app.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // Algunos proveedores no permiten guardar el permiso; igual se puede leer ahora
        }
        val (nombre, tamano) = Archivos.nombreYTamano(app, uri)
        _state.update {
            it.copy(
                archivoUri = uri.toString(),
                archivoNombre = nombre,
                archivoTamano = tamano,
                archivoTipo = Archivos.tipo(app, uri),
                errores = it.errores.copy(archivo = Validaciones.validarArchivo(nombre, tamano)),
                mensaje = null
            )
        }
    }

    fun guardar() {
        val actual = _state.value
        if (actual.guardando) return

        val errores = GuiasErrores(
            objetivo = if (actual.seleccionadoId == null) "Selecciona un objetivo" else null,
            titulo = Validaciones.validarTitulo(actual.titulo),
            archivo = Validaciones.validarArchivo(actual.archivoNombre, actual.archivoTamano)
        )
        _state.update { it.copy(errores = errores) }
        if (errores.objetivo != null || errores.titulo != null || errores.archivo != null) return

        val objetivoId = actual.seleccionadoId ?: return
        val uri = Uri.parse(actual.archivoUri ?: return)
        val nombre = actual.archivoNombre ?: return

        viewModelScope.launch {
            _state.update { it.copy(guardando = true) }
            delay(400)
            val ruta = withContext(Dispatchers.IO) { Archivos.guardarArchivo(app, uri, nombre) }
            if (ruta == null) {
                _state.update { it.copy(guardando = false, mensaje = "No se pudo copiar el archivo") }
                return@launch
            }
            app.guiaRepository.guardar(
                Guia(objetivoId = objetivoId, titulo = actual.titulo.trim(), nombreArchivo = nombre, ruta = ruta, tipo = actual.archivoTipo)
            )
            // Se limpia el formulario para subir otra guía
            _state.update {
                it.copy(guardando = false, titulo = "", archivoUri = null, archivoNombre = null,
                    archivoTamano = 0, mensaje = "Guía subida correctamente")
            }
        }
    }

    fun eliminar(guia: Guia) {
        viewModelScope.launch { app.guiaRepository.eliminar(guia) }
    }
}
