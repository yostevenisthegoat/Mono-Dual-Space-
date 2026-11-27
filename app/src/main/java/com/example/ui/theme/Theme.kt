package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.data.preferences.ThemeMode

@Immutable
data class MonoSemanticColors(
    val primaryBackground: Color,
    val secondaryBackground: Color,
    val cardBackground: Color,
    val chipBackground: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val onAccent: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val isDark: Boolean
)

val LocalMonoColors = staticCompositionLocalOf {
    MonoSemanticColors(
        primaryBackground = MonoDarkPrimaryBg,
        secondaryBackground = MonoDarkSecondaryBg,
        cardBackground = MonoDarkCardBg,
        chipBackground = MonoDarkChipBg,
        border = MonoDarkBorder,
        textPrimary = MonoDarkTextPrimary,
        textSecondary = MonoDarkTextSecondary,
        accent = MonoDarkAccent,
        onAccent = MonoDarkOnAccent,
        success = MonoDarkSuccess,
        warning = MonoDarkWarning,
        danger = MonoDarkDanger,
        isDark = true
    )
}

object MonoTheme {
    val colors: MonoSemanticColors
        @Composable get() = LocalMonoColors.current
}

private val MonoDarkColorScheme = darkColorScheme(
    primary = MonoDarkAccent,
    onPrimary = MonoDarkOnAccent,
    primaryContainer = MonoDarkCardBg,
    onPrimaryContainer = MonoDarkTextPrimary,
    secondary = MonoDarkTextSecondary,
    onSecondary = MonoDarkPrimaryBg,
    secondaryContainer = MonoDarkSecondaryBg,
    onSecondaryContainer = MonoDarkTextPrimary,
    tertiary = MonoDarkTextSecondary,
    onTertiary = MonoDarkPrimaryBg,
    background = MonoDarkPrimaryBg,
    onBackground = MonoDarkTextPrimary,
    surface = MonoDarkSecondaryBg,
    onSurface = MonoDarkTextPrimary,
    surfaceVariant = MonoDarkCardBg,
    onSurfaceVariant = MonoDarkTextSecondary,
    outline = MonoDarkBorder,
    outlineVariant = MonoDarkBorder,
    error = MonoDarkDanger,
    onError = MonoDarkPrimaryBg
)

private val MonoLightColorScheme = lightColorScheme(
    primary = MonoLightAccent,
    onPrimary = MonoLightOnAccent,
    primaryContainer = MonoLightCardBg,
    onPrimaryContainer = MonoLightTextPrimary,
    secondary = MonoLightTextSecondary,
    onSecondary = MonoLightPrimaryBg,
    secondaryContainer = MonoLightSecondaryBg,
    onSecondaryContainer = MonoLightTextPrimary,
    tertiary = MonoLightTextSecondary,
    onTertiary = MonoLightPrimaryBg,
    background = MonoLightPrimaryBg,
    onBackground = MonoLightTextPrimary,
    surface = MonoLightSecondaryBg,
    onSurface = MonoLightTextPrimary,
    surfaceVariant = MonoLightCardBg,
    onSurfaceVariant = MonoLightTextSecondary,
    outline = MonoLightBorder,
    outlineVariant = MonoLightBorder,
    error = MonoLightDanger,
    onError = MonoLightOnAccent
)

val MonoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MonoDualSpaceTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) MonoDarkColorScheme else MonoLightColorScheme
    val monoColors = if (darkTheme) {
        MonoSemanticColors(
            primaryBackground = MonoDarkPrimaryBg,
            secondaryBackground = MonoDarkSecondaryBg,
            cardBackground = MonoDarkCardBg,
            chipBackground = MonoDarkChipBg,
            border = MonoDarkBorder,
            textPrimary = MonoDarkTextPrimary,
            textSecondary = MonoDarkTextSecondary,
            accent = MonoDarkAccent,
            onAccent = MonoDarkOnAccent,
            success = MonoDarkSuccess,
            warning = MonoDarkWarning,
            danger = MonoDarkDanger,
            isDark = true
        )
    } else {
        MonoSemanticColors(
            primaryBackground = MonoLightPrimaryBg,
            secondaryBackground = MonoLightSecondaryBg,
            cardBackground = MonoLightCardBg,
            chipBackground = MonoLightChipBg,
            border = MonoLightBorder,
            textPrimary = MonoLightTextPrimary,
            textSecondary = MonoLightTextSecondary,
            accent = MonoLightAccent,
            onAccent = MonoLightOnAccent,
            success = MonoLightSuccess,
            warning = MonoLightWarning,
            danger = MonoLightDanger,
            isDark = false
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(activity.window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalMonoColors provides monoColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MonoTypography,
            shapes = MonoShapes,
            content = content
        )
    }
}
