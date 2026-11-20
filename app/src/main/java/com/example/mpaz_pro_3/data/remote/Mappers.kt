package com.example.mpaz_pro_3.data.remote

import com.example.mpaz_pro_3.data.local.Intento
import com.example.mpaz_pro_3.data.local.ObjetivoAprendizaje
import com.example.mpaz_pro_3.data.local.Pregunta
import com.example.mpaz_pro_3.data.local.Respuesta

// Conversión entre las clases de Room (local) y las del servidor (DTO)

fun ObjetivoDto.aEntidad() = ObjetivoAprendizaje(
    id = id,
    asignatura = asignatura,
    unidad = unidad,
    codigo = codigo ?: "",
    descripcion = descripcion,
    estado = estado,
    observacion = observacion ?: "",
    asignado = asignado,
    maxIntentos = maxIntentos,
    informeValidado = informeValidado,
    fechaValidacion = fechaValidacion ?: 0L
)

fun ObjetivoAprendizaje.aDto() = ObjetivoDto(
    id = id,
    asignatura = asignatura,
    unidad = unidad,
    codigo = codigo,
    descripcion = descripcion,
    estado = estado,
    observacion = observacion,
    asignado = asignado,
    maxIntentos = maxIntentos,
    informeValidado = informeValidado,
    fechaValidacion = fechaValidacion
)

fun PreguntaDto.aEntidad() = Pregunta(
    id = id ?: 0,
    objetivoId = objetivoId,
    tipo = tipo,
    nivel = nivel,
    enunciado = enunciado,
    opcionA = opcionA,
    opcionB = opcionB,
    opcionC = opcionC,
    correcta = correcta
)

fun Pregunta.aDto() = PreguntaDto(
    id = if (id == 0) null else id,
    objetivoId = objetivoId,
    tipo = tipo,
    nivel = nivel,
    enunciado = enunciado,
    opcionA = opcionA,
    opcionB = opcionB,
    opcionC = opcionC,
    correcta = correcta
)

fun IntentoDto.aEntidad() = Intento(
    id = id ?: 0,
    rutEstudiante = rutEstudiante,
    objetivoId = objetivoId,
    correctas = correctas,
    total = total,
    fecha = fecha ?: System.currentTimeMillis()
)

fun RespuestaDto.aEntidad(intentoId: Long) = Respuesta(
    id = id ?: 0,
    intentoId = intentoId,
    preguntaId = preguntaId,
    elegida = elegida,
    esCorrecta = esCorrecta
)
