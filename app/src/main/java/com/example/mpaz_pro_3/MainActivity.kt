package com.example.mpaz_pro_3

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mpaz_pro_3.navigation.AppNavigation
import com.example.mpaz_pro_3.ui.theme.MPAZPRO3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Íconos claros en la barra de estado (las barras superiores son color petróleo)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            MPAZPRO3Theme {
                AppNavigation()
            }
        }
    }
}
