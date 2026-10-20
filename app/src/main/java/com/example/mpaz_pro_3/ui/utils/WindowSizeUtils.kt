@file:kotlin.OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package com.example.mpaz_pro_3.ui.utils

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable

@Composable
fun obtenerWindowSizeClass(): WindowSizeClass {
    return calculateWindowSizeClass(LocalActivity.current as android.app.Activity)
}

// true en tablets o en un celular girado: se usa un diseño más ancho
@Composable
fun esPantallaAncha(): Boolean {
    return obtenerWindowSizeClass().widthSizeClass != WindowWidthSizeClass.Compact
}
