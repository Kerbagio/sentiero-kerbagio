// UI layer: the two bundled font families and the Material 3 type scale from DESIGN.md.
// The fonts ship in res/font so they render offline; screens only ask for M3 styles like titleLarge.
package com.anthony.sentiero.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.anthony.sentiero.R

val BarlowCondensed = FontFamily(
    Font(R.font.barlow_condensed_medium, FontWeight.Medium),
    Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold),
)

val SourceSans3 = FontFamily(
    Font(R.font.source_sans_3_regular, FontWeight.Normal),
    Font(R.font.source_sans_3_medium, FontWeight.Medium),
    Font(R.font.source_sans_3_semibold, FontWeight.SemiBold),
)

// Tabular figures: every digit has the same width, so "3 of 12" and dates don't jitter.
private const val TNUM = "tnum"

// DESIGN.md tokens. Sans styles carry tnum because dates and counts appear in them.
private val headline = TextStyle(
    fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp,
)
private val title = TextStyle(
    fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp,
)
private val titleSmall = TextStyle(
    fontFamily = SourceSans3, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp,
    fontFeatureSettings = TNUM,
)
private val body = TextStyle(
    fontFamily = SourceSans3, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp,
    fontFeatureSettings = TNUM,
)
private val bodySmall = TextStyle(
    fontFamily = SourceSans3, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp,
    fontFeatureSettings = TNUM,
)
private val labelLarge = TextStyle(
    fontFamily = SourceSans3, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp,
    fontFeatureSettings = TNUM,
)
private val label = TextStyle(
    fontFamily = SourceSans3, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp,
    letterSpacing = 0.02.em, fontFeatureSettings = TNUM,
)

// Mapping table from DESIGN.md "Typography".
val SentieroTypography = Typography(
    displaySmall = headline,
    headlineLarge = headline,
    headlineMedium = headline,
    headlineSmall = headline.copy(fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = title,
    titleMedium = titleSmall,
    titleSmall = titleSmall.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = body,
    bodyMedium = bodySmall,
    bodySmall = bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = labelLarge,
    labelMedium = label,
    labelSmall = label.copy(fontSize = 11.sp, lineHeight = 16.sp),
)
