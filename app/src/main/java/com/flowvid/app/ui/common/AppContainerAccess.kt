package com.flowvid.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.flowvid.app.AppContainer
import com.flowvid.app.FlowVidApplication

@Composable
fun appContainer(): AppContainer {
    val context = LocalContext.current
    return (context.applicationContext as FlowVidApplication).container
}
