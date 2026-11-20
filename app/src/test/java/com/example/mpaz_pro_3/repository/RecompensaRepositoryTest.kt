package com.example.mpaz_pro_3.repository

import com.example.mpaz_pro_3.Datos
import com.example.mpaz_pro_3.data.local.Recompensa
import com.example.mpaz_pro_3.data.local.RecompensaDao
import com.example.mpaz_pro_3.data.remote.ArtworkDto
import com.example.mpaz_pro_3.data.remote.OtrosSpritesDto
import com.example.mpaz_pro_3.data.remote.PokeApi
import com.example.mpaz_pro_3.data.remote.PokemonDto
import com.example.mpaz_pro_3.data.remote.SpritesDto
import com.example.mpaz_pro_3.data.remote.TipoDto
import com.example.mpaz_pro_3.data.remote.TipoSlotDto
import com.example.mpaz_pro_3.data.repository.RecompensaRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class RecompensaRepositoryTest : FunSpec({

    // Respuesta de la PokeAPI para Pikachu (solo los campos que usa la app)
    val pikachu = PokemonDto(
        id = 25,
        name = "pikachu",
        sprites = SpritesDto(
            frontDefault = "https://img/sprite-25.png",
            other = OtrosSpritesDto(ArtworkDto("https://img/artwork-25.png"))
        ),
        types = listOf(TipoSlotDto(TipoDto("electric")))
    )

    test("al ganar se pide un Pokémon a la PokeAPI y se guarda en Room") {
        val dao = mockk<RecompensaDao>(relaxed = true)
        val api = mockk<PokeApi>()
        coEvery { dao.obtenerRecompensa(Datos.RUT, 1) } returns null
        coEvery { api.obtenerPokemon(25) } returns pikachu
        val repo = RecompensaRepository(dao, api, elegirPokemon = { 25 })

        val recompensa = repo.ganarRecompensa(Datos.RUT, 1)

        recompensa.nombre shouldBe "Pikachu"
        recompensa.pokemonId shouldBe 25
        recompensa.imagen shouldBe "https://img/artwork-25.png"
        recompensa.tipos shouldBe "Eléctrico"
        coVerify { dao.guardar(recompensa) }
    }

    test("si ya ganó un Pokémon en ese objetivo, devuelve el mismo sin llamar a la API") {
        val dao = mockk<RecompensaDao>(relaxed = true)
        val api = mockk<PokeApi>()
        val guardada = Recompensa(Datos.RUT, 1, 1, "Bulbasaur", "img", "Planta, Veneno", 0L)
        coEvery { dao.obtenerRecompensa(Datos.RUT, 1) } returns guardada
        val repo = RecompensaRepository(dao, api)

        repo.ganarRecompensa(Datos.RUT, 1) shouldBe guardada
        repo.recompensa(Datos.RUT, 1) shouldBe guardada
        coVerify(exactly = 0) { api.obtenerPokemon(any()) }
    }

    test("la colección se lee desde Room") {
        val dao = mockk<RecompensaDao>(relaxed = true)
        val lista = listOf(Recompensa(Datos.RUT, 1, 4, "Charmander", "img", "Fuego", 0L))
        every { dao.obtenerColeccion(Datos.RUT) } returns flowOf(lista)

        RecompensaRepository(dao, mockk()).coleccion(Datos.RUT).first() shouldBe lista
    }

    test("el número elegido por defecto está entre 1 y 151") {
        val dao = mockk<RecompensaDao>(relaxed = true)
        val api = mockk<PokeApi>()
        coEvery { dao.obtenerRecompensa(any(), any()) } returns null
        coEvery { api.obtenerPokemon(any()) } returns pikachu
        RecompensaRepository(dao, api).ganarRecompensa(Datos.RUT, 2)
        coVerify { api.obtenerPokemon(match { it in 1..151 }) }
    }

    test("si no hay imagen oficial usa el sprite, y si no hay ninguna queda vacía") {
        val sinArtwork = pikachu.copy(sprites = SpritesDto("https://img/sprite-25.png", null))
        RecompensaRepository.aRecompensa(sinArtwork, Datos.RUT, 1).imagen shouldBe "https://img/sprite-25.png"

        val sinImagenes = pikachu.copy(sprites = SpritesDto(null, OtrosSpritesDto(null)))
        RecompensaRepository.aRecompensa(sinImagenes, Datos.RUT, 1).imagen shouldBe ""
    }

    test("varios tipos se muestran separados por coma") {
        val bulbasaur = pikachu.copy(name = "bulbasaur", types = listOf(TipoSlotDto(TipoDto("grass")), TipoSlotDto(TipoDto("poison"))))
        RecompensaRepository.aRecompensa(bulbasaur, Datos.RUT, 1).tipos shouldBe "Planta, Veneno"
    }

    test("traduce al español todos los tipos de la PokeAPI") {
        val traducciones = mapOf(
            "normal" to "Normal", "fire" to "Fuego", "water" to "Agua", "grass" to "Planta",
            "electric" to "Eléctrico", "ice" to "Hielo", "fighting" to "Lucha", "poison" to "Veneno",
            "ground" to "Tierra", "flying" to "Volador", "psychic" to "Psíquico", "bug" to "Bicho",
            "rock" to "Roca", "ghost" to "Fantasma", "dragon" to "Dragón", "fairy" to "Hada",
            "steel" to "Acero", "dark" to "Siniestro", "shadow" to "Shadow"
        )
        traducciones.forEach { (ingles, espanol) ->
            RecompensaRepository.traducirTipo(ingles) shouldBe espanol
        }
    }
})
