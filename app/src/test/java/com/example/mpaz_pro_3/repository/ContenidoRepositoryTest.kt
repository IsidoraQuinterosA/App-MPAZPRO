package com.example.mpaz_pro_3.repository

import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.data.local.Contenido
import com.example.mpaz_pro_3.data.local.ContenidoDao
import com.example.mpaz_pro_3.data.remote.ContenidosApi
import com.example.mpaz_pro_3.data.remote.ObjetivoDto
import com.example.mpaz_pro_3.data.remote.PreguntaDto
import com.example.mpaz_pro_3.data.remote.aDto
import com.example.mpaz_pro_3.data.repository.ContenidoRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import java.io.IOException

class ContenidoRepositoryTest : FunSpec({

    lateinit var dao: ContenidoDao
    lateinit var api: ContenidosApi
    lateinit var repo: ContenidoRepository

    val objetivoDto = ObjetivoDto(
        id = 1, asignatura = "LENGUAJE", unidad = "Unidad 1", codigo = "OA 1",
        descripcion = "Leer y comprender textos", estado = "APROBADO", observacion = null,
        asignado = true, maxIntentos = 2, informeValidado = false, fechaValidacion = null
    )
    val preguntaDto = PreguntaDto(
        id = 7, objetivoId = 1, tipo = "DESAFIO", nivel = 1, enunciado = "Pregunta 7",
        opcionA = "A7", opcionB = "B7", opcionC = "C7", correcta = 0
    )

    beforeTest {
        dao = mockk(relaxed = true)
        api = mockk(relaxed = true)
        every { dao.obtenerObjetivos() } returns flowOf(listOf(Datos.objetivo()))
        repo = ContenidoRepository(dao, api)
    }

    test("las consultas se leen desde Room") {
        val pregunta = Datos.pregunta(1)
        val contenido = Contenido(id = 1, objetivoId = 1, titulo = "T", explicacion = "E", ejemplo = "Ej")
        every { dao.obtenerObjetivo(1) } returns flowOf(Datos.objetivo())
        every { dao.obtenerContenidos(1) } returns flowOf(listOf(contenido))
        every { dao.obtenerPreguntas(1, "DESAFIO") } returns flowOf(listOf(pregunta))
        every { dao.obtenerPreguntas(1, "PRUEBA") } returns flowOf(emptyList())
        every { dao.obtenerTodasLasPreguntas(1) } returns flowOf(listOf(pregunta))

        repo.objetivos.first() shouldBe listOf(Datos.objetivo())
        repo.objetivo(1).first() shouldBe Datos.objetivo()
        repo.contenidos(1).first() shouldBe listOf(contenido)
        repo.desafios(1).first() shouldBe listOf(pregunta)
        repo.preguntasPrueba(1).first() shouldBe emptyList()
        repo.todasLasPreguntas(1).first() shouldBe listOf(pregunta)
    }

    test("sincronizar descarga del microservicio (GET) y reemplaza la copia local") {
        coEvery { api.obtenerObjetivos() } returns listOf(objetivoDto)
        coEvery { api.obtenerPreguntas(null) } returns listOf(preguntaDto)

        repo.sincronizar()

        coVerifyOrder {
            dao.guardarObjetivos(match { it.single().id == 1 && it.single().codigo == "OA 1" })
            dao.eliminarTodasLasPreguntas()
            dao.reemplazarPreguntas(match { it.single().id == 7 })
        }
    }

    test("actualizar un objetivo primero lo envía al servidor (PUT) y luego a Room") {
        val objetivo = Datos.objetivo().copy(maxIntentos = 3)
        coEvery { api.actualizarObjetivo(1, any()) } returns objetivo.aDto()

        repo.actualizarObjetivo(objetivo)

        coVerify { api.actualizarObjetivo(1, objetivo.aDto()) }
        coVerify { dao.actualizarObjetivo(objetivo) }
    }

    test("si el servidor no responde, no cambia Room y avisa con una excepción") {
        coEvery { api.actualizarObjetivo(any(), any()) } throws IOException("sin conexión")

        shouldThrow<IOException> { repo.actualizarObjetivo(Datos.objetivo()) }
        coVerify(exactly = 0) { dao.actualizarObjetivo(any()) }
    }

    test("una pregunta nueva se crea con POST y queda con el id del servidor") {
        coEvery { api.crearPregunta(any()) } returns preguntaDto

        val guardada = repo.guardarPregunta(Datos.pregunta(0))

        guardada.id shouldBe 7
        coVerify { api.crearPregunta(match { it.id == null }) }
        coVerify { dao.guardarPregunta(guardada) }
    }

    test("una pregunta existente se edita con PUT") {
        coEvery { api.actualizarPregunta(7, any()) } returns preguntaDto

        repo.guardarPregunta(Datos.pregunta(7))

        coVerify { api.actualizarPregunta(7, match { it.id == 7 }) }
        coVerify(exactly = 0) { api.crearPregunta(any()) }
    }

    test("eliminar una pregunta usa DELETE y la borra de Room") {
        repo.eliminarPregunta(Datos.pregunta(7))
        coVerifyOrder {
            api.eliminarPregunta(7)
            dao.eliminarPregunta(7)
        }
    }

    test("actualizar contenido guarda en Room") {
        val contenido = Contenido(id = 1, objetivoId = 1, titulo = "T", explicacion = "E", ejemplo = "Ej", imagen = "foto.jpg")
        repo.actualizarContenido(contenido)
        coVerify { dao.actualizarContenido(contenido) }
    }

    test("los datos iniciales se cargan solo la primera vez") {
        coEvery { dao.contarObjetivos() } returns 0
        repo.cargarDatosIniciales()
        coVerify(exactly = 1) { dao.insertarObjetivos(any()) }
        coVerify(exactly = 1) { dao.insertarContenidos(any()) }
        coVerify(exactly = 1) { dao.insertarPreguntas(any()) }

        coEvery { dao.contarObjetivos() } returns 4
        repo.cargarDatosIniciales()
        coVerify(exactly = 1) { dao.insertarObjetivos(any()) }
    }
})
