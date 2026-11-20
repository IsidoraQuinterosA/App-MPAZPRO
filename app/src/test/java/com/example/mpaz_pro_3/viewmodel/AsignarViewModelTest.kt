package com.example.mpaz_pro_3.viewmodel

import android.net.Uri
import com.example.mpaz_pro_3.AppFalsa
import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.conCorrutinas
import com.example.mpaz_pro_3.data.local.Contenido
import com.example.mpaz_pro_3.data.remote.ApiConfig
import com.example.mpaz_pro_3.esperarHasta
import com.example.mpaz_pro_3.util.Archivos
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import java.io.IOException

// Docente: asignar objetivos al curso (PUT al microservicio)
class AsignarViewModelTest : FunSpec({

    val aprobado = Datos.objetivo(id = 1, estado = "APROBADO", asignado = false, maxIntentos = 2)
    val pendiente = Datos.objetivo(id = 2, estado = "PENDIENTE", asignado = false)
    val contenido = Contenido(1, 1, "Título", "Explicación", "Ejemplo", imagen = "")

    fun preparar(): AppFalsa {
        val falsa = AppFalsa()
        every { falsa.contenidoRepository.objetivos } returns flowOf(listOf(aprobado, pendiente))
        every { falsa.contenidoRepository.contenidos(1) } returns flowOf(listOf(contenido.copy(imagen = "foto.jpg")))
        every { falsa.contenidoRepository.contenidos(2) } returns flowOf(emptyList())
        return falsa
    }

    test("al elegir un objetivo se cargan sus datos y su imagen") {
        conCorrutinas {
            val vm = AsignarViewModel(preparar().app)
            advanceUntilIdle()
            vm.state.value.objetivos.size shouldBe 2

            vm.seleccionar(aprobado)
            advanceUntilIdle()

            vm.state.value.seleccionado shouldBe aprobado
            vm.state.value.intentos shouldBe "2"
            vm.state.value.asignado shouldBe true
            vm.state.value.imagen shouldBe "foto.jpg"
        }
    }

    test("los intentos solo aceptan números") {
        conCorrutinas {
            val vm = AsignarViewModel(preparar().app)
            vm.onIntentosChanged("3a45")
            vm.state.value.intentos shouldBe "34"
            vm.onAsignadoChanged(false)
            vm.state.value.asignado shouldBe false
        }
    }

    test("no guarda sin objetivo o con un objetivo no aprobado") {
        conCorrutinas {
            val falsa = preparar()
            val vm = AsignarViewModel(falsa.app)
            advanceUntilIdle()

            vm.guardar()
            vm.state.value.errores.objetivo shouldBe "Selecciona un objetivo"

            vm.seleccionar(pendiente)
            advanceUntilIdle()
            vm.guardar()
            vm.state.value.errores.objetivo.shouldNotBeNull()
            coVerify(exactly = 0) { falsa.contenidoRepository.actualizarObjetivo(any()) }
        }
    }

    test("asigna el objetivo con la cantidad de intentos") {
        conCorrutinas {
            val falsa = preparar()
            val vm = AsignarViewModel(falsa.app)
            advanceUntilIdle()

            vm.seleccionar(aprobado)
            vm.onIntentosChanged("3")
            vm.guardar()
            advanceUntilIdle()

            coVerify { falsa.contenidoRepository.actualizarObjetivo(aprobado.copy(asignado = true, maxIntentos = 3)) }
            vm.state.value.mensaje shouldBe "Objetivo asignado al curso con 3 intento(s)"
        }
    }

    test("quitar la asignación muestra otro mensaje") {
        conCorrutinas {
            val vm = AsignarViewModel(preparar().app)
            advanceUntilIdle()

            vm.seleccionar(aprobado)
            vm.onAsignadoChanged(false)
            vm.guardar()
            advanceUntilIdle()

            vm.state.value.mensaje shouldBe "Se quitó la asignación del objetivo"
        }
    }

    test("si el servidor no responde muestra el error") {
        conCorrutinas {
            val falsa = preparar()
            coEvery { falsa.contenidoRepository.actualizarObjetivo(any()) } throws IOException("sin servidor")
            val vm = AsignarViewModel(falsa.app)
            advanceUntilIdle()

            vm.seleccionar(aprobado)
            vm.guardar()
            advanceUntilIdle()

            vm.state.value.errorServidor shouldBe ApiConfig.MENSAJE_SIN_SERVIDOR
            vm.state.value.guardando shouldBe false
            vm.state.value.mensaje.shouldBeNull()
        }
    }

    test("la imagen de apoyo pide elegir un objetivo primero") {
        conCorrutinas {
            val vm = AsignarViewModel(preparar().app)
            vm.guardarImagen(mockk<Uri>())
            vm.state.value.errores.objetivo.shouldNotBeNull()
        }
    }

    test("guarda la imagen de apoyo elegida en la galería") {
        mockkObject(Archivos)
        try {
            conCorrutinas {
                val falsa = preparar()
                every { falsa.contenidoRepository.contenidos(1) } returns flowOf(listOf(contenido))
                every { Archivos.guardarImagen(any(), any()) } returns "/imagenes/nueva.jpg"
                val vm = AsignarViewModel(falsa.app)
                advanceUntilIdle()

                vm.seleccionar(aprobado)
                vm.guardarImagen(mockk<Uri>())
                esperarHasta { vm.state.value.mensaje != null }

                vm.state.value.imagen shouldBe "/imagenes/nueva.jpg"
                coVerify { falsa.contenidoRepository.actualizarContenido(contenido.copy(imagen = "/imagenes/nueva.jpg")) }
            }
        } finally {
            unmockkObject(Archivos)
        }
    }

    test("avisa si no se pudo copiar la imagen") {
        mockkObject(Archivos)
        try {
            conCorrutinas {
                every { Archivos.guardarImagen(any(), any()) } returns null
                val vm = AsignarViewModel(preparar().app)
                advanceUntilIdle()

                vm.seleccionar(aprobado)
                vm.guardarImagen(mockk<Uri>())
                esperarHasta { vm.state.value.mensaje != null }

                vm.state.value.mensaje shouldBe "No se pudo guardar la imagen"
            }
        } finally {
            unmockkObject(Archivos)
        }
    }
})
