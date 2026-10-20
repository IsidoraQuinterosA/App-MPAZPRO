package com.example.mpaz_pro_3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.model.Asignatura
import com.example.mpaz_pro_3.ui.components.PanelScaffold
import com.example.mpaz_pro_3.ui.components.TarjetaAsignatura
import com.example.mpaz_pro_3.ui.theme.CienciasColor
import com.example.mpaz_pro_3.ui.theme.HistoriaColor
import com.example.mpaz_pro_3.ui.theme.LenguajeColor
import com.example.mpaz_pro_3.ui.theme.MatematicaColor
import com.example.mpaz_pro_3.ui.utils.esPantallaAncha
import com.example.mpaz_pro_3.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

@Composable
fun EstudianteScreen(
    onSesionCerrada: () -> Unit,
    viewModel: SesionViewModel = viewModel()
) {
    val sesion by viewModel.sesion.collectAsStateWithLifecycle()
    val cerrando by viewModel.cerrando.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Celular: 2 columnas. Tablet o celular girado: 4 columnas
    val columnas = if (esPantallaAncha()) 4 else 2
    val primerNombre = sesion?.nombre?.substringBefore(" ") ?: ""

    PanelScaffold(
        titulo = "¡Hola, $primerNombre!",
        subtitulo = "¿Qué quieres aprender hoy?",
        cerrando = cerrando,
        onCerrarSesion = { viewModel.cerrarSesion(onSesionCerrada) },
        snackbarHostState = snackbarHostState
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnas),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text("Tus asignaturas", style = MaterialTheme.typography.titleLarge)
            }
            items(Asignatura.entries) { asignatura ->
                TarjetaAsignatura(
                    nombre = asignatura.nombre,
                    color = colorAsignatura(asignatura),
                    icono = iconoAsignatura(asignatura),
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("${asignatura.nombre}: disponible en la siguiente fase")
                        }
                    }
                )
            }
        }
    }
}

private fun colorAsignatura(asignatura: Asignatura) = when (asignatura) {
    Asignatura.LENGUAJE -> LenguajeColor
    Asignatura.MATEMATICA -> MatematicaColor
    Asignatura.CIENCIAS -> CienciasColor
    Asignatura.HISTORIA -> HistoriaColor
}

private fun iconoAsignatura(asignatura: Asignatura) = when (asignatura) {
    Asignatura.LENGUAJE -> Icons.AutoMirrored.Filled.MenuBook
    Asignatura.MATEMATICA -> Icons.Filled.Calculate
    Asignatura.CIENCIAS -> Icons.Filled.Eco
    Asignatura.HISTORIA -> Icons.Filled.Public
}
