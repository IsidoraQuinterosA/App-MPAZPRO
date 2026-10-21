package com.example.mpaz_pro_3.data.local

// Datos ficticios que se cargan la primera vez que se abre la app (el caso pide datos ficticios).
// Los objetivos y preguntas son ejemplos propios, no copiados de textos escolares.
object DatosIniciales {

    val usuarios = listOf(
        Usuario("25418732-1", "Martina Rojas", "ESTUDIANTE", "0703"),
        Usuario("25602159-5", "Benjamín Soto", "ESTUDIANTE", "1511"),
        Usuario("15842391-K", "Carolina Fuentes", "DOCENTE", "docente123"),
        Usuario("13276054-3", "Rodrigo Pérez", "UTP", "utp12345")
    )

    // Lenguaje y Matemática ya están aprobados por UTP y asignados por el docente.
    // Ciencias e Historia quedan pendientes para poder probar la revisión de UTP.
    val objetivos = listOf(
        ObjetivoAprendizaje(1, "LENGUAJE", "Unidad 1: El mundo de los cuentos", "Por definir",
            "Reconocer personajes, lugares y acciones de un cuento", "APROBADO", asignado = true),
        ObjetivoAprendizaje(2, "MATEMATICA", "Unidad 1: Números hasta el 1.000", "Por definir",
            "Sumar y restar números hasta el 1.000", "APROBADO", asignado = true),
        ObjetivoAprendizaje(3, "CIENCIAS", "Unidad 1: Las plantas", "Por definir",
            "Reconocer qué necesitan las plantas para vivir"),
        ObjetivoAprendizaje(4, "HISTORIA", "Unidad 1: Ubicándonos en el mapa", "Por definir",
            "Usar los puntos cardinales para ubicar lugares")
    )

    val contenidos = listOf(
        Contenido(objetivoId = 1, titulo = "¿Qué tiene un cuento?",
            explicacion = "Un cuento tiene personajes (quiénes participan), un lugar (dónde ocurre) y acciones (qué pasa).",
            ejemplo = "Ana corrió al parque. Ana es el personaje, el parque es el lugar y correr es la acción."),
        Contenido(objetivoId = 2, titulo = "Sumar y restar",
            explicacion = "Para sumar juntamos cantidades y para restar quitamos. Primero calculamos las unidades, luego las decenas y al final las centenas.",
            ejemplo = "124 + 35 = 159. Unidades: 4 + 5 = 9. Decenas: 2 + 3 = 5. Centenas: 1."),
        Contenido(objetivoId = 3, titulo = "Las plantas son seres vivos",
            explicacion = "Las plantas nacen, crecen y se reproducen. Para vivir necesitan agua, luz del sol, aire y un suelo con nutrientes.",
            ejemplo = "Si dejamos una planta sin luz, sus hojas se ponen amarillas."),
        Contenido(objetivoId = 4, titulo = "Los puntos cardinales",
            explicacion = "Los puntos cardinales son cuatro: norte, sur, este y oeste. El sol sale por el este y se pone por el oeste.",
            ejemplo = "En la mayoría de los mapas, el norte está arriba y el sur está abajo.")
    )

