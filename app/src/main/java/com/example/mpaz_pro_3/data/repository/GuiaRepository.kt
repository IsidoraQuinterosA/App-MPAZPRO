package com.example.mpaz_pro_3.data.repository

import com.example.mpaz_pro_3.data.local.Guia
import com.example.mpaz_pro_3.data.local.GuiaDao
import kotlinx.coroutines.flow.Flow
import java.io.File

class GuiaRepository(private val guiaDao: GuiaDao) {

    val todas: Flow<List<Guia>> = guiaDao.obtenerTodas()

    fun guias(objetivoId: Int): Flow<List<Guia>> = guiaDao.obtenerPorObjetivo(objetivoId)

    suspend fun guardar(guia: Guia) {
        guiaDao.insertar(guia)
    }

    // Borra el registro y también el archivo guardado en el teléfono
    suspend fun eliminar(guia: Guia) {
        File(guia.ruta).delete()
        guiaDao.eliminar(guia)
    }
}
