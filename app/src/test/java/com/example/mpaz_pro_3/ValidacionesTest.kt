package com.example.mpaz_pro_3

import com.example.mpaz_pro_3.util.Validaciones
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
