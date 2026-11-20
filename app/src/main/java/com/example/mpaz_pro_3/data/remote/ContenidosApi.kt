package com.example.mpaz_pro_3.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints del microservicio ms-contenidos (Spring Boot, puerto 8081)
interface ContenidosApi {

    @GET("api/objetivos")
    suspend fun obtenerObjetivos(): List<ObjetivoDto>

    @PUT("api/objetivos/{id}")
    suspend fun actualizarObjetivo(@Path("id") id: Int, @Body objetivo: ObjetivoDto): ObjetivoDto

    @GET("api/preguntas")
    suspend fun obtenerPreguntas(@Query("objetivoId") objetivoId: Int? = null): List<PreguntaDto>

    @POST("api/preguntas")
    suspend fun crearPregunta(@Body pregunta: PreguntaDto): PreguntaDto

    @PUT("api/preguntas/{id}")
    suspend fun actualizarPregunta(@Path("id") id: Int, @Body pregunta: PreguntaDto): PreguntaDto

    @DELETE("api/preguntas/{id}")
    suspend fun eliminarPregunta(@Path("id") id: Int)
}
