package com.example.mpaz_pro_3.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mpaz_pro_3.model.Rol
import com.example.mpaz_pro_3.ui.screens.AprendeScreen
import com.example.mpaz_pro_3.ui.screens.AsignarScreen
import com.example.mpaz_pro_3.ui.screens.DesafiosScreen
import com.example.mpaz_pro_3.ui.screens.DocenteScreen
import com.example.mpaz_pro_3.ui.screens.EstudianteScreen
import com.example.mpaz_pro_3.ui.screens.GuiasScreen
import com.example.mpaz_pro_3.ui.screens.InformeScreen
import com.example.mpaz_pro_3.ui.screens.LoginScreen
import com.example.mpaz_pro_3.ui.screens.PruebaScreen
import com.example.mpaz_pro_3.ui.screens.ResultadoScreen
import com.example.mpaz_pro_3.ui.screens.RevisionScreen
import com.example.mpaz_pro_3.ui.screens.RolesScreen
import com.example.mpaz_pro_3.ui.screens.UnidadScreen
import com.example.mpaz_pro_3.ui.screens.UtpScreen
import com.example.mpaz_pro_3.ui.theme.Amarillo
import com.example.mpaz_pro_3.ui.theme.Petroleo
import com.example.mpaz_pro_3.viewmodel.SesionViewModel

@Composable
fun AppNavigation(sesionViewModel: SesionViewModel = viewModel()) {
    val rutaInicial by sesionViewModel.rutaInicial.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    // Mientras se revisa si hay sesión guardada, se muestra un indicador de carga
    val inicio = rutaInicial
    if (inicio == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Petroleo)
        ) {
            CircularProgressIndicator(color = Amarillo)
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = inicio,
        // Animaciones al cambiar de pantalla
        enterTransition = { slideInHorizontally { it / 3 } + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally { it / 3 } + fadeOut() }
    ) {
        composable(Rutas.ROLES) {
            RolesScreen(onRolSeleccionado = { rol -> navController.navigate(Rutas.login(rol)) })
        }

        composable(Rutas.LOGIN) { entrada ->
            val rol = Rol.valueOf(entrada.arguments?.getString("rol") ?: Rol.ESTUDIANTE.name)
            LoginScreen(
                rol = rol,
                onVolver = { navController.popBackStack() },
                onIngresoExitoso = {
                    navController.navigate(Rutas.panelDe(rol)) {
                        // Se borra el historial: "atrás" no vuelve al login
                        popUpTo(Rutas.ROLES) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.ESTUDIANTE) {
            EstudianteScreen(
                onSesionCerrada = { irARoles(navController) },
                onAbrirUnidad = { objetivoId -> navController.navigate(Rutas.unidad(objetivoId)) }
            )
        }

        composable(
            route = Rutas.UNIDAD,
            arguments = listOf(navArgument("objetivoId") { type = NavType.IntType })
        ) { entrada ->
            val objetivoId = entrada.arguments?.getInt("objetivoId") ?: 0
            UnidadScreen(
                onVolver = { navController.popBackStack() },
                onAprende = { navController.navigate(Rutas.aprende(objetivoId)) },
                onDesafios = { navController.navigate(Rutas.desafios(objetivoId)) },
                onPrueba = { navController.navigate(Rutas.prueba(objetivoId)) },
                onVerResultado = { intentoId -> navController.navigate(Rutas.resultado(intentoId)) }
            )
        }

        composable(
            route = Rutas.APRENDE,
            arguments = listOf(navArgument("objetivoId") { type = NavType.IntType })
        ) {
            AprendeScreen(onVolver = { navController.popBackStack() })
        }

        composable(
            route = Rutas.DESAFIOS,
            arguments = listOf(navArgument("objetivoId") { type = NavType.IntType })
        ) {
            DesafiosScreen(onVolver = { navController.popBackStack() })
        }

        composable(
            route = Rutas.PRUEBA,
            arguments = listOf(navArgument("objetivoId") { type = NavType.IntType })
        ) {
            PruebaScreen(
                onVolver = { navController.popBackStack() },
                onTerminada = { intentoId ->
                    // Se quita la prueba del historial: "atrás" vuelve a la unidad
                    navController.navigate(Rutas.resultado(intentoId)) {
                        popUpTo(Rutas.PRUEBA) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Rutas.RESULTADO,
            arguments = listOf(navArgument("intentoId") { type = NavType.LongType })
        ) {
            ResultadoScreen(onVolver = { navController.popBackStack() })
        }

        composable(Rutas.DOCENTE) {
            DocenteScreen(
                onSesionCerrada = { irARoles(navController) },
                onAsignar = { navController.navigate(Rutas.ASIGNAR) },
                onGuias = { navController.navigate(Rutas.GUIAS) },
                onInforme = { navController.navigate(Rutas.INFORME) }
            )
        }

        composable(Rutas.INFORME) {
            InformeScreen(onVolver = { navController.popBackStack() })
        }

        composable(Rutas.GUIAS) {
            GuiasScreen(onVolver = { navController.popBackStack() })
        }

        composable(Rutas.ASIGNAR) {
            AsignarScreen(onVolver = { navController.popBackStack() })
        }

        composable(Rutas.UTP) {
            UtpScreen(
                onSesionCerrada = { irARoles(navController) },
                onRevisar = { objetivoId -> navController.navigate(Rutas.revision(objetivoId)) }
            )
        }

        composable(
            route = Rutas.REVISION,
            arguments = listOf(navArgument("objetivoId") { type = NavType.IntType })
        ) {
            RevisionScreen(onVolver = { navController.popBackStack() })
        }
    }
}

private fun irARoles(navController: NavHostController) {
    navController.navigate(Rutas.ROLES) {
        popUpTo(navController.graph.id) { inclusive = true }
    }
}
