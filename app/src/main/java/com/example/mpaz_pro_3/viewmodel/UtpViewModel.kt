package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.data.local.ObjetivoAprendizaje
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class UtpViewModel(application: Application) : AndroidViewModel(application) {

    private val contenidoRepository = (application as MpazApp).contenidoRepository

    // Objetivos con su estado de revisión. Se actualizan solos al aprobar u observar.
    val objetivos: StateFlow<List<ObjetivoAprendizaje>> = contenidoRepository.objetivos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
