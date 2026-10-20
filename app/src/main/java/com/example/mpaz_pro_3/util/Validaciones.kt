package com.example.mpaz_pro_3.util

// Funciones de validación. Devuelven el mensaje de error, o null si el dato está correcto.
object Validaciones {

    // Deja el RUT como "25418732-1" aunque se escriba con puntos o sin guion
    fun normalizarRut(rut: String): String {
        val limpio = rut.replace(".", "").replace("-", "").trim().uppercase()
        if (limpio.length < 2) return limpio
        return limpio.dropLast(1) + "-" + limpio.last()
    }

    // Calcula el dígito verificador con el método módulo 11
    private fun calcularDv(numero: String): Char {
        var suma = 0
        var multiplicador = 2
        for (i in numero.length - 1 downTo 0) {
            suma += numero[i].digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        return when (val resto = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
    }

    fun rutEsValido(rut: String): Boolean {
        val normalizado = normalizarRut(rut)
        if (!Regex("^\\d{7,8}-[0-9K]$").matches(normalizado)) return false
        val numero = normalizado.substringBefore("-")
        return calcularDv(numero) == normalizado.last()
    }

    fun validarRut(rut: String): String? = when {
        rut.isBlank() -> "El RUT es requerido"
        !rutEsValido(rut) -> "El RUT no es válido"
        else -> null
    }

    fun validarPin(pin: String): String? = when {
        pin.isBlank() -> "El PIN es requerido"
        pin.length != 4 -> "El PIN debe tener 4 números"
        !esFechaValida(pin) -> "El PIN es tu día y mes de nacimiento (ej: 0703)"
        else -> null
    }

    fun validarClave(clave: String): String? = when {
        clave.isBlank() -> "La contraseña es requerida"
        clave.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }

    // Revisa que "DDMM" sea una fecha que exista
    private fun esFechaValida(pin: String): Boolean {
        val dia = pin.substring(0, 2).toIntOrNull() ?: return false
        val mes = pin.substring(2, 4).toIntOrNull() ?: return false
        val diasDelMes = when (mes) {
            2 -> 29
            4, 6, 9, 11 -> 30
            in 1..12 -> 31
            else -> return false
        }
        return dia in 1..diasDelMes
    }
}
