package com.helios.core.designsystem.type

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Roboto Flex typography scale (10 sizes) from shared-spec/design-tokens.json.
 * Numeric variants (tabular lining) for charts and tickers.
 */
object HeliosTypography {

    val displayFontFamily = FontFamily.SansSerif // Roboto Flex fallback
    val textFontFamily = FontFamily.SansSerif
    val monoFontFamily = FontFamily.Monospace // Roboto Mono fallback

    /** Hero 56/56, weight 300, tracking -0.02em */
    val hero = TextStyle(
        fontFamily = displayFontFamily,
        fontSize = 56.sp,
        fontWeight = FontWeight.Light,
        lineHeight = 56.sp,
        letterSpacing = (-0.02).em
    )

    /** Title-1 34/40, weight 400, tracking -0.01em */
    val title1 = TextStyle(
        fontFamily = displayFontFamily,
        fontSize = 34.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 40.sp,
        letterSpacing = (-0.01).em
    )

    /** Title-2 28/34, weight 400, tracking -0.01em */
    val title2 = TextStyle(
        fontFamily = displayFontFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 34.sp,
        letterSpacing = (-0.01).em
    )

    /** Title-3 22/28, weight 400, tracking -0.01em */
    val title3 = TextStyle(
        fontFamily = displayFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 28.sp,
        letterSpacing = (-0.01).em
    )

    /** Headline 17/22, weight 600, tracking -0.01em */
    val headline = TextStyle(
        fontFamily = textFontFamily,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp,
        letterSpacing = (-0.01).em
    )

    /** Body 16/22, weight 400, tracking -0.01em */
    val body = TextStyle(
        fontFamily = textFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp,
        letterSpacing = (-0.01).em
    )

    /** Callout 14/20, weight 400, tracking -0.01em */
    val callout = TextStyle(
        fontFamily = textFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        letterSpacing = (-0.01).em
    )

    /** Subheadline 13/18, weight 400, tracking -0.01em */
    val subheadline = TextStyle(
        fontFamily = textFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp,
        letterSpacing = (-0.01).em
    )

    /** Caption 11/14, weight 400, tracking +0.01em */
    val caption = TextStyle(
        fontFamily = textFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 14.sp,
        letterSpacing = 0.01.em
    )

    /** Caption-2 10/12, weight 500, tracking +0.06em */
    val caption2 = TextStyle(
        fontFamily = textFontFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 12.sp,
        letterSpacing = 0.06.em
    )

    /** Numeric variant for charts: tabular lining figures */
    val chartNumeric = TextStyle(
        fontFamily = monoFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp,
        textAlign = TextAlign.End
    )

    /** Numeric variant for live tickers: tabular lining figures */
    val liveTicker = TextStyle(
        fontFamily = monoFontFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 34.sp,
        letterSpacing = (-0.01).em
    )
}
