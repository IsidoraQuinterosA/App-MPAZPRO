package com.example.mpaz_pro_3.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE rut = :rut AND clave = :clave AND rol = :rol LIMIT 1")
    suspend fun buscar(rut: String, clave: String, rol: String): Usuario?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(usuarios: List<Usuario>)
}
