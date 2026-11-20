package com.example.mpaz_pro_3.viewmodel

import android.content.ContentResolver
import android.net.Uri
import com.example.mpaz_pro_3.AppFalsa
import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.conCorrutinas
import com.example.mpaz_pro_3.data.local.Intento
import com.example.mpaz_pro_3.data.local.Respuesta
import com.example.mpaz_pro_3.data.remote.ApiConfig
import com.example.mpaz_pro_3.esperarHasta
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import java.io.ByteArrayOutputStream
import java.io.IOException

// Docente: informe pedagógico con los resultados del curso (GET, PUT y DELETE)
class InformeViewModelTest : FunSpec({

    val objetivo = Datos.objetivo()
    val preguntas = listOf(
        Datos.pregunta(10, tipo = "PRUEBA", nivel = 0),
        Datos.pregunta(11, tipo = "PRUEBA", nivel = 0)
    )
    val intentos = listOf(
        Intento(1, "25377840-7", 1, correctas = 2, total = 2, fecha = 1L),
        Intento(2, "25111222-3", 1, correctas = 1, total = 2, fecha = 2L)
    )
    val respuestas = listOf(
        Respuesta(1, 1, 10, 0, true), Respuesta(2, 1, 11, 0, true),
        Respuesta(3, 2, 10, 0, true), Respuesta(4, 2, 11, 2, false)
    )

    fun preparar(conIntentos: Boolean = true): AppFalsa {
        val falsa = AppFalsa()
        every { falsa.contenidoRepository.objetivos } returns flowOf(listOf(objetivo))
        every { falsa.progresoRepository.intentosDelObjetivo(1) } returns flowOf(if (conIntentos) intentos else emptyList())
        every { falsa.progresoRepository.respuestasDelObjetivo(1) } returns flowOf(if (conIntentos) respuestas else emptyList())
        every { falsa.contenidoRepository.preguntasPrueba(1) } returns flowOf(preguntas)
        return falsa
    }

    test("al abrir sincroniza con el servidor y calcula el informe del primer objetivo") {
        conCorrutinas {
            val falsa = preparar()
            val vm = InformeViewModel(falsa.app)
            advanceUntilIdle()

            coVerify { falsa.progresoRepository.sincronizarIntentos(1) }
            val estado = vm.state.value
            estado.objetivo shouldBe Datos.objetivo()
            estado.estudiantes shouldBe 2
            estado.totalIntentos shouldBe 2
            estado.promedio shouldBe 75
            estado.preguntas.map { it.porcentaje } shouldBe listOf(100, 50)
            estado.fortalezas.size shouldBe 1
            estado.aReforzar.size shouldBe 1
            estado.porEstudiante.first().mejorPuntaje shouldBe "2 de 2"
            estado.sincronizando shouldBe false
        }
    }

    test("sin servidor muestra el aviso y usa los datos guardados") {
        conCorrutinas {
            val falsa = preparar()
            coEvery { falsa.progresoRepository.sincronizarIntentos(1) } throws IOException("sin servidor")
            val vm = InformeViewModel(falsa.app)
            advanceUntilIdle()

            vm.state.value.errorServidor shouldBe ApiConfig.MENSAJE_SIN_SERVIDOR
            vm.state.value.totalIntentos shouldBe 2
        }
    }

    test("sin intentos el informe queda en cero y no se puede validar") {
        conCorrutinas {
            val falsa = preparar(conIntentos = false)
            val vm = InformeViewModel(falsa.app)
            advanceUntilIdle()

            vm.state.value.promedio shouldBe 0
            vm.state.value.preguntas.map { it.porcentaje } shouldBe listOf(0, 0)

            vm.validar()
            vm.state.value.errorRevisado shouldBe "No hay resultados para validar"
        }
    }

    test("validar exige confirmar la revisión y luego guarda en el servidor") {
        conCorrutinas {
            val falsa = preparar()
            val vm = InformeViewModel(falsa.app)
            advanceUntilIdle()

            vm.validar()
            vm.state.value.errorRevisado shouldBe "Confirma que revisaste el informe antes de validarlo"

            vm.onRevisadoChanged(true)
            vm.validar()
            advanceUntilIdle()

            coVerify { falsa.contenidoRepository.actualizarObjetivo(match { it.informeValidado && it.fechaValidacion > 0 }) }
            vm.state.value.mensaje shouldBe "Informe validado"
        }
    }

    test("si el servidor falla al validar muestra el error") {
        conCorrutinas {
            val falsa = preparar()
            coEvery { falsa.contenidoRepository.actualizarObjetivo(any()) } throws IOException("sin servidor")
            val vm = InformeViewModel(falsa.app)
            advanceUntilIdle()

            vm.onRevisadoChanged(true)
            vm.validar()
            advanceUntilIdle()

            vm.state.value.errorRevisado shouldBe ApiConfig.MENSAJE_SIN_SERVIDOR
            vm.state.value.validando shouldBe false
        }
    }

    test("reiniciar los intentos de un estudiante (DELETE)") {
        conCorrutinas {
            val falsa = preparar()
            val vm = InformeViewModel(falsa.app)
            advanceUntilIdle()

            vm.reiniciar("25111222-3")
            advanceUntilIdle()

            coVerify { falsa.progresoRepository.reiniciarIntentos("25111222-3", 1) }
            vm.state.value.mensaje shouldBe "Intentos de 25111222-3 reiniciados"
        }
    }

    test("si no se pueden reiniciar los intentos muestra el error") {
        conCorrutinas {
            val falsa = preparar()
            coEvery { falsa.progresoRepository.reiniciarIntentos(any(), any()) } throws IOException("sin servidor")
            val vm = InformeViewModel(falsa.app)
            advanceUntilIdle()

            vm.reiniciar("25111222-3")
            advanceUntilIdle()

            vm.state.value.errorServidor shouldBe ApiConfig.MENSAJE_SIN_SERVIDOR
        }
    }

    test("exporta el informe como CSV") {
        conCorrutinas {
            val falsa = preparar()
            val uri = mockk<Uri>()
            val salida = ByteArrayOutputStream()
            val resolver = mockk<ContentResolver>()
            every { resolver.openOutputStream(uri) } returns salida
            every { falsa.app.contentResolver } returns resolver
            val vm = InformeViewModel(falsa.app)
            advanceUntilIdle()

            vm.exportar(uri)
            esperarHasta { vm.state.value.mensaje != null }

            vm.state.value.mensaje shouldBe "Informe exportado correctamente"
            val csv = salida.toString(Charsets.UTF_8.name())
            csv shouldContain "Logro promedio;75%"
            csv shouldContain "25377840-7;2 de 2;1"
        }
    }

    test("avisa si no se pudo exportar") {
        conCorrutinas {
            val falsa = preparar()
            val resolver = mockk<ContentResolver>()
            every { resolver.openOutputStream(any()) } throws SecurityException("sin permiso")
            every { falsa.app.contentResolver } returns resolver
            val vm = InformeViewModel(falsa.app)
            advanceUntilIdle()

            vm.exportar(mockk())
            esperarHasta { vm.state.value.mensaje != null }

            vm.state.value.mensaje shouldBe "No se pudo exportar el informe"
        }
    }
})
