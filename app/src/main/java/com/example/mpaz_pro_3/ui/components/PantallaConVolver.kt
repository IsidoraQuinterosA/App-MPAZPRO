package com.example.mpaz_pro_3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.Fondo
import com.example.mpaz_pro_3.ui.theme.Petroleo

// Estructura de las pantallas internas: barra con botón volver y contenido con scroll.
// widthIn(max) evita que en tablets el contenido se estire de lado a lado.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConVolver(
    titulo: String,
    onVolver: () -> Unit,
    color: Color = Petroleo,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    contenido: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = Fondo,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = color,
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth(),
                content = contenido
            )
        }
    }
}
