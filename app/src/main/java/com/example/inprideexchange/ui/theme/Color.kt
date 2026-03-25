package com.example.inprideexchange.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// 🌞 LIGHT THEME
val OrangeLightColors = lightColorScheme(
    primary = Color(0xFFFF6E40),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFCCBC),
    onPrimaryContainer = Color(0xFF3B0A00),

    secondary = Color(0xFFFFAB91),
    onSecondary = Color(0xFF3B0A00),

    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF3B0A00),

    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF3B0A00),
    surfaceVariant = Color(0xFFFFE0B2),
    onSurfaceVariant = Color(0xFF6D4C41),

    outline = Color(0xFF8D6E63),
    error = Color(0xFFBA1A1A)
)

// 🌚 DARK THEME
val OrangeDarkColors = darkColorScheme(
    primary = Color(0xFFFF8A65),
    onPrimary = Color(0xFF3B0A00),
    primaryContainer = Color(0xFF5D1A00),
    onPrimaryContainer = Color(0xFFFFCCBC),

    secondary = Color(0xFFFFAB91),
    onSecondary = Color(0xFF3B0A00),

    background = Color(0xFF121212),
    onBackground = Color(0xFFFFE0B2),

    surface = Color(0xFF1C1B1B),
    onSurface = Color(0xFFFFCCBC),
    surfaceVariant = Color(0xFF3E2723),
    onSurfaceVariant = Color(0xFFFFAB91),

    outline = Color(0xFF8D6E63),
    error = Color(0xFFFFB4AB)
)




// 🌞 LIGHT BLUE THEME
val BlueLightColors = lightColorScheme(
    primary = Color(0xFF1565C0),             // Rich Royal Blue
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBBDEFB),    // Soft sky-blue container
    onPrimaryContainer = Color(0xFF001A43),

    secondary = Color(0xFF42A5F5),           // Lively accent blue
    onSecondary = Color(0xFF001A43),

    background = Color(0xFFF8FAFF),          // Subtle bluish white background
    onBackground = Color(0xFF0D1B2A),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0D1B2A),
    surfaceVariant = Color(0xFFE3F2FD),      // Very light blue-gray
    onSurfaceVariant = Color(0xFF1E3A5F),

    outline = Color(0xFF90CAF9),
    error = Color(0xFFB3261E)
)

// 🌚 DARK BLUE THEME
val BlueDarkColors = darkColorScheme(
    primary = Color(0xFF64B5F6),             // Vibrant light-blue for dark backgrounds
    onPrimary = Color(0xFF001A43),
    primaryContainer = Color(0xFF003C8F),    // Deep ocean blue container
    onPrimaryContainer = Color(0xFFBBDEFB),

    secondary = Color(0xFF1976D2),
    onSecondary = Color(0xFFE3F2FD),

    background = Color(0xFF0A0F16),          // Deep night navy
    onBackground = Color(0xFFE3F2FD),

    surface = Color(0xFF121212),
    onSurface = Color(0xFFE3F2FD),
    surfaceVariant = Color(0xFF1E3A5F),      // Muted cool navy for cards
    onSurfaceVariant = Color(0xFF90CAF9),

    outline = Color(0xFF64B5F6),
    error = Color(0xFFFFB4A9)
)




// 🌞 LIGHT GREEN THEME — Vibrant, fresh, natural
val GreenLightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),              // Rich Forest Green
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA5D6A7),     // Light mint container
    onPrimaryContainer = Color(0xFF00210C),

    secondary = Color(0xFF66BB6A),            // Soft vibrant leaf green
    onSecondary = Color(0xFF00210C),

    background = Color(0xFFF7FFF8),           // Very soft minty white background
    onBackground = Color(0xFF0C1F0D),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0C1F0D),
    surfaceVariant = Color(0xFFE8F5E9),       // Subtle pale green for inputs/cards
    onSurfaceVariant = Color(0xFF1B5E20),

    outline = Color(0xFF81C784),              // Muted green outline
    error = Color(0xFFB3261E)
)

// 🌚 DARK GREEN THEME — Deep forest with bright highlights
val GreenDarkColors = darkColorScheme(
    primary = Color(0xFF81C784),              // Fresh light green accent
    onPrimary = Color(0xFF00390F),
    primaryContainer = Color(0xFF1B5E20),     // Deep forest container
    onPrimaryContainer = Color(0xFFA5D6A7),

    secondary = Color(0xFF43A047),
    onSecondary = Color(0xFFE8F5E9),

    background = Color(0xFF0D130E),           // Very dark forest tone
    onBackground = Color(0xFFDCEFD8),

    surface = Color(0xFF121812),
    onSurface = Color(0xFFE8F5E9),
    surfaceVariant = Color(0xFF2E4630),       // Muted green-gray variant
    onSurfaceVariant = Color(0xFF81C784),

    outline = Color(0xFF66BB6A),
    error = Color(0xFFFFB4A9)
)



// 🌞 LIGHT THEME — Elegant grayscale with subtle depth
val BlackLightColors = lightColorScheme(
    primary = Color(0xFF000000),          // Pure black accents
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E0E0), // Light gray containers
    onPrimaryContainer = Color(0xFF000000),

    secondary = Color(0xFF424242),        // Deep neutral gray
    onSecondary = Color(0xFFFFFFFF),

    background = Color(0xFFFFFFFF),       // Pure white background
    onBackground = Color(0xFF000000),

    surface = Color(0xFFF7F7F7),          // Slightly off-white cards/surfaces
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF424242),

    outline = Color(0xFF9E9E9E),
    error = Color(0xFFBA1A1A)
)

// 🌚 DARK THEME — True dark mode with high contrast
val BlackDarkColors = darkColorScheme(
    primary = Color(0xFFFFFFFF),          // White primary for contrast
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1C1C1C), // Deep matte black
    onPrimaryContainer = Color(0xFFE0E0E0),

    secondary = Color(0xFF9E9E9E),        // Neutral mid-gray
    onSecondary = Color(0xFF000000),

    background = Color(0xFF000000),       // Absolute black background
    onBackground = Color(0xFFFFFFFF),

    surface = Color(0xFF121212),          // Slightly lifted black for cards
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),

    outline = Color(0xFF616161),
    error = Color(0xFFFFB4A9)
)

