package com.example.mpaz_pro_3.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.mpaz_pro_3.AppFalsa
import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.conCorrutinas
import com.example.mpaz_pro_3.data.local.Contenido
import com.example.mpaz_pro_3.data.local.ObjetivoAprendizaje
import com.example.mpaz_pro_3.data.remote.ApiConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import java.io.IOException

// UTP: aprobar u observar un contenido (PUT al microservicio)
class RevisionViewModelTest : FunSpec({

    val pendiente = Datos.objetivo(estado = "PENDIENTE", asignado = true)
    val ruta = SavedStateHandle(mapOf("objetivoId" to 1))

    fun preparar(objetivo: ObjetivoAprendizaje? = pendiente): AppFalsa {
        val falsa = AppFalsa()
        every { falsa.contenidoRepository.objetivo(1) } returns flowOf(objetivo)
        every { falsa.contenidoRepository.contenidos(1) } returns flowOf(listOf(Contenido(1, 1, "T", "E", "Ej")))
        every { falsa.contenidoRepository.desafios(1) } returns flowOf(listOf(Datos.pregunta(1)))
        every { falsa.contenidoRepository.preguntasPrueba(1) } returns flowOf(listOf(Datos.pregunta(10, tipo = "PRUEBA", nivel = 0)))
        return falsa
    }

    test("muestra el objetivo con su contenido y todas sus preguntas") {
        conCorrutinas {
            val vm = RevisionViewModel(preparar().app, ruta)
            advanceUntilIdle()

            vm.state.value.objetivo shouldBe pendiente
            vm.state.value.contenido?.titulo shouldBe "T"
            vm.state.value.preguntas.map { it.id } shouldBe listOf(1, 10)
        }
    }

    test("aprobar cambia el estado en el servidor") {
        conCorrutinas {
            val falsa = preparar()
            val vm = RevisionViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.aprobar()
            advanceUntilIdle()

            coVerify { falsa.contenidoRepository.actualizarObjetivo(pendiente.copy(estado = "APROBADO", observacion = "")) }
            vm.state.value.mensaje shouldBe "Contenido aprobado"
        }
    }

    test("pedir cambios exige una observación y quita la asignación") {
        conCorrutinas {
            val falsa = preparar()
            val vm = RevisionViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.pedirCambios()
            vm.state.value.error shouldBe "Escribe qué se debe cambiar"

            vm.onObservacionChanged("  Agregar más ejemplos con imágenes  ")
            vm.state.value.error.shouldBeNull()
            vm.pedirCambios()
            advanceUntilIdle()

            coVerify {
                falsa.contenidoRepository.actualizarObjetivo(
                    pendiente.copy(estado = "OBSERVADO", observacion = "Agregar más ejemplos con imágenes", asignado = false)
                )
            }
            vm.state.value.mensaje shouldBe "Observaciones enviadas al docente"
        }
    }

    test("si el servidor no responde muestra el error al aprobar y al observar") {
        conCorrutinas {
            val falsa = preparar()
            coEvery { falsa.contenidoRepository.actualizarObjetivo(any()) } throws IOException("sin servidor")
            val vm = RevisionViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.aprobar()
            advanceUntilIdle()
            vm.state.value.error shouldBe ApiConfig.MENSAJE_SIN_SERVIDOR

            vm.onObservacionChanged("Revisar la redacción de la pregunta")
            vm.pedirCambios()
            advanceUntilIdle()
            vm.state.value.error shouldBe ApiConfig.MENSAJE_SIN_SERVIDOR
            vm.state.value.procesando shouldBe false
        }
    }

    test("sin objetivo cargado no hace nada") {
        conCorrutinas {
            val falsa = preparar(objetivo = null)
            val vm = RevisionViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.aprobar()
            vm.pedirCambios()
            advanceUntilIdle()

            coVerify(exactly = 0) { falsa.contenidoRepository.actualizarObjetivo(any()) }
        }
    }
})
