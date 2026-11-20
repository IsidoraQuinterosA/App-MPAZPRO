package com.example.mpaz_pro_3.viewmodel

import com.example.mpaz_pro_3.AppFalsa
import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.conCorrutinas
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import java.io.IOException

class SesionViewModelTest : FunSpec({

    test("sin sesión guardada la app parte en la elección de rol y sincroniza con el servidor") {
        conCorrutinas {
            val falsa = AppFalsa(sesion = null)
            val vm = SesionViewModel(falsa.app)
            advanceUntilIdle()

            vm.rutaInicial.value shouldBe "roles"
            vm.sinServidor.value shouldBe false
            coVerify { falsa.usuarioRepository.cargarDatosIniciales() }
            coVerify { falsa.contenidoRepository.cargarDatosIniciales() }
            coVerify { falsa.contenidoRepository.sincronizar() }
        }
    }

    test("con sesión de docente parte en su panel y avisa si no hay servidor") {
        conCorrutinas {
            val falsa = AppFalsa(sesion = Datos.sesionDocente)
            coEvery { falsa.contenidoRepository.sincronizar() } throws IOException("sin conexión")
            val vm = SesionViewModel(falsa.app)
            advanceUntilIdle()

            vm.rutaInicial.value shouldBe "docente"
            vm.sinServidor.value shouldBe true
            vm.sesion.first { it != null } shouldBe Datos.sesionDocente
        }
    }

    test("cerrar sesión borra la sesión y avisa a la pantalla") {
        conCorrutinas {
            val falsa = AppFalsa()
            val vm = SesionViewModel(falsa.app)
            var cerrada = false

            vm.cerrarSesion { cerrada = true }
            advanceUntilIdle()

            cerrada shouldBe true
            vm.cerrando.value shouldBe false
            coVerify { falsa.usuarioRepository.cerrarSesion() }
        }
    }
})
