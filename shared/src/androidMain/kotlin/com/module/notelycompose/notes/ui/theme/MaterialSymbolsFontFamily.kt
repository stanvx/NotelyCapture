package com.module.notelycompose.notes.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.module.notelycompose.android.R

/**
 * Material Symbols Outlined font family using the variable font.
 * 
 * This provides access to Google's latest Material Symbols with support for
 * variable font features including FILL, GRAD, opsz, and wght axes.
 * 
 * Requires Android API 26+ for variable font features.
 */
@OptIn(ExperimentalTextApi::class)
actual val MaterialSymbolsOutlined = FontFamily(
    Font(
        R.font.material_symbols_outlined_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),           // wght: Normal weight
            FontVariation.grade(0),              // GRAD: Standard grade
            FontVariation.Setting("FILL", 0f),   // FILL: Outlined (0) vs Filled (1)
            FontVariation.Setting("opsz", 24f)   // opsz: Optical size 24dp (standard)
        )
    )
)

/**
 * Material Symbols Outlined with filled style
 */
@OptIn(ExperimentalTextApi::class)
actual val MaterialSymbolsFilled = FontFamily(
    Font(
        R.font.material_symbols_outlined_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.grade(0),
            FontVariation.Setting("FILL", 1f),   // FILL: Filled style
            FontVariation.Setting("opsz", 24f)
        )
    )
)

/**
 * Material Symbols Outlined with larger optical size for bigger icons
 */
@OptIn(ExperimentalTextApi::class)
actual val MaterialSymbolsLarge = FontFamily(
    Font(
        R.font.material_symbols_outlined_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.grade(0),
            FontVariation.Setting("FILL", 0f),
            FontVariation.Setting("opsz", 48f)   // opsz: Larger optical size
        )
    )
)
