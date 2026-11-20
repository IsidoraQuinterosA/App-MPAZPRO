package com.example.mpaz_pro_3.viewmodel

import android.content.ContentResolver
import android.net.Uri
import com.example.mpaz_pro_3.AppFalsa
import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.conCorrutinas
import com.example.mpaz_pro_3.data.local.Guia
import com.example.mpaz_pro_3.esperarHasta
import com.example.mpaz_pro_3.util.Archivos
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle

// Docente: subir guías (recurso nativo Archivos)
class GuiasViewModelTest : FunSpec({

    val guia = Guia(1, 1, "Guía de lectura", "guia.pdf", "/guias/guia.pdf", "application/pdf")
    lateinit var uri: Uri

    beforeTest {
        mockkObject(Archivos)
        mockkStatic(Uri::class)
        uri = mockk(relaxed = true)
        every { uri.toString() } returns "content://guias/guia.pdf"
        every { Uri.parse("content://guias/guia.pdf") } returns uri
        every { Archivos.nombreYTamano(any(), any()) } returns Pair("guia.pdf", 2048L)
        every { Archivos.tipo(any(), any()) } returns "application/pdf"
    }

    afterTest { unmockkAll() }

    fun preparar(): AppFalsa {
        val falsa = AppFalsa()
        every { falsa.contenidoRepository.objetivos } returns flowOf(listOf(Datos.objetivo()))
        every { falsa.guiaRepository.todas } returns flowOf(listOf(guia))
        val resolver = mockk<ContentResolver>()
        every { resolver.takePersistableUriPermission(any(), any()) } throws SecurityException("no permitido")
        every { falsa.app.contentResolver } returns resolver
        return falsa
    }

    test("muestra los objetivos y las guías subidas") {
        conCorrutinas {
            val vm = GuiasViewModel(preparar().app)
            advanceUntilIdle()

            vm.state.value.objetivos.size shouldBe 1
            vm.state.value.guias shouldBe listOf(guia)
        }
    }

    test("no sube la guía si faltan datos") {
        conCorrutinas {
            val falsa = preparar()
            val vm = GuiasViewModel(falsa.app)
            advanceUntilIdle()

            vm.guardar()

            vm.state.value.errores.objetivo.shouldNotBeNull()
            vm.state.value.errores.titulo.shouldNotBeNull()
            vm.state.value.errores.archivo.shouldNotBeNull()
            coVerify(exactly = 0) { falsa.guiaRepository.guardar(any()) }
        }
    }

    test("elegir un archivo guarda su nombre, tamaño y tipo") {
        conCorrutinas {
            val vm = GuiasViewModel(preparar().app)
            vm.onArchivoElegido(uri)

            vm.state.value.archivoNombre shouldBe "guia.pdf"
            vm.state.value.archivoTamano shouldBe 2048L
            vm.state.value.archivoTipo shouldBe "application/pdf"
            vm.state.value.errores.archivo.shouldBeNull()
        }
    }

    test("sube la guía y limpia el formulario") {
        conCorrutinas {
            val falsa = preparar()
            every { Archivos.guardarArchivo(any(), any(), "guia.pdf") } returns "/guias/guia.pdf"
            val vm = GuiasViewModel(falsa.app)
            advanceUntilIdle()

            vm.seleccionar(1)
            vm.onTituloChanged("  Guía de lectura  ")
            vm.onArchivoElegido(uri)
            vm.guardar()
            esperarHasta { vm.state.value.mensaje != null }

            coVerify { falsa.guiaRepository.guardar(Guia(0, 1, "Guía de lectura", "guia.pdf", "/guias/guia.pdf", "application/pdf")) }
            vm.state.value.mensaje shouldBe "Guía subida correctamente"
            vm.state.value.titulo shouldBe ""
            vm.state.value.archivoNombre.shouldBeNull()
        }
    }

    test("avisa si no se pudo copiar el archivo") {
        conCorrutinas {
            every { Archivos.guardarArchivo(any(), any(), any()) } returns null
            val vm = GuiasViewModel(preparar().app)
            advanceUntilIdle()

            vm.seleccionar(1)
            vm.onTituloChanged("Guía de lectura")
            vm.onArchivoElegido(uri)
            vm.guardar()
            esperarHasta { vm.state.value.mensaje != null }

            vm.state.value.mensaje shouldBe "No se pudo copiar el archivo"
        }
    }

    test("eliminar una guía") {
        conCorrutinas {
            val falsa = preparar()
            val vm = GuiasViewModel(falsa.app)
            vm.eliminar(guia)
            advanceUntilIdle()

            coVerify { falsa.guiaRepository.eliminar(guia) }
        }
    }
})
