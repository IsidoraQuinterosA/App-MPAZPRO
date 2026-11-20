package com.example.mpaz_pro_3.repository

import com.example.mpaz_pro_3.data.local.Guia
import com.example.mpaz_pro_3.data.local.GuiaDao
import com.example.mpaz_pro_3.data.repository.GuiaRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import java.io.File

class GuiaRepositoryTest : FunSpec({

    val guia = Guia(id = 1, objetivoId = 1, titulo = "Guía de lectura", nombreArchivo = "guia.pdf", ruta = "", tipo = "application/pdf")

    test("entrega todas las guías y las de un objetivo") {
        val dao = mockk<GuiaDao>(relaxed = true)
        every { dao.obtenerTodas() } returns flowOf(listOf(guia))
        every { dao.obtenerPorObjetivo(1) } returns flowOf(listOf(guia))
        val repo = GuiaRepository(dao)

        repo.todas.first() shouldBe listOf(guia)
        repo.guias(1).first() shouldBe listOf(guia)
    }

    test("guardar inserta la guía en Room") {
        val dao = mockk<GuiaDao>(relaxed = true)
        every { dao.obtenerTodas() } returns flowOf(emptyList())
        GuiaRepository(dao).guardar(guia)
        coVerify { dao.insertar(guia) }
    }

    test("eliminar borra el archivo y el registro") {
        val archivo = File.createTempFile("guia", ".pdf")
        val dao = mockk<GuiaDao>(relaxed = true)
        every { dao.obtenerTodas() } returns flowOf(emptyList())
        val conRuta = guia.copy(ruta = archivo.absolutePath)

        GuiaRepository(dao).eliminar(conRuta)

        archivo.exists() shouldBe false
        coVerify { dao.eliminar(conRuta) }
    }
})
