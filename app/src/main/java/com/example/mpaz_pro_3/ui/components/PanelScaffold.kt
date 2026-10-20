package com.example.mpaz_pro_3.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.mpaz_pro_3.ui.theme.Amarillo
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.Fondo
import com.example.mpaz_pro_3.ui.theme.Petroleo

// Estructura común de los paneles: barra superior con botón para salir
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelScaffold(
    titulo: String,
    subtitulo: String,
    cerrando: Boolean,
    onCerrarSesion: () -> Unit,
    snackbarHostState: SnackbarHostState,
    contenido: @Composable (PaddingValues) -> Unit
) {
    // rememberSaveable: el diálogo sigue abierto aunque se gire la pantalla
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = Fondo,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(titulo)
                        Text(subtitulo, color = Amarillo, style = MaterialTheme.typography.bodyMedium)
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarDialogo = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Petroleo,
                    titleContentColor = Blanco,
                    actionIconContentColor = Blanco
                )
            )
        },
        content = contenido
    )

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("¿Quieres salir?") },
            text = { Text("Para volver a entrar tendrás que ingresar tu RUT.") },
            confirmButton = {
                TextButton(onClick = onCerrarSesion, enabled = !cerrando) {
                    Text(if (cerrando) "Saliendo..." else "Salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}
