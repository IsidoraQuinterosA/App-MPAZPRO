package com.example.mpaz_pro_3.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.mpaz_pro_3.AppFalsa
import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.conCorrutinas
import com.example.mpaz_pro_3.data.local.Progreso
import com.example.mpaz_pro_3.data.local.Recompensa
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import java.io.IOException

class DesafiosViewModelTest : FunSpec({

    val rut = Datos.RUT
    val ruta = SavedStateHandle(mapOf("objetivoId" to 1))
    val desafios = listOf(
        Datos.pregunta(1, nivel = 1, correcta = 0),
        Datos.pregunta(2, nivel = 2, correcta = 1),
        Datos.pregunta(3, nivel = 3, correcta = 2)
    )
    val pikachu = Recompensa(rut, 1, 25, "Pikachu", "img", "Eléctrico", 0L)

    fun preparar(progreso: Progreso?): AppFalsa {
        val falsa = AppFalsa()
        every { falsa.progresoRepository.progreso(rut, 1) } returns flowOf(progreso)
        every { falsa.contenidoRepository.desafios(1) } returns flowOf(desafios)
        every { falsa.contenidoRepository.objetivo(1) } returns flowOf(Datos.objetivo())
        return falsa
    }

    test("sin avance parte en el nivel 1") {
        conCorrutinas {
            val vm = DesafiosViewModel(preparar(null).app, ruta)
            advanceUntilIdle()

            vm.state.value.cargado shouldBe true
            vm.state.value.nivelActual shouldBe 1
            vm.state.value.pregunta?.id shouldBe 1
            vm.state.value.asignatura shouldBe "LENGUAJE"
        }
    }

    test("una respuesta incorrecta no guarda avance") {
        conCorrutinas {
            val falsa = preparar(null)
            val vm = DesafiosViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.elegir(2)
            advanceUntilIdle()

            vm.state.value.esCorrecta shouldBe false
            coVerify(exactly = 0) { falsa.progresoRepository.guardarProgreso(any()) }
        }
    }

    test("una respuesta correcta guarda el nivel superado y no se puede volver a responder") {
        conCorrutinas {
            val falsa = preparar(null)
            val vm = DesafiosViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.elegir(0)
            advanceUntilIdle()
            vm.elegir(1)

            vm.state.value.esCorrecta shouldBe true
            vm.state.value.seleccion shouldBe 0
            coVerify(exactly = 1) { falsa.progresoRepository.guardarProgreso(Progreso(rut, 1, nivelSuperado = 1)) }
        }
    }

    test("no baja el nivel guardado si ya había superado uno mayor") {
        conCorrutinas {
            val falsa = preparar(Progreso(rut, 1, nivelSuperado = 1))
            val vm = DesafiosViewModel(falsa.app, ruta)
            advanceUntilIdle()
            vm.state.value.nivelActual shouldBe 2

            every { falsa.progresoRepository.progreso(rut, 1) } returns flowOf(Progreso(rut, 1, nivelSuperado = 3))
            vm.elegir(1)
            advanceUntilIdle()

            coVerify(exactly = 0) { falsa.progresoRepository.guardarProgreso(any()) }
        }
    }

    test("al superar los 3 niveles gana un Pokémon de la PokeAPI") {
        conCorrutinas {
            val falsa = preparar(Progreso(rut, 1, nivelSuperado = 2))
            coEvery { falsa.recompensaRepository.ganarRecompensa(rut, 1) } returns pikachu
            val vm = DesafiosViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.elegir(2)
            vm.siguienteNivel()
            advanceUntilIdle()

            vm.state.value.nivelActual shouldBe 4
            vm.state.value.pregunta.shouldBeNull()
            vm.state.value.recompensa shouldBe pikachu
            vm.state.value.cargandoRecompensa shouldBe false
        }
    }

    test("pasar de nivel antes del 3 no pide premio") {
        conCorrutinas {
            val falsa = preparar(null)
            val vm = DesafiosViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.siguienteNivel()
            advanceUntilIdle()

            vm.state.value.nivelActual shouldBe 2
            vm.state.value.seleccion.shouldBeNull()
            coVerify(exactly = 0) { falsa.recompensaRepository.ganarRecompensa(any(), any()) }
        }
    }

    test("si ya había terminado, muestra el Pokémon guardado") {
        conCorrutinas {
            val falsa = preparar(Progreso(rut, 1, nivelSuperado = 3))
            coEvery { falsa.recompensaRepository.recompensa(rut, 1) } returns pikachu
            val vm = DesafiosViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.state.value.recompensa shouldBe pikachu
            coVerify(exactly = 0) { falsa.recompensaRepository.ganarRecompensa(any(), any()) }
        }
    }

    test("sin internet muestra un error y permite reintentar") {
        conCorrutinas {
            val falsa = preparar(Progreso(rut, 1, nivelSuperado = 3))
            coEvery { falsa.recompensaRepository.recompensa(rut, 1) } returns null
            coEvery { falsa.recompensaRepository.ganarRecompensa(rut, 1) } throws IOException("sin internet")
            val vm = DesafiosViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.state.value.errorRecompensa.shouldNotBeNull()
            vm.state.value.recompensa.shouldBeNull()

            coEvery { falsa.recompensaRepository.ganarRecompensa(rut, 1) } returns pikachu
            vm.obtenerRecompensa()
            advanceUntilIdle()

            vm.state.value.errorRecompensa.shouldBeNull()
            vm.state.value.recompensa shouldBe pikachu
        }
    }
})
