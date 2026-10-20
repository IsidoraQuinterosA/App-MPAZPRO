package com.example.mpaz_pro_3.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Tema de MPAZ PRO 3 con los colores del logo.
 * Se quitaron los colores dinámicos de Android 12+ para que la app
 * se vea siempre igual, sin importar el fondo de pantalla del teléfono.
 */
private val EsquemaMpaz = lightColorScheme(
    primary = Petroleo,
    onPrimary = Blanco,
    primaryContainer = CelesteClaro,
    onPrimaryContainer = Tinta,
    secondary = Amarillo,
    onSecondary = Tinta,
    tertiary = Celeste,
    onTertiary = Tinta,
    background = Fondo,
    onBackground = Tinta,
    surface = Blanco,
    onSurface = Tinta,
    surfaceVariant = CelesteClaro,
    onSurfaceVariant = TintaSuave,
    outline = Borde,
    error = ErrorRojo,
    onError = Blanco,
    errorContainer = ErrorFondo,
    onErrorContainer = ErrorRojo
)

private val FormasMpaz = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MPAZPRO3Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaMpaz,
        typography = Typography,
        shapes = FormasMpaz,
        content = content
    )
}
