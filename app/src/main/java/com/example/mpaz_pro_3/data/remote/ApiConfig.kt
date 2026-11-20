package com.example.mpaz_pro_3.data.remote

// Direcciones de los servicios que usa la app.
// 10.0.2.2 es "localhost del computador" visto desde el emulador de Android.
// En un teléfono real, cambiar 10.0.2.2 por la IP del computador en la misma red Wi-Fi.
object ApiConfig {
    const val URL_CONTENIDOS = "http://10.0.2.2:8081/"   // Microservicio ms-contenidos
    const val URL_RESULTADOS = "http://10.0.2.2:8082/"   // Microservicio ms-resultados
    const val URL_POKEAPI = "https://pokeapi.co/api/v2/" // API externa
}
