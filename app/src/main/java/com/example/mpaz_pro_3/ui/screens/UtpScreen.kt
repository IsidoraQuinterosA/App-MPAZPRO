package com.example.mpaz_pro_3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.ui.components.EtiquetaEstado
import com.example.mpaz_pro_3.ui.components.PanelScaffold
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.TintaSuave
import com.example.mpaz_pro_3.ui.theme.colorAsignatura
import com.example.mpaz_pro_3.ui.theme.iconoAsignatura
import com.example.mpaz_pro_3.ui.theme.nombreAsignatura
import com.example.mpaz_pro_3.ui.utils.esPantallaAncha
import com.example.mpaz_pro_3.viewmodel.SesionViewModel
import com.example.mpaz_pro_3.viewmodel.UtpViewModel

@Composable
fun UtpScreen(
    onSesionCerrada: () -> Unit,
    onRevisar: (Int) -> Unit,
    viewModel: SesionViewModel = viewModel(),
    utpViewModel: UtpViewModel = viewModel()
) {
    val sesion by viewModel.sesion.collectAsStateWithLifecycle()
    val cerrando by viewModel.cerrando.collectAsStateWithLifecycle()
    val objetivos by utpViewModel.objetivos.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val columnas = if (esPantallaAncha()) 2 else 1
    val pendientes = objetivos.count { it.estado == "PENDIENTE" }

    PanelScaffold(
        titulo = "Revisión UTP",
        subtitulo = sesion?.nombre ?: "",
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
                Text(
                    if (pendientes == 0) "No hay contenidos pendientes" else "Contenidos por revisar: $pendientes",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(objetivos) { objetivo ->
                Card(
                    onClick = { onRevisar(objetivo.id) },
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                        Icon(iconoAsignatura(objetivo.asignatura), contentDescription = null, tint = colorAsignatura(objetivo.asignatura))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(nombreAsignatura(objetivo.asignatura), style = MaterialTheme.typography.titleMedium)
                            Text(objetivo.unidad, style = MaterialTheme.typography.bodyMedium, color = TintaSuave)
                        }
                        EtiquetaEstado(objetivo.estado)
                    }
                }
            }
        }
    }
}
