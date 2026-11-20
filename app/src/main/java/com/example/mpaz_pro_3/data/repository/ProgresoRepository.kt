package com.example.mpaz_pro_3.data.repository

import com.example.mpaz_pro_3.data.local.Intento
import com.example.mpaz_pro_3.data.local.Pregunta
import com.example.mpaz_pro_3.data.local.Progreso
import com.example.mpaz_pro_3.data.local.ProgresoDao
import com.example.mpaz_pro_3.data.local.Respuesta
import com.example.mpaz_pro_3.data.remote.IntentoDto
import com.example.mpaz_pro_3.data.remote.RespuestaDto
import com.example.mpaz_pro_3.data.remote.ResultadosApi
import com.example.mpaz_pro_3.data.remote.aEntidad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

// Acceso al avance de los estudiantes.
// - Progreso (Aprende y desafíos): solo en Room, en el teléfono.
// - Intentos de la prueba final: se envían al microservicio ms-resultados y se guarda una copia en Room.
class ProgresoRepository(
    private val progresoDao: ProgresoDao,
    private val api: ResultadosApi
) {

    fun progresoEstudiante(rut: String): Flow<List<Progreso>> = progresoDao.obtenerProgresoEstudiante(rut)

    fun progreso(rut: String, objetivoId: Int): Flow<Progreso?> = progresoDao.obtenerProgreso(rut, objetivoId)

    suspend fun guardarProgreso(progreso: Progreso) {
        progresoDao.guardarProgreso(progreso)
    }

    fun intentos(rut: String, objetivoId: Int): Flow<List<Intento>> = progresoDao.obtenerIntentos(rut, objetivoId)

    fun intentosDelObjetivo(objetivoId: Int): Flow<List<Intento>> = progresoDao.obtenerIntentosDelObjetivo(objetivoId)

    fun intento(id: Long): Flow<Intento?> = progresoDao.obtenerIntento(id)

    fun respuestasDelObjetivo(objetivoId: Int): Flow<List<Respuesta>> =
        progresoDao.obtenerRespuestasDelObjetivo(objetivoId)

    fun respuestas(intentoId: Long): Flow<List<Respuesta>> = progresoDao.obtenerRespuestas(intentoId)

    // Guarda un intento de la prueba final con todas sus respuestas.
    // "elegidas" tiene la alternativa que marcó el estudiante en cada pregunta.
    // Primero se envía al servidor (POST); si no responde, se guarda solo en el teléfono
    // para que el estudiante no pierda su prueba.
    suspend fun guardarIntento(rut: String, objetivoId: Int, preguntas: List<Pregunta>, elegidas: List<Int>): Long {
        val respuestas = preguntas.mapIndexed { i, pregunta ->
            RespuestaDto(
                id = null,
                preguntaId = pregunta.id,
                elegida = elegidas[i],
                esCorrecta = elegidas[i] == pregunta.correcta
            )
        }
        val correctas = respuestas.count { it.esCorrecta }
        val nuevo = IntentoDto(
            id = null,
            rutEstudiante = rut,
            objetivoId = objetivoId,
            correctas = correctas,
            total = preguntas.size,
            fecha = System.currentTimeMillis(),
            respuestas = respuestas
        )

        val guardado = try {
            api.registrarIntento(nuevo)
        } catch (e: Exception) {
            null
        }

        return if (guardado != null) {
            val id = guardado.id ?: 0L
            progresoDao.guardarIntentos(listOf(guardado.aEntidad()))
            progresoDao.insertarRespuestas(guardado.respuestas.map { it.aEntidad(id).copy(id = 0) })
            id
        } else {
            val id = progresoDao.insertarIntento(nuevo.aEntidad())
            progresoDao.insertarRespuestas(respuestas.map { it.aEntidad(id).copy(id = 0) })
            id
        }
    }

    // Trae del servidor todos los intentos de un objetivo (GET) y reemplaza la copia local.
    // Lo usa el informe del docente para ver los resultados de todo el curso.
    suspend fun sincronizarIntentos(objetivoId: Int) {
        val intentos = api.obtenerIntentos(objetivoId = objetivoId)
        progresoDao.eliminarRespuestasDelObjetivo(objetivoId)
        progresoDao.eliminarIntentosDelObjetivo(objetivoId)
        progresoDao.guardarIntentos(intentos.map { it.aEntidad() })
        val respuestas = intentos.flatMap { intento ->
            intento.respuestas.map { it.aEntidad(intento.id ?: 0L).copy(id = 0) }
        }
        progresoDao.insertarRespuestas(respuestas)
    }

    // El docente reinicia los intentos de un estudiante: se borran en el servidor (DELETE) y en Room
    suspend fun reiniciarIntentos(rut: String, objetivoId: Int) {
        val intentos = progresoDao.obtenerIntentos(rut, objetivoId).first()
        intentos.forEach { intento ->
            api.eliminarIntento(intento.id)
            progresoDao.eliminarRespuestasDelIntento(intento.id)
            progresoDao.eliminarIntento(intento.id)
        }
    }
}
