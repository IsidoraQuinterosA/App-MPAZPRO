package com.example.mpaz_pro_3.repository

import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.data.local.Intento
import com.example.mpaz_pro_3.data.local.Progreso
import com.example.mpaz_pro_3.data.local.ProgresoDao
import com.example.mpaz_pro_3.data.local.Respuesta
import com.example.mpaz_pro_3.data.remote.IntentoDto
import com.example.mpaz_pro_3.data.remote.RespuestaDto
import com.example.mpaz_pro_3.data.remote.ResultadosApi
import com.example.mpaz_pro_3.data.repository.ProgresoRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import java.io.IOException

class ProgresoRepositoryTest : FunSpec({

    lateinit var dao: ProgresoDao
    lateinit var api: ResultadosApi
    lateinit var repo: ProgresoRepository

    val rut = Datos.RUT
    val preguntas = listOf(
        Datos.pregunta(10, tipo = "PRUEBA", nivel = 0, correcta = 0),
        Datos.pregunta(11, tipo = "PRUEBA", nivel = 0, correcta = 2)
    )

    beforeTest {
        dao = mockk(relaxed = true)
        api = mockk(relaxed = true)
        repo = ProgresoRepository(dao, api)
    }

    test("las consultas de avance se leen desde Room") {
        val progreso = Progreso(rut, 1, aprendeVisto = true, nivelSuperado = 2)
        val intento = Intento(id = 1, rutEstudiante = rut, objetivoId = 1, correctas = 1, total = 2, fecha = 1L)
        val respuesta = Respuesta(id = 1, intentoId = 1, preguntaId = 10, elegida = 0, esCorrecta = true)
        every { dao.obtenerProgresoEstudiante(rut) } returns flowOf(listOf(progreso))
        every { dao.obtenerProgreso(rut, 1) } returns flowOf(progreso)
        every { dao.obtenerIntentos(rut, 1) } returns flowOf(listOf(intento))
        every { dao.obtenerIntentosDelObjetivo(1) } returns flowOf(listOf(intento))
        every { dao.obtenerIntento(1) } returns flowOf(intento)
        every { dao.obtenerRespuestasDelObjetivo(1) } returns flowOf(listOf(respuesta))
        every { dao.obtenerRespuestas(1) } returns flowOf(listOf(respuesta))

        repo.progresoEstudiante(rut).first() shouldBe listOf(progreso)
        repo.progreso(rut, 1).first() shouldBe progreso
        repo.intentos(rut, 1).first() shouldBe listOf(intento)
        repo.intentosDelObjetivo(1).first() shouldBe listOf(intento)
        repo.intento(1).first() shouldBe intento
        repo.respuestasDelObjetivo(1).first() shouldBe listOf(respuesta)
        repo.respuestas(1).first() shouldBe listOf(respuesta)
    }

    test("guardar progreso lo deja en Room") {
        val progreso = Progreso(rut, 1, aprendeVisto = true)
        repo.guardarProgreso(progreso)
        coVerify { dao.guardarProgreso(progreso) }
    }

    test("el intento se envía al microservicio (POST) con las correctas calculadas") {
        val enviado = slot<IntentoDto>()
        coEvery { api.registrarIntento(capture(enviado)) } answers {
            enviado.captured.copy(id = 50, respuestas = enviado.captured.respuestas.mapIndexed { i, r -> r.copy(id = i + 1L) })
        }

        val id = repo.guardarIntento(rut, 1, preguntas, listOf(0, 1))

        id shouldBe 50
        enviado.captured.correctas shouldBe 1
        enviado.captured.total shouldBe 2
        enviado.captured.respuestas.map { it.esCorrecta } shouldBe listOf(true, false)
        coVerify { dao.guardarIntentos(match { it.single().id == 50L }) }
        coVerify { dao.insertarRespuestas(match { lista -> lista.size == 2 && lista.all { it.intentoId == 50L && it.id == 0L } }) }
        coVerify(exactly = 0) { dao.insertarIntento(any()) }
    }

    test("si el servidor no responde, el intento se guarda solo en el teléfono") {
        coEvery { api.registrarIntento(any()) } throws IOException("sin conexión")
        coEvery { dao.insertarIntento(any()) } returns 8L

        val id = repo.guardarIntento(rut, 1, preguntas, listOf(0, 2))

        id shouldBe 8
        coVerify { dao.insertarIntento(match { it.correctas == 2 && it.total == 2 }) }
        coVerify { dao.insertarRespuestas(match { lista -> lista.all { it.intentoId == 8L } }) }
    }

    test("sincronizar intentos (GET) reemplaza los del objetivo en Room") {
        val remoto = IntentoDto(
            id = 3, rutEstudiante = rut, objetivoId = 1, correctas = 1, total = 1, fecha = 100L,
            respuestas = listOf(RespuestaDto(id = 9, preguntaId = 10, elegida = 0, esCorrecta = true))
        )
        coEvery { api.obtenerIntentos(objetivoId = 1, rut = null) } returns listOf(remoto)

        repo.sincronizarIntentos(1)

        coVerify { dao.eliminarRespuestasDelObjetivo(1) }
        coVerify { dao.eliminarIntentosDelObjetivo(1) }
        coVerify { dao.guardarIntentos(match { it.single().id == 3L }) }
        coVerify { dao.insertarRespuestas(match { it.single().intentoId == 3L }) }
    }

    test("reiniciar intentos los borra en el servidor (DELETE) y en Room") {
        val intentos = listOf(
            Intento(id = 1, rutEstudiante = rut, objetivoId = 1, correctas = 0, total = 2, fecha = 1L),
            Intento(id = 2, rutEstudiante = rut, objetivoId = 1, correctas = 1, total = 2, fecha = 2L)
        )
        every { dao.obtenerIntentos(rut, 1) } returns flowOf(intentos)

        repo.reiniciarIntentos(rut, 1)

        coVerify { api.eliminarIntento(1) }
        coVerify { api.eliminarIntento(2) }
        coVerify { dao.eliminarRespuestasDelIntento(1) }
        coVerify { dao.eliminarIntento(2) }
    }
})
