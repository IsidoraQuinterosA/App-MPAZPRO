package com.example.mpaz_pro_3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.mpaz_pro_3.ui.theme.colorAsignatura
import com.example.mpaz_pro_3.ui.theme.iconoAsignatura
import com.example.mpaz_pro_3.ui.utils.esPantallaAncha
import com.example.mpaz_pro_3.viewmodel.EstudianteViewModel
import com.example.mpaz_pro_3.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

@Composable
fun EstudianteScreen(
    onSesionCerrada: () -> Unit,
    onAbrirUnidad: (Int) -> Unit,
    viewModel: SesionViewModel = viewModel(),
    estudianteViewModel: EstudianteViewModel = viewModel()
) {
    val sesion by viewModel.sesion.collectAsStateWithLifecycle()
    val objetivos by estudianteViewModel.objetivos.collectAsStateWithLifecycle()
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
                // Busca en Room el objetivo de esta asignatura.
                // Solo está disponible si UTP lo aprobó y el docente lo asignó.
                val objetivo = objetivos.find { it.asignatura == asignatura.name }
                val disponible = objetivo != null && objetivo.estado == "APROBADO" && objetivo.asignado

                TarjetaAsignatura(
                    nombre = asignatura.nombre,
                    subtitulo = if (disponible) objetivo?.unidad ?: "" else "Próximamente",
                    disponible = disponible,
                    color = colorAsignatura(asignatura.name),
                    icono = iconoAsignatura(asignatura.name),
                    onClick = {
                        if (disponible && objetivo != null) {
                            onAbrirUnidad(objetivo.id)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("${asignatura.nombre} aún no está disponible")
                            }
                        }
                    }
                )
            }
        }
    }
}
