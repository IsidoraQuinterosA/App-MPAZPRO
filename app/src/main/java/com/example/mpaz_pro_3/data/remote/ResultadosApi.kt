package com.example.mpaz_pro_3.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints del microservicio ms-resultados (Spring Boot, puerto 8082)
interface ResultadosApi {

    @GET("api/intentos")
    suspend fun obtenerIntentos(
        @Query("objetivoId") objetivoId: Int? = null,
        @Query("rut") rut: String? = null
    ): List<IntentoDto>

    @POST("api/intentos")
    suspend fun registrarIntento(@Body intento: IntentoDto): IntentoDto

    @DELETE("api/intentos/{id}")
    suspend fun eliminarIntento(@Path("id") id: Long)
}
