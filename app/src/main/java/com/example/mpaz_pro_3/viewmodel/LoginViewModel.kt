package com.example.mpaz_pro_3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_pro_3.MpazApp
import com.example.mpaz_pro_3.model.LoginErrores
import com.example.mpaz_pro_3.model.LoginUiState
import com.example.mpaz_pro_3.model.Rol
import com.example.mpaz_pro_3.util.Validaciones
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MpazApp).usuarioRepository

    // 1. Estado privado mutable (solo el ViewModel puede modificarlo)
    private val _state = MutableStateFlow(LoginUiState())

    // 2. Estado público de solo lectura. Como vive en el ViewModel,
    //    lo escrito no se pierde al girar la pantalla.
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    // 3. Eventos de la pantalla
    fun onRutChanged(rut: String) {
        _state.update {
            it.copy(rut = rut, errores = it.errores.copy(rut = null), mensajeError = null)
        }
    }

    fun onClaveChanged(clave: String, rol: Rol) {
        // El PIN del estudiante solo acepta números y máximo 4
        val valor = if (rol == Rol.ESTUDIANTE) clave.filter { it.isDigit() }.take(4) else clave
        _state.update {
            it.copy(clave = valor, errores = it.errores.copy(clave = null), mensajeError = null)
        }
    }

    fun onMostrarClaveChanged() {
        _state.update { it.copy(mostrarClave = !it.mostrarClave) }
    }

    // 4. Lógica de validación
    private fun validar(rol: Rol): Boolean {
        val actual = _state.value
        val errores = LoginErrores(
            rut = Validaciones.validarRut(actual.rut),
            clave = if (rol == Rol.ESTUDIANTE) Validaciones.validarPin(actual.clave)
            else Validaciones.validarClave(actual.clave)
        )
        _state.update { it.copy(errores = errores) }
        return errores.rut == null && errores.clave == null
    }

    // 5. Iniciar sesión
    fun ingresar(rol: Rol) {
        if (_state.value.cargando || !validar(rol)) return

        viewModelScope.launch {
            _state.update { it.copy(cargando = true, mensajeError = null) }
            delay(800) // Pausa corta para que se vea la animación de carga

            val rut = Validaciones.normalizarRut(_state.value.rut)
            val sesion = repository.iniciarSesion(rut, _state.value.clave, rol)

            if (sesion != null) {
                _state.update { it.copy(cargando = false, ingresoExitoso = true) }
            } else {
                _state.update {
                    it.copy(cargando = false, mensajeError = "El RUT o la clave no son correctos")
                }
            }
        }
    }

    // Se llama después de navegar, para no volver a navegar al girar la pantalla
    fun onIngresoNavegado() {
        _state.update { it.copy(ingresoExitoso = false) }
    }
}
