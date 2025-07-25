package com.module.notelycompose.notes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material 3 Expressive Typography system using Poppins font family
 * 
 * Implements the complete M3 typography scale with 15 semantic type roles:
 * - Display (Large, Medium, Small): For large, prominent text
 * - Headline (Large, Medium, Small): For important headlines  
 * - Title (Large, Medium, Small): For medium-emphasis text
 * - Body (Large, Medium, Small): For regular reading text
 * - Label (Large, Medium, Small): For UI labels and captions
 * 
 * Based on Material 3 Typography Guidelines:
 * https://m3.material.io/styles/typography/overview
 */
@Composable
fun createMaterial3ExpressiveTypography(): Typography {
    val poppinsFamily = material3PoppingsFontFamily()
    
    return Typography(
        // Display styles - For large, prominent text (hero text, large numerals)
        displayLarge = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),
        
        // Headline styles - For important headlines and section headers
        headlineLarge = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
        
        // Title styles - For medium-emphasis text, card titles
        titleLarge = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        
        // Body styles - For regular reading text
        bodyLarge = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),
        
        // Label styles - For UI labels, captions, and supporting text
        labelLarge = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}

/**
 * Material 3 Expressive Typography Tokens
 * 
 * Provides semantic access to typography styles for specific use cases
 */
object Material3TypographyTokens {
    @Composable
    fun noteTitle() = createMaterial3ExpressiveTypography().headlineMedium
    
    @Composable
    fun noteBody() = createMaterial3ExpressiveTypography().bodyLarge
    
    @Composable
    fun noteTimestamp() = createMaterial3ExpressiveTypography().bodySmall
    
    @Composable
    fun buttonText() = createMaterial3ExpressiveTypography().labelLarge
    
    @Composable
    fun appBarTitle() = createMaterial3ExpressiveTypography().titleLarge
    
    @Composable
    fun cardTitle() = createMaterial3ExpressiveTypography().titleMedium
    
    @Composable
    fun caption() = createMaterial3ExpressiveTypography().labelSmall
}