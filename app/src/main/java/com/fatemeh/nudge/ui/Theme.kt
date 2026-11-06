package com.fatemeh.nudge.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Mint = Color(0xFF5CCFA6)
val MintDark = Color(0xFF2E9E78)
val Cream = Color(0xFFFFFBF2)
val Ink = Color(0xFF2B2B2B)

@Composable
fun NudgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Mint, onPrimary = Ink,
            secondary = MintDark, background = Cream, surface = Color.White,
            primaryContainer = Color(0xFFD6F5E8), onSurface = Ink
        ),
        content = content
    )
}
