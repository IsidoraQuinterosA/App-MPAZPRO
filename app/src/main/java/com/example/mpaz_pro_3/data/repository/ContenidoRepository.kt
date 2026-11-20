package com.example.mpaz_pro_3.data.repository

import com.example.mpaz_pro_3.data.local.Contenido
import com.example.mpaz_pro_3.data.local.ContenidoDao
import com.example.mpaz_pro_3.data.local.DatosIniciales
import com.example.mpaz_pro_3.data.local.ObjetivoAprendizaje
import com.example.mpaz_pro_3.data.local.Pregunta
import com.example.mpaz_pro_3.data.remote.ContenidosApi
import com.example.mpaz_pro_3.data.remote.aDto
import com.example.mpaz_pro_3.data.remote.aEntidad
import kotlinx.coroutines.flow.Flow

// Acceso a objetivos, contenidos y preguntas (lo usan estudiante, docente y UTP).
// El microservicio ms-contenidos es la fuente oficial; Room guarda una copia local
// para que las pantallas se actualicen solas y la app funcione sin conexión.
class ContenidoRepository(
    private val contenidoDao: ContenidoDao,
    private val api: ContenidosApi
) {

    val objetivos: Flow<List<ObjetivoAprendizaje>> = contenidoDao.obtenerObjetivos()

    fun objetivo(id: Int): Flow<ObjetivoAprendizaje?> = contenidoDao.obtenerObjetivo(id)

    fun contenidos(objetivoId: Int): Flow<List<Contenido>> = contenidoDao.obtenerContenidos(objetivoId)

    fun desafios(objetivoId: Int): Flow<List<Pregunta>> = contenidoDao.obtenerPreguntas(objetivoId, "DESAFIO")

    fun preguntasPrueba(objetivoId: Int): Flow<List<Pregunta>> = contenidoDao.obtenerPreguntas(objetivoId, "PRUEBA")

    fun todasLasPreguntas(objetivoId: Int): Flow<List<Pregunta>> = contenidoDao.obtenerTodasLasPreguntas(objetivoId)

    // Descarga objetivos y preguntas del servidor y los guarda en Room (GET)
    suspend fun sincronizar() {
        val objetivos = api.obtenerObjetivos().map { it.aEntidad() }
        val preguntas = api.obtenerPreguntas().map { it.aEntidad() }
        contenidoDao.guardarObjetivos(objetivos)
        contenidoDao.eliminarTodasLasPreguntas()
        contenidoDao.reemplazarPreguntas(preguntas)
    }

    // Primero se actualiza en el servidor (PUT) y luego la copia local.
    // Si el servidor no responde, lanza la excepción para que el ViewModel muestre el error.
    suspend fun actualizarObjetivo(objetivo: ObjetivoAprendizaje) {
        val actualizado = api.actualizarObjetivo(objetivo.id, objetivo.aDto())
        contenidoDao.actualizarObjetivo(actualizado.aEntidad())
    }

    // Crea (POST) o edita (PUT) una pregunta según si ya tiene id
    suspend fun guardarPregunta(pregunta: Pregunta): Pregunta {
        val respuesta = if (pregunta.id == 0) {
            api.crearPregunta(pregunta.aDto())
        } else {
            api.actualizarPregunta(pregunta.id, pregunta.aDto())
        }
        val guardada = respuesta.aEntidad()
        contenidoDao.guardarPregunta(guardada)
        return guardada
    }

    // Elimina en el servidor (DELETE) y luego en Room
    suspend fun eliminarPregunta(pregunta: Pregunta) {
        api.eliminarPregunta(pregunta.id)
        contenidoDao.eliminarPregunta(pregunta.id)
    }

    suspend fun actualizarContenido(contenido: Contenido) {
        contenidoDao.actualizarContenido(contenido)
    }

    // Datos locales para el primer uso sin conexión (los mismos que tiene el servidor)
    suspend fun cargarDatosIniciales() {
        if (contenidoDao.contarObjetivos() == 0) {
            contenidoDao.insertarObjetivos(DatosIniciales.objetivos)
            contenidoDao.insertarContenidos(DatosIniciales.contenidos)
            contenidoDao.insertarPreguntas(DatosIniciales.preguntas)
        }
    }
}
