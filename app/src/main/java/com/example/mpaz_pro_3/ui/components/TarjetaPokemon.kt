package com.example.mpaz_pro_3.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mpaz_pro_3.data.local.Recompensa
import com.example.mpaz_pro_3.ui.theme.AmarilloClaro
import com.example.mpaz_pro_3.ui.theme.Blanco
import com.example.mpaz_pro_3.ui.theme.TintaSuave

// Tarjeta con el Pokémon ganado. La imagen se descarga de internet con Coil (AsyncImage).
// Aparece con un pequeño rebote.
@Composable
fun TarjetaPokemon(
    recompensa: Recompensa,
    modifier: Modifier = Modifier,
    tamanoImagen: Dp = 140.dp
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(recompensa.pokemonId) { visible = true }
    val escala by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "escalaPokemon"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco),
        modifier = modifier.scale(escala)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            AsyncImage(
                model = recompensa.imagen,
                contentDescription = "Imagen de ${recompensa.nombre}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(tamanoImagen)
                    .clip(CircleShape)
                    .background(AmarilloClaro)
            )
            Text(recompensa.nombre, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text("N.º ${recompensa.pokemonId} · ${recompensa.tipos}", color = TintaSuave, textAlign = TextAlign.Center)
        }
    }
}
