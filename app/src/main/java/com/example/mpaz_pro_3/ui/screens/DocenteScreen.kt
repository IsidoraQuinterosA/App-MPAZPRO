package com.example.mpaz_pro_3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpaz_pro_3.ui.components.PanelScaffold
import com.example.mpaz_pro_3.ui.components.TarjetaOpcion
import com.example.mpaz_pro_3.ui.utils.esPantallaAncha
import com.example.mpaz_pro_3.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

// Opción del menú. Si accion es null, la función todavía no está disponible.
private data class OpcionMenu(
    val titulo: String,
    val descripcion: String,
    val icono: ImageVector,
    val accion: (() -> Unit)?
)

@Composable
fun DocenteScreen(
    onSesionCerrada: () -> Unit,
    onAsignar: () -> Unit,
    onGuias: () -> Unit,
    onInforme: () -> Unit,
    viewModel: SesionViewModel = viewModel()
) {
    val sesion by viewModel.sesion.collectAsStateWithLifecycle()
    val cerrando by viewModel.cerrando.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val columnas = if (esPantallaAncha()) 2 else 1

    val opciones = listOf(
        OpcionMenu("Asignar contenidos", "Elige objetivos y define los intentos de la prueba", Icons.AutoMirrored.Filled.Assignment, onAsignar),
        OpcionMenu("Subir guías", "Agrega guías en PDF, Word o imagen", Icons.Filled.UploadFile, onGuias),
        OpcionMenu("Informe pedagógico", "Revisa, valida y descarga los resultados", Icons.Filled.Assessment, onInforme)
    )

    PanelScaffold(
        titulo = "Panel docente",
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
            items(opciones) { opcion ->
                TarjetaOpcion(
                    titulo = opcion.titulo,
                    descripcion = opcion.descripcion,
                    icono = opcion.icono,
                    onClick = {
                        if (opcion.accion != null) {
                            opcion.accion.invoke()
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("${opcion.titulo}: disponible próximamente") }
                        }
                    }
                )
            }
        }
    }
}
