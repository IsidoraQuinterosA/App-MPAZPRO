package com.example.mpaz_pro_3.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mpaz_pro_3.R
import com.example.mpaz_pro_3.model.Rol
import com.example.mpaz_pro_3.ui.components.TarjetaOpcion
import com.example.mpaz_pro_3.ui.theme.Amarillo
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.Petroleo
import com.example.mpaz_pro_3.ui.theme.Tinta
import com.example.mpaz_pro_3.ui.utils.esPantallaAncha

@Composable
fun RolesScreen(onRolSeleccionado: (Rol) -> Unit) {
    // Se activa al entrar para animar la aparición de las tarjetas
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val modifierBase = Modifier
        .fillMaxSize()
        .background(Petroleo)
        .systemBarsPadding()
        .padding(24.dp)

    if (esPantallaAncha()) {
        // Tablet o celular girado: logo a la izquierda y perfiles a la derecha
        Row(modifier = modifierBase, verticalAlignment = Alignment.CenterVertically) {
            Encabezado(modifier = Modifier.weight(1f))
            Spacer(Modifier.width(24.dp))
            ListaRoles(
                visible = visible,
                onRolSeleccionado = onRolSeleccionado,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            )
        }
    } else {
        // Celular vertical: todo en una columna
        Column(
            modifier = modifierBase.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Encabezado()
            Spacer(Modifier.height(32.dp))
            ListaRoles(visible = visible, onRolSeleccionado = onRolSeleccionado)
        }
    }
}

@Composable
private fun Encabezado(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Logo MPAZ PRO 3",
            modifier = Modifier.width(150.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("MPAZ PRO 3.º", style = MaterialTheme.typography.displaySmall, color = Blanco)
        Text(
            "Escuela Escritora Marcela Paz",
            style = MaterialTheme.typography.titleMedium,
            color = Amarillo,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ListaRoles(
    visible: Boolean,
    onRolSeleccionado: (Rol) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("¿Quién eres?", style = MaterialTheme.typography.headlineSmall, color = Blanco)

        Rol.entries.forEachIndexed { indice, rol ->
            // Cada tarjeta aparece un poco después que la anterior
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = indice * 150)) +
                    slideInVertically(tween(400, delayMillis = indice * 150)) { it }
            ) {
                TarjetaOpcion(
                    titulo = "Soy ${rol.titulo.lowercase()}",
                    descripcion = descripcionRol(rol),
                    icono = iconoRol(rol),
                    onClick = { onRolSeleccionado(rol) },
                    colorFondo = if (rol == Rol.ESTUDIANTE) Amarillo else Blanco,
                    colorTexto = Tinta
                )
            }
        }
    }
}

private fun descripcionRol(rol: Rol) = when (rol) {
    Rol.ESTUDIANTE -> "Aprende y responde actividades"
    Rol.DOCENTE -> "Asigna contenidos y revisa resultados"
    Rol.UTP -> "Revisa y aprueba contenidos"
}

private fun iconoRol(rol: Rol) = when (rol) {
    Rol.ESTUDIANTE -> Icons.Filled.School
    Rol.DOCENTE -> Icons.Filled.Person
    Rol.UTP -> Icons.Filled.VerifiedUser
}
