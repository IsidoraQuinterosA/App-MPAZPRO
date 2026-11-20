package com.example.mpaz_pro_3.viewmodel

import com.example.mpaz_pro_3.AppFalsa
import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.conCorrutinas
import com.example.mpaz_pro_3.data.local.Recompensa
import com.example.mpaz_pro_3.util.ConexionInternet
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle

// ViewModel que solo exponen listas o estados leídos desde los repositorios
class ListasViewModelTest : FunSpec({

    test("el estudiante ve los objetivos guardados") {
        conCorrutinas {
            val falsa = AppFalsa()
            every { falsa.contenidoRepository.objetivos } returns flowOf(listOf(Datos.objetivo()))
            EstudianteViewModel(falsa.app).objetivos.first { it.isNotEmpty() } shouldBe listOf(Datos.objetivo())
        }
    }

    test("UTP ve los objetivos con su estado de revisión") {
        conCorrutinas {
            val falsa = AppFalsa()
            val pendiente = Datos.objetivo(estado = "PENDIENTE")
            every { falsa.contenidoRepository.objetivos } returns flowOf(listOf(pendiente))
            UtpViewModel(falsa.app).objetivos.first { it.isNotEmpty() } shouldBe listOf(pendiente)
        }
    }

    test("el aviso de conexión refleja el estado de la red") {
        conCorrutinas {
            val falsa = AppFalsa()
            val conexion = mockk<ConexionInternet>()
            every { conexion.conectado } returns flowOf(false)
            every { falsa.app.conexionInternet } returns conexion

            ConexionViewModel(falsa.app).conectado.first { !it } shouldBe false
        }
    }

    test("la colección muestra los Pokémon ganados por el estudiante") {
        conCorrutinas {
            val falsa = AppFalsa()
            val lista = listOf(Recompensa(Datos.RUT, 1, 25, "Pikachu", "img", "Eléctrico", 0L))
            every { falsa.recompensaRepository.coleccion(Datos.RUT) } returns flowOf(lista)

            val vm = ColeccionViewModel(falsa.app)
            advanceUntilIdle()

            vm.recompensas.value shouldBe lista
        }
    }
})
