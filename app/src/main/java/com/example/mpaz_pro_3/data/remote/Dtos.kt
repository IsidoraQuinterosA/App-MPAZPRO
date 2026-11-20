package com.example.mpaz_pro_3.data.remote

import com.google.gson.annotations.SerializedName

// Clases que representan el JSON que envían y reciben las APIs (DTO = objeto de transferencia)

// ---------- ms-contenidos ----------
data class ObjetivoDto(
    val id: Int,
    val asignatura: String,
    val unidad: String,
    val codigo: String?,
    val descripcion: String,
    val estado: String,
    val observacion: String?,
    val asignado: Boolean,
    val maxIntentos: Int,
    val informeValidado: Boolean,
    val fechaValidacion: Long?
)

data class PreguntaDto(
    val id: Int?,              // null al crear: el servidor asigna el id
    val objetivoId: Int,
    val tipo: String,          // DESAFIO o PRUEBA
    val nivel: Int,
    val enunciado: String,
    val opcionA: String,
    val opcionB: String,
    val opcionC: String,
    val correcta: Int
)

// ---------- ms-resultados ----------
data class RespuestaDto(
    val id: Long?,
    val preguntaId: Int,
    val elegida: Int,
    val esCorrecta: Boolean
)

data class IntentoDto(
    val id: Long?,
    val rutEstudiante: String,
    val objetivoId: Int,
    val correctas: Int,
    val total: Int,
    val fecha: Long?,
    val respuestas: List<RespuestaDto>
)

// ---------- PokeAPI (solo los campos que usa la app) ----------
data class PokemonDto(
    val id: Int,
    val name: String,
    val sprites: SpritesDto,
    val types: List<TipoSlotDto>
)

data class SpritesDto(
    @SerializedName("front_default") val frontDefault: String?,
    val other: OtrosSpritesDto?
)

data class OtrosSpritesDto(
    @SerializedName("official-artwork") val officialArtwork: ArtworkDto?
)

data class ArtworkDto(
    @SerializedName("front_default") val frontDefault: String?
)

data class TipoSlotDto(val type: TipoDto)

data class TipoDto(val name: String)
