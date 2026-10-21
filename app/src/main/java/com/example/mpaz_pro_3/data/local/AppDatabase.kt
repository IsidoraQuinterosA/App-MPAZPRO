package com.example.mpaz_pro_3.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Usuario::class,
        ObjetivoAprendizaje::class,
        Contenido::class,
        Pregunta::class,
        Intento::class,
        Respuesta::class,
        Progreso::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun contenidoDao(): ContenidoDao
    abstract fun progresoDao(): ProgresoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpaz_database"
                )
                    // Si cambia la versión, se borra la base y se vuelven a cargar los datos iniciales
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
