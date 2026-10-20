package com.example.mpaz_pro_3.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.R
import com.example.mpaz_pro_3.model.Rol
import com.example.mpaz_pro_3.ui.components.BotonCarga
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.ErrorFondo
import com.example.mpaz_pro_3.ui.theme.ErrorRojo
import com.example.mpaz_pro_3.ui.theme.Fondo
import com.example.mpaz_pro_3.ui.theme.Petroleo
import com.example.mpaz_pro_3.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    rol: Rol,
    onVolver: () -> Unit,
    onIngresoExitoso: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val foco = LocalFocusManager.current
    val esEstudiante = rol == Rol.ESTUDIANTE

    // Cuando el ingreso es correcto, se navega al panel
    LaunchedEffect(state.ingresoExitoso) {
        if (state.ingresoExitoso) {
            viewModel.onIngresoNavegado()
            onIngresoExitoso()
        }
    }

    Scaffold(
        containerColor = Fondo,
        topBar = {
            TopAppBar(
                title = { Text("Ingreso ${rol.titulo.lowercase()}") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Petroleo,
                    titleContentColor = Blanco,
                    navigationIconContentColor = Blanco
                )
            )
        }
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // widthIn: en tablets el formulario no se estira de lado a lado
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_icono),
                    contentDescription = null,
                    modifier = Modifier
                        .size(88.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Text(
                    text = if (esEstudiante) "Escribe tu RUT y tu PIN" else "Escribe tu RUT y tu contraseña",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Campo RUT
                OutlinedTextField(
                    value = state.rut,
                    onValueChange = viewModel::onRutChanged,
                    label = { Text("RUT") },
                    placeholder = { Text("12345678-9") },
                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                    trailingIcon = {
                        if (state.errores.rut != null) {
                            Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", tint = ErrorRojo)
                        }
                    },
                    isError = state.errores.rut != null,
                    supportingText = { state.errores.rut?.let { Text(it) } },
                    singleLine = true,
                    enabled = !state.cargando,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo PIN (estudiante) o contraseña (docente y UTP)
                OutlinedTextField(
                    value = state.clave,
                    onValueChange = { viewModel.onClaveChanged(it, rol) },
                    label = { Text(if (esEstudiante) "PIN" else "Contraseña") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = viewModel::onMostrarClaveChanged) {
                            Icon(
                                imageVector = if (state.mostrarClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (state.mostrarClave) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    isError = state.errores.clave != null,
                    supportingText = {
                        val ayuda = if (esEstudiante) "Tu día y mes de nacimiento. Ejemplo: 0703" else ""
                        Text(state.errores.clave ?: ayuda)
                    },
                    visualTransformation = if (state.mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !state.cargando,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (esEstudiante) KeyboardType.NumberPassword else KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        foco.clearFocus()
                        viewModel.ingresar(rol)
                    }),
                    modifier = Modifier.fillMaxWidth()
                )

                // Mensaje de error general con animación de aparición
                AnimatedVisibility(visible = state.mensajeError != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = ErrorFondo)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = ErrorRojo)
                            Spacer(Modifier.width(8.dp))
                            Text(state.mensajeError ?: "", color = ErrorRojo)
                        }
                    }
                }

                BotonCarga(
                    texto = "Entrar",
                    cargando = state.cargando,
                    icono = Icons.AutoMirrored.Filled.Login,
                    onClick = {
                        foco.clearFocus()
                        viewModel.ingresar(rol)
                    }
                )
            }
        }
    }
}
