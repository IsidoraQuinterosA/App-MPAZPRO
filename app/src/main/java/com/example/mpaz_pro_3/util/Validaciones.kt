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

    // Docente: el objetivo debe estar elegido y aprobado por UTP
    fun validarObjetivo(estado: String?): String? = when (estado) {
        null -> "Selecciona un objetivo"
        "APROBADO" -> null
        else -> "Este objetivo aún no está aprobado por UTP"
    }

    fun validarIntentos(texto: String): String? {
        val numero = texto.toIntOrNull()
        return when {
            texto.isBlank() -> "Ingresa la cantidad de intentos"
            numero == null -> "Debe ser un número"
            numero !in 1..5 -> "Debe ser entre 1 y 5"
            else -> null
        }
    }

    // Docente: formulario para subir guías
    const val TAMANO_MAXIMO = 10 * 1024 * 1024L  // 10 MB

    fun validarTitulo(titulo: String): String? = when {
        titulo.isBlank() -> "Escribe un título"
        titulo.trim().length < 3 -> "El título debe tener al menos 3 caracteres"
        else -> null
    }

    fun validarArchivo(nombre: String?, tamano: Long): String? = when {
        nombre == null -> "Elige un archivo"
        tamano > TAMANO_MAXIMO -> "El archivo no puede superar 10 MB"
        else -> null
    }

    // Prueba final: cada pregunta debe tener una alternativa marcada
    fun validarRespuesta(elegida: Int?): String? =
        if (elegida == null) "Elige una respuesta para continuar" else null

    fun validarPruebaCompleta(elegidas: List<Int?>): String? =
        if (elegidas.any { it == null }) "Responde todas las preguntas antes de entregar" else null

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
