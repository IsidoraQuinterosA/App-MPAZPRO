package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ConexionViewModel(application: Application) : AndroidViewModel(application) {

    val conectado: StateFlow<Boolean> = (application as MpazApp).conexionInternet.conectado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
}
