package com.module.notelycompose.notes.ui.theme

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import notelycompose.shared.generated.resources.Res
import notelycompose.shared.generated.resources.poppins_bold
import notelycompose.shared.generated.resources.poppins_regular

@Composable
fun PoppingsFontFamily() = FontFamily(
    Font(Res.font.poppins_regular, weight = FontWeight.Normal),
    Font(Res.font.poppins_bold, weight = FontWeight.Bold)
)