    val preguntas = listOf(
        // Lenguaje
        Pregunta(objetivoId = 1, tipo = "DESAFIO", nivel = 1, enunciado = "¿Quién puede ser un personaje de un cuento?",
            opcionA = "El perro Tito", opcionB = "Una mesa", opcionC = "El lunes", correcta = 0),
        Pregunta(objetivoId = 1, tipo = "DESAFIO", nivel = 2, enunciado = "Ana corrió al parque. ¿Dónde ocurre?",
            opcionA = "En la casa", opcionB = "En el parque", opcionC = "En la escuela", correcta = 1),
        Pregunta(objetivoId = 1, tipo = "DESAFIO", nivel = 3, enunciado = "Ana buscó su pelota en el parque. ¿Qué hizo Ana?",
            opcionA = "Durmió", opcionB = "Cantó", opcionC = "Buscó su pelota", correcta = 2),
        Pregunta(objetivoId = 1, tipo = "PRUEBA", nivel = 0, enunciado = "¿Qué palabra nombra un lugar?",
            opcionA = "bosque", opcionB = "saltar", opcionC = "feliz", correcta = 0),
        Pregunta(objetivoId = 1, tipo = "PRUEBA", nivel = 0, enunciado = "El gato Lolo subió al techo. ¿Quién es el personaje?",
            opcionA = "El techo", opcionB = "El gato Lolo", opcionC = "Subió", correcta = 1),
        Pregunta(objetivoId = 1, tipo = "PRUEBA", nivel = 0, enunciado = "Pedro plantó un árbol. ¿Qué hizo Pedro?",
            opcionA = "Comió", opcionB = "Plantó un árbol", opcionC = "Nadó", correcta = 1),

        // Matemática
        Pregunta(objetivoId = 2, tipo = "DESAFIO", nivel = 1, enunciado = "¿Cuánto es 7 + 5?",
            opcionA = "10", opcionB = "12", opcionC = "13", correcta = 1),
        Pregunta(objetivoId = 2, tipo = "DESAFIO", nivel = 2, enunciado = "¿Cuánto es 40 + 30?",
            opcionA = "70", opcionB = "60", opcionC = "700", correcta = 0),
        Pregunta(objetivoId = 2, tipo = "DESAFIO", nivel = 3, enunciado = "¿Cuánto es 124 + 35?",
            opcionA = "149", opcionB = "159", opcionC = "169", correcta = 1),
        Pregunta(objetivoId = 2, tipo = "PRUEBA", nivel = 0, enunciado = "¿Cuánto es 15 - 6?",
            opcionA = "9", opcionB = "11", opcionC = "8", correcta = 0),
        Pregunta(objetivoId = 2, tipo = "PRUEBA", nivel = 0, enunciado = "¿Cuánto es 200 + 300?",
            opcionA = "400", opcionB = "500", opcionC = "600", correcta = 1),
        Pregunta(objetivoId = 2, tipo = "PRUEBA", nivel = 0, enunciado = "¿Cuánto es 350 - 120?",
            opcionA = "230", opcionB = "220", opcionC = "130", correcta = 0),

        // Ciencias Naturales
        Pregunta(objetivoId = 3, tipo = "DESAFIO", nivel = 1, enunciado = "¿Las plantas son seres vivos?",
            opcionA = "Sí", opcionB = "No", opcionC = "Solo en verano", correcta = 0),
        Pregunta(objetivoId = 3, tipo = "DESAFIO", nivel = 2, enunciado = "¿Qué necesita una planta para vivir?",
            opcionA = "Agua y luz", opcionB = "Solo arena", opcionC = "Plástico", correcta = 0),
        Pregunta(objetivoId = 3, tipo = "DESAFIO", nivel = 3, enunciado = "¿Por dónde absorbe agua una planta?",
            opcionA = "Por las hojas", opcionB = "Por la raíz", opcionC = "Por los frutos", correcta = 1),
        Pregunta(objetivoId = 3, tipo = "PRUEBA", nivel = 0, enunciado = "¿Qué parte de la planta está bajo la tierra?",
            opcionA = "La flor", opcionB = "La raíz", opcionC = "La hoja", correcta = 1),
        Pregunta(objetivoId = 3, tipo = "PRUEBA", nivel = 0, enunciado = "¿Qué le pasa a una planta sin luz?",
            opcionA = "Crece más rápido", opcionB = "Se debilita", opcionC = "Da más flores", correcta = 1),
        Pregunta(objetivoId = 3, tipo = "PRUEBA", nivel = 0, enunciado = "¿Qué parte de la planta forma las semillas?",
            opcionA = "La flor", opcionB = "La raíz", opcionC = "El tallo", correcta = 0),

        // Historia
        Pregunta(objetivoId = 4, tipo = "DESAFIO", nivel = 1, enunciado = "¿Cuántos puntos cardinales hay?",
            opcionA = "Dos", opcionB = "Cuatro", opcionC = "Seis", correcta = 1),
        Pregunta(objetivoId = 4, tipo = "DESAFIO", nivel = 2, enunciado = "¿Por dónde sale el sol?",
            opcionA = "Por el este", opcionB = "Por el oeste", opcionC = "Por el sur", correcta = 0),
        Pregunta(objetivoId = 4, tipo = "DESAFIO", nivel = 3, enunciado = "En la mayoría de los mapas, ¿dónde está el norte?",
            opcionA = "Abajo", opcionB = "Arriba", opcionC = "A la izquierda", correcta = 1),
        Pregunta(objetivoId = 4, tipo = "PRUEBA", nivel = 0, enunciado = "¿Por dónde se pone el sol?",
            opcionA = "Por el oeste", opcionB = "Por el norte", opcionC = "Por el este", correcta = 0),
        Pregunta(objetivoId = 4, tipo = "PRUEBA", nivel = 0, enunciado = "¿Cuál de estos es un punto cardinal?",
            opcionA = "Derecha", opcionB = "Sur", opcionC = "Adelante", correcta = 1),
        Pregunta(objetivoId = 4, tipo = "PRUEBA", nivel = 0, enunciado = "Si el norte está arriba, ¿dónde está el sur?",
            opcionA = "A la derecha", opcionB = "Arriba", opcionC = "Abajo", correcta = 2)
    )
}
