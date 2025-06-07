package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import notelycompose.shared.generated.resources.Res
import notelycompose.shared.generated.resources.ic_empty_notes
import org.jetbrains.compose.resources.painterResource
import notelycompose.shared.generated.resources.empty_list_title
import notelycompose.shared.generated.resources.empty_list_description
import notelycompose.shared.generated.resources.empty_list_description_tablet
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmptyNoteUi(
    isTablet: Boolean
) {
    val emptyNoteDescStr = if(isTablet) {
        stringResource(Res.string.empty_list_description_tablet)
    } else {
        stringResource(Res.string.empty_list_description)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .offset(y = (-40).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_empty_notes),
            contentDescription = "No Notes",
            modifier = Modifier.size(250.dp),
            tint = Color(0xFFD18B60)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.empty_list_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = LocalCustomColors.current.bodyContentColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = emptyNoteDescStr,
            fontSize = 16.sp,
            color = Color(0xFF6B6B6B),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}