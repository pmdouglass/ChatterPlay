package com.example.chatterplay.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

data class AppColorScheme(
    val background: Color,
    val onBackground: Color,
    val textOnBackground: Color,

    val gameBackground: Color,
    val onGameBackground: Color,
    val textOnGameBackground: Color,

    val primary: Color,
    val highlight: Color,
    val custom: Brush
    )

data class AppTypography(
    val headingSmall: TextStyle,
    val headingMedium: TextStyle,
    val headingLarge: TextStyle,
    val titleSmall: TextStyle,
    val titleMedium: TextStyle,
    val titleLarge: TextStyle,
    val bodySmall: TextStyle,
    val bodyMedium: TextStyle,
    val bodyLarge: TextStyle,
    val infoSmall: TextStyle,
    val infoMedium: TextStyle,
    val infoLarge: TextStyle,
    val H0: TextStyle,
    val H1: TextStyle,
    val H2: TextStyle,
    val H3: TextStyle,
    val H4: TextStyle,
    val H5: TextStyle,
    val T1: TextStyle,
    val T2: TextStyle,
    val T3: TextStyle,
    val T4: TextStyle,
    val T5: TextStyle

)

data class AppShape(
    val container: Shape,
    val button: Shape
)

data class AppSize(
    val large: Dp
)





val LocalAppColorScheme = staticCompositionLocalOf {
    AppColorScheme(

        background = Color.Unspecified,
        onBackground = Color.Unspecified,
        textOnBackground = Color.Unspecified,

        gameBackground = Color.Unspecified,
        onGameBackground = Color.Unspecified,
        textOnGameBackground = Color.Unspecified,

        primary = Color.Unspecified,
        highlight = Color.Unspecified,
        custom = Brush.horizontalGradient()

    )
}

val LocalApptypography = staticCompositionLocalOf {
    AppTypography(
        headingSmall = TextStyle.Default,
        headingMedium = TextStyle.Default,
        headingLarge = TextStyle.Default,
        titleSmall = TextStyle.Default,
        titleMedium = TextStyle.Default,
        titleLarge = TextStyle.Default,
        bodySmall = TextStyle.Default,
        bodyMedium = TextStyle.Default,
        bodyLarge = TextStyle.Default,
        infoSmall = TextStyle.Default,
        infoMedium = TextStyle.Default,
        infoLarge = TextStyle.Default,
        H0 = TextStyle.Default,
        H1 = TextStyle.Default,
        H2 = TextStyle.Default,
        H3 = TextStyle.Default,
        H4 = TextStyle.Default,
        H5 = TextStyle.Default,
        T1 = TextStyle.Default,
        T2 = TextStyle.Default,
        T3 = TextStyle.Default,
        T4 = TextStyle.Default,
        T5 = TextStyle.Default
    )
}

val LocalAppShape = staticCompositionLocalOf {
    AppShape(
        container = RectangleShape,
        button = RectangleShape
    )
}

val LocalAppSize = staticCompositionLocalOf {
    AppSize(
        large = Dp.Unspecified
    )
}




