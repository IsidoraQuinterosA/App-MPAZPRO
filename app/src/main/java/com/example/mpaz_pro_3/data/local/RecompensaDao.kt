package com.example.mpaz_pro_3.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecompensaDao {

    @Query("SELECT * FROM recompensas WHERE rutEstudiante = :rut ORDER BY fecha DESC")
    fun obtenerColeccion(rut: String): Flow<List<Recompensa>>

    @Query("SELECT * FROM recompensas WHERE rutEstudiante = :rut AND objetivoId = :objetivoId LIMIT 1")
    suspend fun obtenerRecompensa(rut: String, objetivoId: Int): Recompensa?

    @Upsert
    suspend fun guardar(recompensa: Recompensa)
}
