package com.example.mpaz_pro_3.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Crea los clientes de Retrofit. Cada API tiene su propia URL base.
object RetrofitClient {

    // Tiempos cortos para que la app avise rápido si un servicio no responde
    private val http = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private fun crear(urlBase: String): Retrofit = Retrofit.Builder()
        .baseUrl(urlBase)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create()) // Convierte JSON <-> clases Kotlin
        .build()

    val contenidosApi: ContenidosApi by lazy { crear(ApiConfig.URL_CONTENIDOS).create(ContenidosApi::class.java) }

    val resultadosApi: ResultadosApi by lazy { crear(ApiConfig.URL_RESULTADOS).create(ResultadosApi::class.java) }

    val pokeApi: PokeApi by lazy { crear(ApiConfig.URL_POKEAPI).create(PokeApi::class.java) }
}
