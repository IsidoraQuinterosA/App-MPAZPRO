package com.example.mpaz_pro_3.remote

import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.data.remote.IntentoDto
import com.example.mpaz_pro_3.data.remote.ObjetivoDto
import com.example.mpaz_pro_3.data.remote.PreguntaDto
import com.example.mpaz_pro_3.data.remote.RespuestaDto
import com.example.mpaz_pro_3.data.remote.aDto
import com.example.mpaz_pro_3.data.remote.aEntidad
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

// Conversión entre los DTO del servidor y las tablas de Room
class MappersTest : FunSpec({

    test("un objetivo con campos vacíos del servidor queda con valores por defecto") {
        val dto = ObjetivoDto(
            id = 2, asignatura = "MATEMATICA", unidad = "Unidad 2", codigo = null,
            descripcion = "Sumar", estado = "PENDIENTE", observacion = null, asignado = false,
            maxIntentos = 3, informeValidado = false, fechaValidacion = null
        )
        val entidad = dto.aEntidad()
        entidad.codigo shouldBe ""
        entidad.observacion shouldBe ""
        entidad.fechaValidacion shouldBe 0L
        entidad.maxIntentos shouldBe 3
    }

    test("objetivo: ida y vuelta conserva los datos") {
        val objetivo = Datos.objetivo().copy(informeValidado = true, fechaValidacion = 123L)
        objetivo.aDto().aEntidad() shouldBe objetivo
    }

    test("una pregunta nueva (id 0) se envía sin id para que el servidor lo asigne") {
        Datos.pregunta(0).aDto().id.shouldBeNull()
        Datos.pregunta(5).aDto().id shouldBe 5
    }

    test("una pregunta sin id del servidor queda con id 0 en Room") {
        val dto = PreguntaDto(null, 1, "PRUEBA", 0, "¿Cuánto es 2 + 2?", "3", "4", "5", 1)
        val entidad = dto.aEntidad()
        entidad.id shouldBe 0
        entidad.correcta shouldBe 1
        entidad.tipo shouldBe "PRUEBA"
    }

    test("intento y respuesta del servidor se convierten a Room") {
        val respuesta = RespuestaDto(id = 4, preguntaId = 10, elegida = 2, esCorrecta = false)
        val dto = IntentoDto(9, Datos.RUT, 1, 0, 1, 500L, listOf(respuesta))

        val intento = dto.aEntidad()
        intento.id shouldBe 9L
        intento.fecha shouldBe 500L

        val entidad = respuesta.aEntidad(intentoId = 9)
        entidad.intentoId shouldBe 9L
        entidad.elegida shouldBe 2
        entidad.esCorrecta shouldBe false
    }

    test("datos sin id ni fecha usan valores por defecto") {
        val dto = IntentoDto(null, Datos.RUT, 1, 1, 1, null, emptyList())
        dto.aEntidad().id shouldBe 0L
        dto.aEntidad().fecha shouldBeGreaterThan 0L
        RespuestaDto(null, 10, 0, true).aEntidad(1).id shouldBe 0L
    }
})
