package com.flowvid.app.ui.theme

import androidx.compose.ui.graphics.Color

// Dark palette — used app-wide by default, and always for the immersive Feed
// chrome regardless of the user's theme setting (video benefits from a
// consistently dark surround for contrast, like a film reel in a dark room).
val FlowVidBackgroundDark = Color(0xFF0B0B0D)
val FlowVidSurfaceDark = Color(0xFF16161A)
val FlowVidSurfaceDarkElevated = Color(0xFF1F1F24)
val FlowVidOnDark = Color(0xFFF2F0EC)
val FlowVidOnDarkMuted = Color(0xFFA8A6A1)

// The single deliberate accent, used sparingly: progress fill, active icon
// state, the speed badge. A warm amber/honey "projector bulb" glow.
val FlowVidAmber = Color(0xFFE3A83B)
val FlowVidAmberDim = Color(0xFF8A6A2C)

val FlowVidError = Color(0xFFE5484D)

// Light palette — used only by utility screens (Settings, Search, Favorites,
// Folder picker, Onboarding) when the user's theme is Light or System-light.
val FlowVidBackgroundLight = Color(0xFFFBFAF7)
val FlowVidSurfaceLight = Color(0xFFFFFFFF)
val FlowVidOnLight = Color(0xFF1B1A17)
val FlowVidOnLightMuted = Color(0xFF6E6B64)
