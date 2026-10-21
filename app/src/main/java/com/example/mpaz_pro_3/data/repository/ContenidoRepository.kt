package com.example.mpaz_pro_3.data.repository

import com.example.mpaz_pro_3.data.local.Contenido
import com.example.mpaz_pro_3.data.local.ContenidoDao
import com.example.mpaz_pro_3.data.local.DatosIniciales
import com.example.mpaz_pro_3.data.local.ObjetivoAprendizaje
import com.example.mpaz_pro_3.data.local.Pregunta
import kotlinx.coroutines.flow.Flow

// Acceso a objetivos, contenidos y preguntas (lo usan estudiante, docente y UTP)
class ContenidoRepository(private val contenidoDao: ContenidoDao) {

    val objetivos: Flow<List<ObjetivoAprendizaje>> = contenidoDao.obtenerObjetivos()

    fun objetivo(id: Int): Flow<ObjetivoAprendizaje?> = contenidoDao.obtenerObjetivo(id)

    fun contenidos(objetivoId: Int): Flow<List<Contenido>> = contenidoDao.obtenerContenidos(objetivoId)

    fun desafios(objetivoId: Int): Flow<List<Pregunta>> = contenidoDao.obtenerPreguntas(objetivoId, "DESAFIO")

    fun preguntasPrueba(objetivoId: Int): Flow<List<Pregunta>> = contenidoDao.obtenerPreguntas(objetivoId, "PRUEBA")

    suspend fun actualizarObjetivo(objetivo: ObjetivoAprendizaje) {
        contenidoDao.actualizarObjetivo(objetivo)
    }

    suspend fun actualizarContenido(contenido: Contenido) {
        contenidoDao.actualizarContenido(contenido)
    }

    suspend fun cargarDatosIniciales() {
        if (contenidoDao.contarObjetivos() == 0) {
            contenidoDao.insertarObjetivos(DatosIniciales.objetivos)
            contenidoDao.insertarContenidos(DatosIniciales.contenidos)
            contenidoDao.insertarPreguntas(DatosIniciales.preguntas)
        }
    }
}
