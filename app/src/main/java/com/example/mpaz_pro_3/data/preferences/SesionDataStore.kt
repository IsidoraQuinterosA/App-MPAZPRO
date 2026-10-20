package com.example.mpaz_pro_3.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mpaz_pro_3.model.Rol
import com.example.mpaz_pro_3.model.Sesion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion")

private val CLAVE_RUT = stringPreferencesKey("rut")
private val CLAVE_NOMBRE = stringPreferencesKey("nombre")
private val CLAVE_ROL = stringPreferencesKey("rol")

// Guarda la sesión iniciada para no pedir el RUT cada vez que se abre la app
class SesionDataStore(private val context: Context) {

    val sesion: Flow<Sesion?> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            val rut = prefs[CLAVE_RUT]
            val nombre = prefs[CLAVE_NOMBRE]
            val rol = prefs[CLAVE_ROL]
            if (rut != null && nombre != null && rol != null) {
                Sesion(rut, nombre, Rol.valueOf(rol))
            } else {
                null
            }
        }

    suspend fun guardar(sesion: Sesion) {
        context.dataStore.edit { prefs ->
            prefs[CLAVE_RUT] = sesion.rut
            prefs[CLAVE_NOMBRE] = sesion.nombre
            prefs[CLAVE_ROL] = sesion.rol.name
        }
    }

    suspend fun borrar() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }
}
