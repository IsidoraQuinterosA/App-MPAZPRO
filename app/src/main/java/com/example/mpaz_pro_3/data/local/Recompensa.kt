package com.example.mpaz_pro_3.data.local

import androidx.room.Entity

// Tabla "recompensas": el Pokémon que gana el estudiante al superar los desafíos de una unidad.
// Los datos vienen de la API externa PokeAPI y se guardan en el teléfono.
@Entity(tableName = "recompensas", primaryKeys = ["rutEstudiante", "objetivoId"])
data class Recompensa(
    val rutEstudiante: String,
    val objetivoId: Int,
    val pokemonId: Int,
    val nombre: String,
    val imagen: String,        // URL de la imagen oficial del Pokémon
    val tipos: String,         // Por ejemplo "Eléctrico" o "Planta, Veneno"
    val fecha: Long = System.currentTimeMillis()
)
