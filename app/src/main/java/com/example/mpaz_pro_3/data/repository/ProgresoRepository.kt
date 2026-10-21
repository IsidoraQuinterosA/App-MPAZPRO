package com.example.mpaz_pro_3.data.repository

import com.example.mpaz_pro_3.data.local.DatosIniciales
import com.example.mpaz_pro_3.data.local.Intento
import com.example.mpaz_pro_3.data.local.Pregunta
import com.example.mpaz_pro_3.data.local.Progreso
import com.example.mpaz_pro_3.data.local.ProgresoDao
import com.example.mpaz_pro_3.data.local.Respuesta
import kotlinx.coroutines.flow.Flow

// Acceso al avance de los estudiantes: progreso, intentos y respuestas
class ProgresoRepository(private val progresoDao: ProgresoDao) {

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

    // Carga resultados ficticios para que el informe del docente tenga datos de prueba
    suspend fun cargarDatosIniciales() {
        progresoDao.insertarIntentos(DatosIniciales.intentos)
        progresoDao.insertarRespuestasIniciales(DatosIniciales.respuestas)
    }

    fun respuestas(intentoId: Long): Flow<List<Respuesta>> = progresoDao.obtenerRespuestas(intentoId)

    // Guarda un intento de la prueba final con todas sus respuestas.
    // "elegidas" tiene la alternativa que marcó el estudiante en cada pregunta.
    suspend fun guardarIntento(rut: String, objetivoId: Int, preguntas: List<Pregunta>, elegidas: List<Int>): Long {
        val correctas = preguntas.indices.count { i -> elegidas[i] == preguntas[i].correcta }
        val intentoId = progresoDao.insertarIntento(
            Intento(rutEstudiante = rut, objetivoId = objetivoId, correctas = correctas, total = preguntas.size)
        )
        val respuestas = preguntas.mapIndexed { i, pregunta ->
            Respuesta(
                intentoId = intentoId,
                preguntaId = pregunta.id,
                elegida = elegidas[i],
                esCorrecta = elegidas[i] == pregunta.correcta
            )
        }
        progresoDao.insertarRespuestas(respuestas)
        return intentoId
    }
}
