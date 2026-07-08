package com.bodycontrol

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.bodycontrol.data.PracticeRepository
import com.bodycontrol.notify.ReminderScheduler
import com.bodycontrol.ui.App

private val LightColors = lightColorScheme(
    primary = Color(0xFF0E9F7E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFF3E8),
    onPrimaryContainer = Color(0xFF00382C),
    secondaryContainer = Color(0xFFE3F0EB),
    onSecondaryContainer = Color(0xFF14332A),
    background = Color(0xFFF4F7F6),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFEDF2F0),
    onSurfaceVariant = Color(0xFF5B6560),
    outline = Color(0xFFC3CBC7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FE3C4),
    onPrimary = Color(0xFF00382C),
    primaryContainer = Color(0xFF11463A),
    onPrimaryContainer = Color(0xFFCFF3E8),
    secondaryContainer = Color(0xFF25332E),
    onSecondaryContainer = Color(0xFFD6E7E0),
    background = Color(0xFF0F1513),
    onBackground = Color(0xFFE1E4E1),
    surface = Color(0xFF161D1B),
    onSurface = Color(0xFFE1E4E1),
    surfaceVariant = Color(0xFF1E2623),
    onSurfaceVariant = Color(0xFFA7B2AD),
    outline = Color(0xFF3C4642),
)

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* 用户选择即可，无需额外处理 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PracticeRepository.init(this)
        ReminderScheduler.ensureChannel(this)
        requestNotificationPermissionIfNeeded()

        enableEdgeToEdge()
        setContent {
            val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
            MaterialTheme(colorScheme = colors) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    App()
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
