package com.example.mpaz_pro_3.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.mpaz_pro_3.AppFalsa
import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.conCorrutinas
import com.example.mpaz_pro_3.data.local.Contenido
import com.example.mpaz_pro_3.data.local.Guia
import com.example.mpaz_pro_3.data.local.Intento
import com.example.mpaz_pro_3.data.local.Progreso
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle

// Pantallas Unidad y Aprende del estudiante
class EstudianteFlujoViewModelTest : FunSpec({

    val rut = Datos.RUT
    val ruta = SavedStateHandle(mapOf("objetivoId" to 1))

    test("la unidad junta el objetivo, el avance, los intentos y las guías") {
        conCorrutinas {
            val falsa = AppFalsa()
            val intentos = listOf(
                Intento(id = 9, rutEstudiante = rut, objetivoId = 1, correctas = 3, total = 4, fecha = 2L),
                Intento(id = 8, rutEstudiante = rut, objetivoId = 1, correctas = 1, total = 4, fecha = 1L)
            )
            val guia = Guia(1, 1, "Guía", "guia.pdf", "/ruta", "application/pdf")
            every { falsa.contenidoRepository.objetivo(1) } returns flowOf(Datos.objetivo())
            every { falsa.progresoRepository.progreso(rut, 1) } returns flowOf(Progreso(rut, 1, aprendeVisto = true, nivelSuperado = 2))
            every { falsa.progresoRepository.intentos(rut, 1) } returns flowOf(intentos)
            every { falsa.guiaRepository.guias(1) } returns flowOf(listOf(guia))

            val vm = UnidadViewModel(falsa.app, ruta)
            advanceUntilIdle()

            with(vm.state.value) {
                objetivo shouldBe Datos.objetivo()
                aprendeVisto shouldBe true
                nivelSuperado shouldBe 2
                intentosUsados shouldBe 2
                ultimoIntentoId shouldBe 9L
                guias shouldBe listOf(guia)
            }
        }
    }

    test("sin avance guardado la unidad parte desde cero") {
        conCorrutinas {
            val falsa = AppFalsa()
            every { falsa.contenidoRepository.objetivo(1) } returns flowOf(Datos.objetivo())
            every { falsa.progresoRepository.progreso(rut, 1) } returns flowOf(null)
            every { falsa.progresoRepository.intentos(rut, 1) } returns flowOf(emptyList())
            every { falsa.guiaRepository.guias(1) } returns flowOf(emptyList())

            val vm = UnidadViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.state.value.aprendeVisto shouldBe false
            vm.state.value.nivelSuperado shouldBe 0
            vm.state.value.ultimoIntentoId shouldBe null
        }
    }

    test("Aprende muestra el contenido y al terminar guarda el avance") {
        conCorrutinas {
            val falsa = AppFalsa()
            val contenido = Contenido(1, 1, "Sustantivos", "Explicación", "Ejemplo")
            every { falsa.contenidoRepository.contenidos(1) } returns flowOf(listOf(contenido))
            every { falsa.contenidoRepository.objetivo(1) } returns flowOf(Datos.objetivo())
            every { falsa.progresoRepository.progreso(rut, 1) } returns flowOf(Progreso(rut, 1, nivelSuperado = 2))

            val vm = AprendeViewModel(falsa.app, ruta)
            advanceUntilIdle()
            vm.state.value.contenido shouldBe contenido
            vm.state.value.asignatura shouldBe "LENGUAJE"

            vm.terminar()
            advanceUntilIdle()

            vm.state.value.terminado shouldBe true
            coVerify { falsa.progresoRepository.guardarProgreso(Progreso(rut, 1, aprendeVisto = true, nivelSuperado = 2)) }
        }
    }

    test("Aprende crea el avance si el estudiante no tenía") {
        conCorrutinas {
            val falsa = AppFalsa()
            every { falsa.contenidoRepository.contenidos(1) } returns flowOf(emptyList())
            every { falsa.contenidoRepository.objetivo(1) } returns flowOf(null)
            every { falsa.progresoRepository.progreso(rut, 1) } returns flowOf(null)

            val vm = AprendeViewModel(falsa.app, ruta)
            vm.terminar()
            advanceUntilIdle()

            vm.state.value.asignatura shouldBe ""
            coVerify(exactly = 1) { falsa.progresoRepository.guardarProgreso(Progreso(rut, 1, aprendeVisto = true)) }
        }
    }
})
