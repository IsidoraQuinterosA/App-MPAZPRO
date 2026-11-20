package com.example.mpaz_pro_3.viewmodel

import com.example.mpaz_pro_3.AppFalsa
import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.conCorrutinas
import com.example.mpaz_pro_3.model.Rol
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.advanceUntilIdle

class LoginViewModelTest : FunSpec({

    test("escribir el RUT limpia su error") {
        val vm = LoginViewModel(AppFalsa().app)
        vm.ingresar(Rol.ESTUDIANTE)
        vm.state.value.errores.rut.shouldNotBeNull()

        vm.onRutChanged("25418732-1")
        vm.state.value.rut shouldBe "25418732-1"
        vm.state.value.errores.rut.shouldBeNull()
    }

    test("el PIN del estudiante acepta solo 4 números; la contraseña adulta no se filtra") {
        val vm = LoginViewModel(AppFalsa().app)
        vm.onClaveChanged("12a345", Rol.ESTUDIANTE)
        vm.state.value.clave shouldBe "1234"

        vm.onClaveChanged("docente123", Rol.DOCENTE)
        vm.state.value.clave shouldBe "docente123"
    }

    test("mostrar u ocultar la clave") {
        val vm = LoginViewModel(AppFalsa().app)
        vm.onMostrarClaveChanged()
        vm.state.value.mostrarClave shouldBe true
        vm.onMostrarClaveChanged()
        vm.state.value.mostrarClave shouldBe false
    }

    test("con datos inválidos muestra errores y no consulta el repositorio") {
        val falsa = AppFalsa()
        val vm = LoginViewModel(falsa.app)
        vm.onRutChanged("123")
        vm.onClaveChanged("12", Rol.DOCENTE)

        vm.ingresar(Rol.DOCENTE)

        vm.state.value.errores.rut.shouldNotBeNull()
        vm.state.value.errores.clave.shouldNotBeNull()
        coVerify(exactly = 0) { falsa.usuarioRepository.iniciarSesion(any(), any(), any()) }
    }

    test("con datos correctos inicia sesión") {
        conCorrutinas {
            val falsa = AppFalsa()
            coEvery { falsa.usuarioRepository.iniciarSesion(Datos.RUT, "0703", Rol.ESTUDIANTE) } returns Datos.sesionEstudiante
            val vm = LoginViewModel(falsa.app)
            vm.onRutChanged("25.418.732-1")
            vm.onClaveChanged("0703", Rol.ESTUDIANTE)

            vm.ingresar(Rol.ESTUDIANTE)
            advanceUntilIdle()

            vm.state.value.cargando shouldBe false
            vm.state.value.ingresoExitoso shouldBe true

            vm.onIngresoNavegado()
            vm.state.value.ingresoExitoso shouldBe false
        }
    }

    test("si el RUT o la clave no coinciden muestra un mensaje") {
        conCorrutinas {
            val falsa = AppFalsa()
            coEvery { falsa.usuarioRepository.iniciarSesion(any(), any(), any()) } returns null
            val vm = LoginViewModel(falsa.app)
            vm.onRutChanged(Datos.RUT)
            vm.onClaveChanged("docente123", Rol.DOCENTE)

            vm.ingresar(Rol.DOCENTE)
            advanceUntilIdle()

            vm.state.value.mensajeError shouldBe "El RUT o la clave no son correctos"
            vm.state.value.ingresoExitoso shouldBe false
        }
    }
})
