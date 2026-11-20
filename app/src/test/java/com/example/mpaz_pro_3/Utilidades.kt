package com.example.mpaz_pro_3

import com.example.mpaz_pro_3.data.local.ObjetivoAprendizaje
import com.example.mpaz_pro_3.data.local.Pregunta
import com.example.mpaz_pro_3.data.repository.ContenidoRepository
import com.example.mpaz_pro_3.data.repository.GuiaRepository
import com.example.mpaz_pro_3.data.repository.ProgresoRepository
import com.example.mpaz_pro_3.data.repository.RecompensaRepository
import com.example.mpaz_pro_3.data.repository.UsuarioRepository
import com.example.mpaz_pro_3.model.Rol
import com.example.mpaz_pro_3.model.Sesion
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

// Datos de ejemplo que se repiten en varias pruebas
object Datos {
    const val RUT = "25418732-1"
    val sesionEstudiante = Sesion(RUT, "Ana Pérez", Rol.ESTUDIANTE)
    val sesionDocente = Sesion("12345678-5", "Marcela Rojas", Rol.DOCENTE)

    fun objetivo(
        id: Int = 1,
        estado: String = "APROBADO",
        asignado: Boolean = true,
        maxIntentos: Int = 2
    ) = ObjetivoAprendizaje(
        id = id,
        asignatura = "LENGUAJE",
        unidad = "Unidad 1",
        codigo = "OA 1",
        descripcion = "Leer y comprender textos",
        estado = estado,
        asignado = asignado,
        maxIntentos = maxIntentos
    )

    fun pregunta(
        id: Int,
        objetivoId: Int = 1,
        tipo: String = "DESAFIO",
        nivel: Int = 1,
        correcta: Int = 0
    ) = Pregunta(
        id = id,
        objetivoId = objetivoId,
        tipo = tipo,
        nivel = nivel,
        enunciado = "Pregunta $id",
        opcionA = "A$id",
        opcionB = "B$id",
        opcionC = "C$id",
        correcta = correcta
    )
}

// Aplicación falsa: entrega repositorios simulados con MockK a los ViewModel
class AppFalsa(sesion: Sesion? = Datos.sesionEstudiante) {
    val usuarioRepository: UsuarioRepository = mockk(relaxed = true)
    val contenidoRepository: ContenidoRepository = mockk(relaxed = true)
    val progresoRepository: ProgresoRepository = mockk(relaxed = true)
    val guiaRepository: GuiaRepository = mockk(relaxed = true)
    val recompensaRepository: RecompensaRepository = mockk(relaxed = true)
    val app: MpazApp = mockk(relaxed = true)

    init {
        every { usuarioRepository.sesion } returns flowOf(sesion)
        every { app.usuarioRepository } returns usuarioRepository
        every { app.contenidoRepository } returns contenidoRepository
        every { app.progresoRepository } returns progresoRepository
        every { app.guiaRepository } returns guiaRepository
        every { app.recompensaRepository } returns recompensaRepository
    }
}

// Ejecuta una prueba con corrutinas reemplazando el hilo principal (Dispatchers.Main).
// Los delay() de los ViewModel se saltan con el tiempo virtual de runTest.
@OptIn(ExperimentalCoroutinesApi::class)
fun conCorrutinas(bloque: suspend TestScope.() -> Unit) {
    val dispatcher = StandardTestDispatcher()
    Dispatchers.setMain(dispatcher)
    try {
        runTest(dispatcher) {
            bloque()
            advanceUntilIdle()
        }
    } finally {
        Dispatchers.resetMain()
    }
}

// Para el código que trabaja en Dispatchers.IO (hilo real): espera hasta que se cumpla la condición
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.esperarHasta(condicion: () -> Boolean) {
    repeat(300) {
        advanceUntilIdle()
        if (condicion()) return
        Thread.sleep(10)
    }
}
