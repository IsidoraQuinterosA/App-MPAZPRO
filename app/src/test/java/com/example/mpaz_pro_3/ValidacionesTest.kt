package com.example.mpaz_pro_3

import com.example.mpaz_pro_3.util.Validaciones
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de las reglas de validación (se ejecutan sin teléfono: clic derecho > Run)
class ValidacionesTest {

    @Test
    fun rutValido() {
        assertTrue(Validaciones.rutEsValido("25.418.732-1"))
        assertTrue(Validaciones.rutEsValido("15842391-k"))
    }

    @Test
    fun rutInvalido() {
        assertFalse(Validaciones.rutEsValido("25.418.732-2"))
        assertNotNull(Validaciones.validarRut(""))
    }

    @Test
    fun pinValido() {
        assertNull(Validaciones.validarPin("0703"))
        assertNotNull(Validaciones.validarPin("3102")) // 31 de febrero no existe
        assertNotNull(Validaciones.validarPin("12"))
    }

    @Test
    fun claveValida() {
        assertNull(Validaciones.validarClave("docente123"))
        assertNotNull(Validaciones.validarClave("123"))
    }

    @Test
    fun intentosEntreUnoYCinco() {
        assertNull(Validaciones.validarIntentos("3"))
        assertNotNull(Validaciones.validarIntentos(""))
        assertNotNull(Validaciones.validarIntentos("0"))
        assertNotNull(Validaciones.validarIntentos("6"))
    }

    @Test
    fun objetivoDebeEstarAprobado() {
        assertNull(Validaciones.validarObjetivo("APROBADO"))
        assertNotNull(Validaciones.validarObjetivo("PENDIENTE"))
        assertNotNull(Validaciones.validarObjetivo(null))
    }

    @Test
    fun guiaConTituloYArchivo() {
        assertNotNull(Validaciones.validarTitulo("ab"))
        assertNull(Validaciones.validarTitulo("Guía de sumas"))
        assertNotNull(Validaciones.validarArchivo(null, 0))
        assertNotNull(Validaciones.validarArchivo("guia.pdf", 20L * 1024 * 1024))
        assertNull(Validaciones.validarArchivo("guia.pdf", 1024))
    }

    @Test
    fun observacionObligatoria() {
        assertNotNull(Validaciones.validarObservacion(""))
        assertNotNull(Validaciones.validarObservacion("corto"))
        assertNull(Validaciones.validarObservacion("Revisar la pregunta 2 del nivel 3"))
    }

    @Test
    fun pruebaCompleta() {
        assertNotNull(Validaciones.validarPruebaCompleta(listOf(0, null, 1)))
        assertNull(Validaciones.validarPruebaCompleta(listOf(0, 2, 1)))
    }
}
