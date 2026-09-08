package com.flowvid.app.ui.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.flowvid.app.R
import com.flowvid.app.ui.theme.FlowVidBackgroundDark
import com.flowvid.app.ui.theme.FlowVidOnDark
import com.flowvid.app.util.PermissionUtils

@Composable
fun OnboardingScreen(onAccessGranted: () -> Unit) {
    val context = LocalContext.current
    var wasDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted || PermissionUtils.hasAnyAccess(context)) {
            onAccessGranted()
        } else {
            wasDenied = true
        }
    }

    Surface(color = FlowVidBackgroundDark, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = FlowVidOnDark,
            )
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = FlowVidOnDark.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp),
                textAlign = TextAlign.Center,
            )

            if (!wasDenied) {
                Text(
                    text = stringResource(R.string.onboarding_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = FlowVidOnDark,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.onboarding_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = FlowVidOnDark.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp, bottom = 32.dp),
                )
                Button(onClick = { permissionLauncher.launch(PermissionUtils.primaryMediaPermission()) }) {
                    Text(stringResource(R.string.onboarding_continue))
                }
            } else {
                Text(
                    text = stringResource(R.string.onboarding_denied_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = FlowVidOnDark,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.onboarding_denied_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = FlowVidOnDark.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp, bottom = 32.dp),
                )
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.onboarding_open_settings))
                }
                TextButton(onClick = {
                    wasDenied = false
                    permissionLauncher.launch(PermissionUtils.primaryMediaPermission())
                }) {
                    Text(stringResource(R.string.onboarding_try_again), color = FlowVidOnDark.copy(alpha = 0.7f))
                }
            }
        }
    }
}
