package com.example.mpaz_pro_3.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// Consultas del avance de los estudiantes: progreso, intentos y respuestas
@Dao
interface ProgresoDao {

    @Query("SELECT * FROM progreso WHERE rutEstudiante = :rut")
    fun obtenerProgresoEstudiante(rut: String): Flow<List<Progreso>>

    @Query("SELECT * FROM progreso WHERE rutEstudiante = :rut AND objetivoId = :objetivoId")
    fun obtenerProgreso(rut: String, objetivoId: Int): Flow<Progreso?>

    // Upsert: inserta si no existe o actualiza si ya existe
    @Upsert
    suspend fun guardarProgreso(progreso: Progreso)

    @Insert
    suspend fun insertarIntento(intento: Intento): Long

    @Insert
    suspend fun insertarRespuestas(respuestas: List<Respuesta>)

    @Query("SELECT * FROM intentos WHERE rutEstudiante = :rut AND objetivoId = :objetivoId ORDER BY fecha DESC")
    fun obtenerIntentos(rut: String, objetivoId: Int): Flow<List<Intento>>

    @Query("SELECT * FROM intentos WHERE objetivoId = :objetivoId ORDER BY fecha DESC")
    fun obtenerIntentosDelObjetivo(objetivoId: Int): Flow<List<Intento>>

    @Query("SELECT * FROM intentos WHERE id = :id")
    fun obtenerIntento(id: Long): Flow<Intento?>

    @Query("SELECT * FROM respuestas WHERE intentoId = :intentoId")
    fun obtenerRespuestas(intentoId: Long): Flow<List<Respuesta>>

    // Todas las respuestas de un objetivo (une las tablas respuestas e intentos), para el informe
    @Query(
        "SELECT respuestas.* FROM respuestas " +
            "INNER JOIN intentos ON respuestas.intentoId = intentos.id " +
            "WHERE intentos.objetivoId = :objetivoId"
    )
    fun obtenerRespuestasDelObjetivo(objetivoId: Int): Flow<List<Respuesta>>

    // Para los resultados ficticios iniciales. IGNORE evita duplicarlos.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarIntentos(intentos: List<Intento>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarRespuestasIniciales(respuestas: List<Respuesta>)
}
