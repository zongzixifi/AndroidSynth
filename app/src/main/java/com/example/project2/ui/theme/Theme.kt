package com.example.project2.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.project2.R

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF333366),
    secondary = Color(0xFF669999),
    tertiary = Color(0xFFFF99CC),

    background = Color(0xFF999999),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFFFFBFE),
    onSurface = Color(0xFF666699),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF333366),
    secondary = Color(0xFF669999),
    tertiary = Color(0xFFFF99CC),
    background = Color(0xFF999999),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFFFFBFE),
    onSurface = Color(0xFF666699),

)

val D_DIN = FontFamily(
    Font(R.font.d_din_pro_400_regular, FontWeight.Normal),
    Font(R.font.d_din_pro_500_medium, FontWeight.Medium),
    Font(R.font.d_din_pro_600_semibold, FontWeight.SemiBold),
    Font(R.font.d_din_pro_700_bold, FontWeight.Bold),
    Font(R.font.d_din_pro_800_extrabold, FontWeight.ExtraBold),
    Font(R.font.d_din_pro_900_heavy, FontWeight.Black)
)

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = D_DIN),
    displayMedium = TextStyle(fontFamily = D_DIN),
    displaySmall = TextStyle(fontFamily = D_DIN),
    headlineLarge = TextStyle(fontFamily = D_DIN),
    headlineMedium = TextStyle(fontFamily = D_DIN),
    headlineSmall = TextStyle(fontFamily = D_DIN),
    titleLarge = TextStyle(fontFamily = D_DIN),
    titleMedium = TextStyle(fontFamily = D_DIN),
    titleSmall = TextStyle(fontFamily = D_DIN),
    bodyLarge = TextStyle(fontFamily = D_DIN, fontWeight = FontWeight.Bold),
    bodyMedium = TextStyle(fontFamily = D_DIN),
    bodySmall = TextStyle(fontFamily = D_DIN),
    labelLarge = TextStyle(fontFamily = D_DIN),
    labelMedium = TextStyle(fontFamily = D_DIN),
    labelSmall = TextStyle(fontFamily = D_DIN)
)

@Composable
fun Project2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
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
        typography = AppTypography,
        content = content
    )
}