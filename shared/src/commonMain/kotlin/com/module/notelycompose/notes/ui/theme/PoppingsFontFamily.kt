package com.module.notelycompose.notes.ui.theme

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.poppins_bold
import com.module.notelycompose.resources.poppins_regular

/**
 * Material 3 Expressive Poppins font family with weight mapping
 * 
 * Uses existing Regular and Bold weights, with fallbacks for full M3 typography scale.
 * For optimal M3 expressiveness, consider adding Medium (500) and SemiBold (600) weights.
 */
@Composable
fun PoppingsFontFamily() = FontFamily(
    // Core weights (available)
    Font(Res.font.poppins_regular, weight = FontWeight.Normal), // 400
    Font(Res.font.poppins_bold, weight = FontWeight.Bold),     // 700
    
    // Fallback mappings for M3 typography scale
    Font(Res.font.poppins_regular, weight = FontWeight.Light),    // 300 -> Regular fallback
    Font(Res.font.poppins_regular, weight = FontWeight.Medium),   // 500 -> Regular fallback  
    Font(Res.font.poppins_bold, weight = FontWeight.SemiBold),    // 600 -> Bold fallback
    Font(Res.font.poppins_bold, weight = FontWeight.ExtraBold),   // 800 -> Bold fallback
)

/**
 * Get the Poppins font family optimized for Material 3 Expressive typography
 */
@Composable
fun material3PoppingsFontFamily(): FontFamily = PoppingsFontFamily()
