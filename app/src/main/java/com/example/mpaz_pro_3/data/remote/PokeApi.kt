package com.example.mpaz_pro_3.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

// API externa: PokeAPI (https://pokeapi.co). Se usa para las recompensas del estudiante.
interface PokeApi {

    @GET("pokemon/{id}")
    suspend fun obtenerPokemon(@Path("id") id: Int): PokemonDto
}
