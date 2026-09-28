package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FarmGreenPrimaryDark,
    onPrimary = FarmGreenOnPrimaryDark,
    primaryContainer = FarmGreenContainerDark,
    onPrimaryContainer = FarmGreenOnContainerDark,
    secondary = FarmSageSecondaryDark,
    onSecondary = FarmSageOnSecondaryDark,
    secondaryContainer = FarmSageContainerDark,
    onSecondaryContainer = FarmSageOnContainerDark,
    tertiary = HarvestAmberTertiaryDark,
    onTertiary = HarvestAmberOnTertiaryDark,
    tertiaryContainer = HarvestAmberContainerDark,
    onTertiaryContainer = HarvestAmberOnContainerDark,
    background = OrganicBackgroundDark,
    surface = OrganicSurfaceDark,
    surfaceVariant = OrganicSurfaceVariantDark,
    outline = OrganicOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = FarmGreenPrimary,
    onPrimary = FarmGreenOnPrimary,
    primaryContainer = FarmGreenContainerDark.copy(alpha = 0.15f),
    onPrimaryContainer = FarmGreenOnContainer,
    secondary = FarmSageSecondary,
    onSecondary = FarmSageOnSecondary,
    secondaryContainer = FarmSageContainer,
    onSecondaryContainer = FarmSageOnContainer,
    tertiary = HarvestAmberTertiary,
    onTertiary = HarvestAmberOnTertiary,
    tertiaryContainer = HarvestAmberContainer,
    onTertiaryContainer = HarvestAmberOnContainer,
    background = OrganicBackgroundLight,
    surface = OrganicSurfaceLight,
    surfaceVariant = OrganicSurfaceVariantLight,
    outline = OrganicOutlineLight
)

@Composable
fun SolaivanamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep farm branding colors distinct
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = SolaivanamTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
