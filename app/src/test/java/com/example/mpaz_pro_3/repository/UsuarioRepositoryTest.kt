package com.example.mpaz_pro_3.repository

import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.data.local.Usuario
import com.example.mpaz_pro_3.data.local.UsuarioDao
import com.example.mpaz_pro_3.data.preferences.SesionDataStore
import com.example.mpaz_pro_3.data.repository.UsuarioRepository
import com.example.mpaz_pro_3.model.Rol
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class UsuarioRepositoryTest : FunSpec({

    fun crear(usuarios: Int = 1): Triple<UsuarioRepository, UsuarioDao, SesionDataStore> {
        val dao = mockk<UsuarioDao>(relaxed = true)
        val dataStore = mockk<SesionDataStore>(relaxed = true)
        every { dataStore.sesion } returns flowOf(Datos.sesionEstudiante)
        coEvery { dao.contar() } returns usuarios
        return Triple(UsuarioRepository(dao, dataStore), dao, dataStore)
    }

    test("la sesión se lee desde DataStore") {
        val (repo, _, _) = crear()
        repo.sesion.first() shouldBe Datos.sesionEstudiante
    }

    test("carga los usuarios de ejemplo solo si la tabla está vacía") {
        val (vacio, daoVacio, _) = crear(usuarios = 0)
        vacio.cargarDatosIniciales()
        coVerify(exactly = 1) { daoVacio.insertar(any()) }

        val (lleno, daoLleno, _) = crear(usuarios = 5)
        lleno.cargarDatosIniciales()
        coVerify(exactly = 0) { daoLleno.insertar(any()) }
    }

    test("inicia sesión con datos correctos y la guarda en DataStore") {
        val (repo, dao, dataStore) = crear()
        coEvery { dao.buscar(Datos.RUT, "0703", "ESTUDIANTE") } returns
            Usuario(Datos.RUT, "Ana Pérez", "ESTUDIANTE", "0703")

        val sesion = repo.iniciarSesion(Datos.RUT, "0703", Rol.ESTUDIANTE)

        sesion?.nombre shouldBe "Ana Pérez"
        sesion?.rol shouldBe Rol.ESTUDIANTE
        coVerify { dataStore.guardar(Datos.sesionEstudiante) }
    }

    test("no inicia sesión si el usuario no existe") {
        val (repo, dao, dataStore) = crear()
        coEvery { dao.buscar(any(), any(), any()) } returns null

        repo.iniciarSesion(Datos.RUT, "9999", Rol.ESTUDIANTE).shouldBeNull()
        coVerify(exactly = 0) { dataStore.guardar(any()) }
    }

    test("cerrar sesión borra los datos guardados") {
        val (repo, _, dataStore) = crear()
        repo.cerrarSesion()
        coVerify { dataStore.borrar() }
    }
})
