package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.data.local.ObjetivoAprendizaje
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class EstudianteViewModel(application: Application) : AndroidViewModel(application) {

    private val contenidoRepository = (application as MpazApp).contenidoRepository

    // Objetivos leídos desde Room. La lista se actualiza sola si cambia la base de datos.
    val objetivos: StateFlow<List<ObjetivoAprendizaje>> = contenidoRepository.objetivos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
