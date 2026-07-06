package com.bodycontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import com.bodycontrol.ui.App

private val LightColors = lightColorScheme(
    primary = Color(0xFF0E7C66),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7F2E2),
    onPrimaryContainer = Color(0xFF00201A),
    secondaryContainer = Color(0xFFCDEBDF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FD9BE),
    onPrimary = Color(0xFF003730),
    primaryContainer = Color(0xFF005043),
    onPrimaryContainer = Color(0xFFB7F2E2),
    secondaryContainer = Color(0xFF32483F),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dark = isSystemInDarkTheme()
            val context = LocalContext.current
            val colors = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                    if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                dark -> DarkColors
                else -> LightColors
            }
            MaterialTheme(colorScheme = colors) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    App()
                }
            }
        }
    }
}
