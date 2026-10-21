package com.example.mpaz_pro_3.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GuiaDao {

    @Query("SELECT * FROM guias ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<Guia>>

    @Query("SELECT * FROM guias WHERE objetivoId = :objetivoId ORDER BY id DESC")
    fun obtenerPorObjetivo(objetivoId: Int): Flow<List<Guia>>

    @Insert
    suspend fun insertar(guia: Guia)

    @Delete
    suspend fun eliminar(guia: Guia)
}
