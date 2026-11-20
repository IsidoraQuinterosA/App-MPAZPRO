package com.example.mpaz_pro_3.viewmodel

import com.example.mpaz_pro_3.AppFalsa
import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.conCorrutinas
import com.example.mpaz_pro_3.data.local.Pregunta
import com.example.mpaz_pro_3.data.remote.ApiConfig
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

// Docente: CRUD de preguntas conectado al microservicio ms-contenidos
class PreguntasViewModelTest : FunSpec({

    val existentes = listOf(Datos.pregunta(1), Datos.pregunta(10, tipo = "PRUEBA", nivel = 0))

    fun preparar(): AppFalsa {
        val falsa = AppFalsa()
        every { falsa.contenidoRepository.objetivos } returns flowOf(listOf(Datos.objetivo(1), Datos.objetivo(2)))
        every { falsa.contenidoRepository.todasLasPreguntas(1) } returns flowOf(existentes)
        every { falsa.contenidoRepository.todasLasPreguntas(2) } returns flowOf(emptyList())
        return falsa
    }

    fun PreguntasViewModel.llenarFormulario() {
        onEnunciadoChanged("  ¿Cuál es un sustantivo?  ")
        onOpcionChanged(0, "Correr")
        onOpcionChanged(1, "Casa")
        onOpcionChanged(2, "Rápido")
        onCorrectaChanged(1)
    }

    test("Read: al abrir muestra las preguntas del primer objetivo") {
        conCorrutinas {
            val vm = PreguntasViewModel(preparar().app)
            advanceUntilIdle()

            vm.state.value.seleccionadoId shouldBe 1
            vm.state.value.preguntas shouldBe existentes

            vm.seleccionarObjetivo(2)
            advanceUntilIdle()
            vm.state.value.preguntas shouldBe emptyList()
        }
    }

    test("el formulario vacío muestra un error en cada campo") {
        conCorrutinas {
            val falsa = preparar()
            val vm = PreguntasViewModel(falsa.app)
            advanceUntilIdle()

            vm.nuevaPregunta()
            vm.onNivelChanged(0)
            vm.guardar()

            with(vm.state.value.errores) {
                hayErrores shouldBe true
                enunciado.shouldNotBeNull()
                opcionA.shouldNotBeNull()
                opcionB.shouldNotBeNull()
                opcionC.shouldNotBeNull()
                correcta.shouldNotBeNull()
                nivel.shouldNotBeNull()
            }
            coVerify(exactly = 0) { falsa.contenidoRepository.guardarPregunta(any()) }
        }
    }

    test("al escribir se limpia el error del campo") {
        conCorrutinas {
            val vm = PreguntasViewModel(preparar().app)
            advanceUntilIdle()
            vm.nuevaPregunta()
            vm.guardar()

            vm.llenarFormulario()
            vm.onNivelChanged(2)
            vm.onTipoChanged("PRUEBA")

            vm.state.value.errores.hayErrores shouldBe false
            vm.state.value.tipo shouldBe "PRUEBA"
        }
    }

    test("las alternativas repetidas no se aceptan") {
        conCorrutinas {
            val vm = PreguntasViewModel(preparar().app)
            advanceUntilIdle()
            vm.nuevaPregunta()
            vm.llenarFormulario()
            vm.onOpcionChanged(2, "casa")

            vm.guardar()

            vm.state.value.errores.opcionC shouldBe "Las alternativas no se pueden repetir"
        }
    }

    test("Create: una pregunta nueva se envía al servidor (POST)") {
        conCorrutinas {
            val falsa = preparar()
            val vm = PreguntasViewModel(falsa.app)
            advanceUntilIdle()

            vm.nuevaPregunta()
            vm.llenarFormulario()
            vm.onNivelChanged(3)
            vm.guardar()
            advanceUntilIdle()

            coVerify {
                falsa.contenidoRepository.guardarPregunta(
                    Pregunta(0, 1, "DESAFIO", 3, "¿Cuál es un sustantivo?", "Correr", "Casa", "Rápido", 1)
                )
            }
            vm.state.value.mensaje shouldBe "Pregunta creada"
            vm.state.value.formularioAbierto shouldBe false
        }
    }

    test("Update: editar una pregunta de prueba la envía con nivel 0 (PUT)") {
        conCorrutinas {
            val falsa = preparar()
            val vm = PreguntasViewModel(falsa.app)
            advanceUntilIdle()

            vm.editarPregunta(existentes[1])
            vm.state.value.editandoId shouldBe 10
            vm.state.value.nivel shouldBe 1
            vm.onEnunciadoChanged("Pregunta editada")
            vm.guardar()
            advanceUntilIdle()

            coVerify { falsa.contenidoRepository.guardarPregunta(match { it.id == 10 && it.nivel == 0 && it.enunciado == "Pregunta editada" }) }
            vm.state.value.mensaje shouldBe "Pregunta actualizada"
        }
    }

    test("editar un desafío conserva su nivel y se puede cancelar") {
        conCorrutinas {
            val vm = PreguntasViewModel(preparar().app)
            advanceUntilIdle()

            vm.editarPregunta(Datos.pregunta(5, nivel = 3))
            vm.state.value.nivel shouldBe 3
            vm.cerrarFormulario()
            vm.state.value.formularioAbierto shouldBe false
        }
    }

    test("si el servidor no responde al guardar muestra el error") {
        conCorrutinas {
            val falsa = preparar()
            coEvery { falsa.contenidoRepository.guardarPregunta(any()) } throws IOException("sin servidor")
            val vm = PreguntasViewModel(falsa.app)
            advanceUntilIdle()

            vm.nuevaPregunta()
            vm.llenarFormulario()
            vm.guardar()
            advanceUntilIdle()

            vm.state.value.errorServidor shouldBe ApiConfig.MENSAJE_SIN_SERVIDOR
            vm.state.value.formularioAbierto shouldBe true
            vm.state.value.guardando shouldBe false
        }
    }

    test("Delete: eliminar una pregunta (DELETE)") {
        conCorrutinas {
            val falsa = preparar()
            val vm = PreguntasViewModel(falsa.app)
            advanceUntilIdle()

            vm.eliminar(existentes[0])
            advanceUntilIdle()

            coVerify { falsa.contenidoRepository.eliminarPregunta(existentes[0]) }
            vm.state.value.mensaje shouldBe "Pregunta eliminada"
        }
    }

    test("si no se puede eliminar muestra el error") {
        conCorrutinas {
            val falsa = preparar()
            coEvery { falsa.contenidoRepository.eliminarPregunta(any()) } throws IOException("sin servidor")
            val vm = PreguntasViewModel(falsa.app)
            advanceUntilIdle()

            vm.eliminar(existentes[0])
            advanceUntilIdle()

            vm.state.value.errorServidor shouldBe ApiConfig.MENSAJE_SIN_SERVIDOR
            vm.state.value.mensaje.shouldBeNull()
        }
    }
})
