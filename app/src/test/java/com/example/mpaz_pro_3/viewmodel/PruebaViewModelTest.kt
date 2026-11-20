package com.example.mpaz_pro_3.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.mpaz_pro_3.AppFalsa
import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.conCorrutinas
import com.example.mpaz_pro_3.data.local.Intento
import com.example.mpaz_pro_3.data.local.Respuesta
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle

// Prueba final y su resultado
class PruebaViewModelTest : FunSpec({

    val rut = Datos.RUT
    val ruta = SavedStateHandle(mapOf("objetivoId" to 1))
    val preguntas = listOf(
        Datos.pregunta(10, tipo = "PRUEBA", nivel = 0, correcta = 0),
        Datos.pregunta(11, tipo = "PRUEBA", nivel = 0, correcta = 1)
    )

    fun preparar(intentosUsados: Int, maxIntentos: Int = 2): AppFalsa {
        val falsa = AppFalsa()
        val intentos = List(intentosUsados) { Intento(id = it + 1L, rutEstudiante = rut, objetivoId = 1, correctas = 0, total = 2, fecha = 1L) }
        every { falsa.contenidoRepository.preguntasPrueba(1) } returns flowOf(preguntas)
        every { falsa.contenidoRepository.objetivo(1) } returns flowOf(Datos.objetivo(maxIntentos = maxIntentos))
        every { falsa.progresoRepository.intentos(rut, 1) } returns flowOf(intentos)
        return falsa
    }

    test("carga las preguntas y los intentos disponibles") {
        conCorrutinas {
            val vm = PruebaViewModel(preparar(intentosUsados = 1).app, ruta)
            advanceUntilIdle()

            with(vm.state.value) {
                cargado shouldBe true
                elegidas shouldBe listOf(null, null)
                maxIntentos shouldBe 2
                intentosUsados shouldBe 1
                sinIntentos shouldBe false
                esUltima shouldBe false
            }
        }
    }

    test("no avanza sin responder y vuelve atrás sin pasar de la primera") {
        conCorrutinas {
            val vm = PruebaViewModel(preparar(0).app, ruta)
            advanceUntilIdle()

            vm.siguiente()
            vm.state.value.error.shouldNotBeNull()
            vm.state.value.indice shouldBe 0

            vm.elegir(1)
            vm.state.value.error.shouldBeNull()
            vm.siguiente()
            vm.state.value.indice shouldBe 1
            vm.state.value.esUltima shouldBe true

            vm.anterior()
            vm.anterior()
            vm.state.value.indice shouldBe 0
        }
    }

    test("no se entrega con preguntas sin responder") {
        conCorrutinas {
            val falsa = preparar(0)
            val vm = PruebaViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.elegir(0)
            vm.entregar()
            advanceUntilIdle()

            vm.state.value.error shouldBe "Responde todas las preguntas antes de entregar"
            coVerify(exactly = 0) { falsa.progresoRepository.guardarIntento(any(), any(), any(), any()) }
        }
    }

    test("al entregar guarda el intento y queda listo para ver el resultado") {
        conCorrutinas {
            val falsa = preparar(0)
            coEvery { falsa.progresoRepository.guardarIntento(rut, 1, preguntas, listOf(0, 2)) } returns 7L
            val vm = PruebaViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.elegir(0)
            vm.siguiente()
            vm.elegir(2)
            vm.entregar()
            advanceUntilIdle()

            vm.state.value.intentoGuardado shouldBe 7L
            vm.state.value.enviando shouldBe false

            vm.onNavegado()
            vm.state.value.intentoGuardado.shouldBeNull()
        }
    }

    test("sin intentos disponibles no se puede entregar") {
        conCorrutinas {
            val falsa = preparar(intentosUsados = 2, maxIntentos = 2)
            val vm = PruebaViewModel(falsa.app, ruta)
            advanceUntilIdle()

            vm.state.value.sinIntentos shouldBe true
            vm.elegir(0)
            vm.siguiente()
            vm.elegir(1)
            vm.entregar()
            advanceUntilIdle()

            coVerify(exactly = 0) { falsa.progresoRepository.guardarIntento(any(), any(), any(), any()) }
        }
    }

    test("el resultado muestra cada respuesta con la alternativa correcta") {
        conCorrutinas {
            val falsa = AppFalsa()
            val intento = Intento(id = 7, rutEstudiante = rut, objetivoId = 1, correctas = 1, total = 2, fecha = 1L)
            every { falsa.progresoRepository.intento(7L) } returns flowOf(intento)
            every { falsa.contenidoRepository.preguntasPrueba(1) } returns flowOf(preguntas)
            every { falsa.contenidoRepository.objetivo(1) } returns flowOf(Datos.objetivo())
            every { falsa.progresoRepository.respuestas(7L) } returns flowOf(
                listOf(
                    Respuesta(1, 7, 10, 0, true),
                    Respuesta(2, 7, 11, -1, false),
                    Respuesta(3, 7, 99, 0, false) // Pregunta que ya no existe: se omite
                )
            )

            val vm = ResultadoViewModel(falsa.app, SavedStateHandle(mapOf("intentoId" to 7L)))
            advanceUntilIdle()

            with(vm.state.value) {
                cargado shouldBe true
                asignatura shouldBe "LENGUAJE"
                correctas shouldBe 1
                total shouldBe 2
                items.size shouldBe 2
                items[0].respuesta shouldBe "A10"
                items[0].esCorrecta shouldBe true
                items[1].respuesta shouldBe "Sin respuesta"
                items[1].correcta shouldBe "B11"
            }
        }
    }

    test("si el intento no existe el resultado queda sin cargar") {
        conCorrutinas {
            val falsa = AppFalsa()
            every { falsa.progresoRepository.intento(any()) } returns flowOf(null)

            val vm = ResultadoViewModel(falsa.app, SavedStateHandle())
            advanceUntilIdle()

            vm.state.value.cargado shouldBe false
        }
    }
})
