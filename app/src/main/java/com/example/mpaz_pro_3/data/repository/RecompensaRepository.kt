package com.example.mpaz_pro_3.data.repository

import com.example.mpaz_pro_3.data.local.Recompensa
import com.example.mpaz_pro_3.data.local.RecompensaDao
import com.example.mpaz_pro_3.data.remote.PokeApi
import com.example.mpaz_pro_3.data.remote.PokemonDto
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

// Recompensas del estudiante con la API externa PokeAPI.
// No se mezcla con los microservicios: solo agrega un premio visual al terminar los desafíos.
class RecompensaRepository(
    private val recompensaDao: RecompensaDao,
    private val api: PokeApi,
    // Elige un Pokémon de la primera generación (1 a 151). Se puede reemplazar en las pruebas.
    private val elegirPokemon: () -> Int = { Random.nextInt(1, 152) }
) {

    fun coleccion(rut: String): Flow<List<Recompensa>> = recompensaDao.obtenerColeccion(rut)

    suspend fun recompensa(rut: String, objetivoId: Int): Recompensa? =
        recompensaDao.obtenerRecompensa(rut, objetivoId)

    // Entrega el Pokémon ganado en este objetivo. Si ya lo había ganado, devuelve el mismo.
    suspend fun ganarRecompensa(rut: String, objetivoId: Int): Recompensa {
        recompensaDao.obtenerRecompensa(rut, objetivoId)?.let { return it }

        val pokemon = api.obtenerPokemon(elegirPokemon())
        val recompensa = aRecompensa(pokemon, rut, objetivoId)
        recompensaDao.guardar(recompensa)
        return recompensa
    }

    companion object {
        // Convierte la respuesta de la PokeAPI en una recompensa
        fun aRecompensa(pokemon: PokemonDto, rut: String, objetivoId: Int) = Recompensa(
            rutEstudiante = rut,
            objetivoId = objetivoId,
            pokemonId = pokemon.id,
            nombre = pokemon.name.replaceFirstChar { it.uppercase() },
            imagen = pokemon.sprites.other?.officialArtwork?.frontDefault
                ?: pokemon.sprites.frontDefault
                ?: "",
            tipos = pokemon.types.joinToString(", ") { traducirTipo(it.type.name) }
        )

        // La PokeAPI entrega los tipos en inglés; se traducen para los niños
        fun traducirTipo(tipo: String): String = when (tipo) {
            "normal" -> "Normal"
            "fire" -> "Fuego"
            "water" -> "Agua"
            "grass" -> "Planta"
            "electric" -> "Eléctrico"
            "ice" -> "Hielo"
            "fighting" -> "Lucha"
            "poison" -> "Veneno"
            "ground" -> "Tierra"
            "flying" -> "Volador"
            "psychic" -> "Psíquico"
            "bug" -> "Bicho"
            "rock" -> "Roca"
            "ghost" -> "Fantasma"
            "dragon" -> "Dragón"
            "fairy" -> "Hada"
            "steel" -> "Acero"
            "dark" -> "Siniestro"
            else -> tipo.replaceFirstChar { it.uppercase() }
        }
    }
}
