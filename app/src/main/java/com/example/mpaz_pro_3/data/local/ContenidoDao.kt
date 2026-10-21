package com.example.mpaz_pro_3.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Consultas de objetivos, contenidos y preguntas
@Dao
interface ContenidoDao {

    @Query("SELECT * FROM objetivos ORDER BY id")
    fun obtenerObjetivos(): Flow<List<ObjetivoAprendizaje>>

    @Query("SELECT * FROM objetivos WHERE id = :id")
    fun obtenerObjetivo(id: Int): Flow<ObjetivoAprendizaje?>

    @Query("SELECT * FROM contenidos WHERE objetivoId = :objetivoId")
    fun obtenerContenidos(objetivoId: Int): Flow<List<Contenido>>

    @Query("SELECT * FROM preguntas WHERE objetivoId = :objetivoId AND tipo = :tipo ORDER BY nivel, id")
    fun obtenerPreguntas(objetivoId: Int, tipo: String): Flow<List<Pregunta>>

    @Query("SELECT COUNT(*) FROM objetivos")
    suspend fun contarObjetivos(): Int

    @Insert
    suspend fun insertarObjetivos(objetivos: List<ObjetivoAprendizaje>)

    @Insert
    suspend fun insertarContenidos(contenidos: List<Contenido>)

    @Insert
    suspend fun insertarPreguntas(preguntas: List<Pregunta>)

    @Update
    suspend fun actualizarObjetivo(objetivo: ObjetivoAprendizaje)
}
