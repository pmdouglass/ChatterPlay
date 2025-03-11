package com.example.chatterplay.ui.theme


import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CRDarkColorScheme = AppColorScheme(
    background = light,
    onBackground = onLight,
    textOnBackground = textOnLight,

    gameBackground = background,
    onGameBackground = onPrimary,
    textOnGameBackground = textOnPrimary,

    primary = yellow,
    highlight = highlight,
    custom = customPurple
)


private val CRLightColorScheme = AppColorScheme(
    background = light,
    onBackground = onLight,
    textOnBackground = textOnLight,

    gameBackground = background,
    onGameBackground = onPrimary,
    textOnGameBackground = textOnPrimary,

    primary = yellow,
    highlight = highlight,
    custom = customPurple
)

private val CRtypography = AppTypography(
    headingSmall = TextStyle(),
    headingMedium = TextStyle(
        fontSize = 27.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp,
        letterSpacing = 1.5.sp
    ),
    headingLarge = TextStyle(
        fontSize = 27.sp,
        lineHeight = 28.sp,
        letterSpacing = 1.5.sp
    ),
    titleSmall = TextStyle(),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = .5.sp
    ),
    titleLarge = TextStyle(),
    bodySmall = TextStyle(
        fontSize = 12.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 13.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 14.sp
    ),
    infoSmall = TextStyle(
        fontSize = 11.sp
    ),
    infoMedium = TextStyle(
        fontSize = 13.sp
    ),
    infoLarge = TextStyle(
        fontSize = 16.sp
    ),




    H0 = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    ),
    H1 = TextStyle(
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    ),
    H2 = TextStyle(
        fontSize = 23.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    ),
    H3 = TextStyle(
        fontSize = 27.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    ),
    H4 = TextStyle(
        fontSize = 33.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    ),
    H5 = TextStyle(
        fontSize = 39.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    ),
    H6 = TextStyle(
        fontSize = 65.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    ),
    H7 = TextStyle(
        fontSize = 127.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    ),
    T1 = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
    ),
    T2 = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
    ),
    T3 = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    ),
    T4 = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    ),
    T5 = TextStyle(
        fontSize = 19.sp,
        fontWeight = FontWeight.SemiBold
    )



    )

private val CRShape = AppShape(
    container = RoundedCornerShape(15.dp),
    button = RoundedCornerShape(25.dp)
)

private val CRSize = AppSize(
    large = 24.dp
)

@Composable
fun CRAppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) CRDarkColorScheme else CRLightColorScheme
    val rippleIndication = rememberRipple()
    CompositionLocalProvider(
        LocalAppColorScheme provides colorScheme,
        LocalApptypography provides CRtypography,
        LocalAppShape provides CRShape,
        LocalAppSize provides CRSize,
        LocalIndication provides rippleIndication,
        content = content
    )
}

object CRAppTheme {
    val colorScheme: AppColorScheme
        @Composable get() = LocalAppColorScheme.current

    val typography: AppTypography
        @Composable get() = LocalApptypography.current

    val shape: AppShape
        @Composable get() = LocalAppShape.current

    val size: AppSize
        @Composable get () = LocalAppSize.current
}