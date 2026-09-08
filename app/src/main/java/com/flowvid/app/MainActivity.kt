package com.flowvid.app

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.flowvid.app.domain.model.ThemeMode
import com.flowvid.app.ui.navigation.FlowVidNavHost
import com.flowvid.app.ui.theme.FlowVidTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as FlowVidApplication).container

        setContent {
            val settings by container.settingsRepository.settings.collectAsState(initial = null)

            // Follow the user's landscape-rotation preference (section 19). The
            // Activity is declared with android:configChanges for orientation in
            // the manifest, so this never triggers a recreate — Compose simply
            // relays out for the new orientation.
            LaunchedEffect(settings?.allowLandscapeRotation) {
                requestedOrientation = if (settings?.allowLandscapeRotation == true) {
                    ActivityInfo.SCREEN_ORIENTATION_FULL_USER
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }

            FlowVidTheme(themeMode = settings?.themeMode ?: ThemeMode.SYSTEM) {
                FlowVidNavHost()
            }
        }
    }
}
