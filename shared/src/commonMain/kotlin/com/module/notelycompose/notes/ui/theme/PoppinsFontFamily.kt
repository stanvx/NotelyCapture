package com.module.notelycompose.notes.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.poppins_bold
import com.module.notelycompose.resources.poppins_medium
import com.module.notelycompose.resources.poppins_regular
import com.module.notelycompose.resources.poppins_semibold

/**
 * Material 3 Expressive Poppins font family with proper weight mapping
 * 
 * Provides complete font weight coverage for Material 3 Expressive Typography:
 * - Light (300): Fallback to Regular for subtle text
 * - Normal (400): Regular weight for body text
 * - Medium (500): Medium weight for enhanced emphasis
 * - SemiBold (600): SemiBold weight for strong emphasis
 * - Bold (700): Bold weight for headings
 * - ExtraBold (800): Fallback to Bold for maximum emphasis
 */
@Composable
fun PoppinsFontFamily(): FontFamily {
    return FontFamily(
        // Light weight (300) - fallback to regular
        Font(Res.font.poppins_regular, weight = FontWeight.Light),
        
        // Normal weight (400) - primary body text
        Font(Res.font.poppins_regular, weight = FontWeight.Normal),
        
        // Medium weight (500) - enhanced emphasis 
        Font(Res.font.poppins_medium, weight = FontWeight.Medium),
        
        // SemiBold weight (600) - strong emphasis
        Font(Res.font.poppins_semibold, weight = FontWeight.SemiBold),
        
        // Bold weight (700) - headings and titles
        Font(Res.font.poppins_bold, weight = FontWeight.Bold),
        
        // ExtraBold weight (800) - fallback to bold for maximum emphasis
        Font(Res.font.poppins_bold, weight = FontWeight.ExtraBold)
    )
}
