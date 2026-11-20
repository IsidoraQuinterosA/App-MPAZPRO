package com.example.mpaz_pro_3.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

// Validaciones del formulario de preguntas, la prueba final y el RUT (escritas con Kotest)
class ValidacionesPreguntasTest : DescribeSpec({

    describe("RUT") {
        it("se normaliza con guion y en mayúscula") {
            Validaciones.normalizarRut("25.418.732-1") shouldBe "25418732-1"
            Validaciones.normalizarRut("15842391k") shouldBe "15842391-K"
            Validaciones.normalizarRut("1") shouldBe "1"
        }
        it("valida el dígito verificador y rechaza formatos incorrectos") {
            Validaciones.rutEsValido("11111111-1") shouldBe true
            Validaciones.rutEsValido("abc") shouldBe false
            Validaciones.validarRut("25418732-1").shouldBeNull()
            Validaciones.validarRut("25418732-9") shouldBe "El RUT no es válido"
        }
    }

    describe("PIN") {
        it("acepta el 29 de febrero y rechaza fechas que no existen") {
            Validaciones.validarPin("2902").shouldBeNull()
            Validaciones.validarPin("3104").shouldNotBeNull()
            Validaciones.validarPin("0113").shouldNotBeNull()
            Validaciones.validarPin("0012").shouldNotBeNull()
            Validaciones.validarPin("").shouldNotBeNull()
            Validaciones.validarIntentos("a").shouldNotBeNull()
        }
    }

    describe("enunciado") {
        it("es obligatorio y tiene entre 5 y 150 caracteres") {
            Validaciones.validarEnunciado("").shouldNotBeNull()
            Validaciones.validarEnunciado("abc").shouldNotBeNull()
            Validaciones.validarEnunciado("x".repeat(151)).shouldNotBeNull()
            Validaciones.validarEnunciado("¿Cuánto es 2 + 3?").shouldBeNull()
        }
    }

    describe("alternativas") {
        it("son obligatorias y de máximo 60 caracteres") {
            Validaciones.validarOpcion(" ").shouldNotBeNull()
            Validaciones.validarOpcion("x".repeat(61)).shouldNotBeNull()
            Validaciones.validarOpcion("Cinco").shouldBeNull()
        }
        it("no se pueden repetir aunque cambien mayúsculas o espacios") {
            Validaciones.validarOpcionesDistintas("Perro", " perro ", "Gato").shouldNotBeNull()
            Validaciones.validarOpcionesDistintas("Perro", "Gato", "Loro").shouldBeNull()
            Validaciones.validarOpcionesDistintas("Perro", "", "").shouldBeNull()
        }
        it("se debe marcar una correcta entre A, B y C") {
            Validaciones.validarCorrecta(null).shouldNotBeNull()
            Validaciones.validarCorrecta(3).shouldNotBeNull()
            Validaciones.validarCorrecta(2).shouldBeNull()
        }
    }

    describe("nivel") {
        it("los desafíos van del nivel 1 al 3 y la prueba no usa nivel") {
            Validaciones.validarNivel("DESAFIO", 0).shouldNotBeNull()
            Validaciones.validarNivel("DESAFIO", 2).shouldBeNull()
            Validaciones.validarNivel("PRUEBA", 0).shouldBeNull()
        }
    }

    describe("revisión, informe y prueba") {
        it("la observación de UTP tiene al menos 10 caracteres") {
            Validaciones.validarObservacion("").shouldNotBeNull()
            Validaciones.validarObservacion("corto").shouldNotBeNull()
            Validaciones.validarObservacion("Agregar más ejemplos").shouldBeNull()
        }
        it("el docente confirma que revisó el informe") {
            Validaciones.validarConfirmacion(false).shouldNotBeNull()
            Validaciones.validarConfirmacion(true).shouldBeNull()
        }
        it("cada pregunta de la prueba debe tener respuesta") {
            Validaciones.validarRespuesta(null).shouldNotBeNull()
            Validaciones.validarRespuesta(1).shouldBeNull()
            Validaciones.validarPruebaCompleta(listOf(0, null)).shouldNotBeNull()
            Validaciones.validarPruebaCompleta(listOf(0, 2)).shouldBeNull()
            Validaciones.validarTitulo("").shouldNotBeNull()
        }
    }
})
