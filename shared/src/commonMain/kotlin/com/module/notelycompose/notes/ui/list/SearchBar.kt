package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.search_bar_search_description
import com.module.notelycompose.resources.search_bar_search_text
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchBar(
    onSearchByKeyword: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var isLabelVisible by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                isLabelVisible = !isFocused && searchText.isEmpty()
                onSearchByKeyword(it)
            },
            placeholder = {
                Text(
                    text = stringResource(Res.string.search_bar_search_text),
                    color = LocalCustomColors.current.languageSearchBorderColor
                )
            },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    isLabelVisible = !isFocused && searchText.isEmpty()
                },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                cursorColor = LocalCustomColors.current.searchOutlinedTextFieldColor,
                textColor = LocalCustomColors.current.searchOutlinedTextFieldColor,
                focusedBorderColor = LocalCustomColors.current.searchOutlinedTextFieldColor,
                unfocusedBorderColor = LocalCustomColors.current.searchOutlinedTextFieldColor,
                disabledBorderColor = LocalCustomColors.current.searchOutlinedTextFieldColor
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(Res.string.search_bar_search_description),
                    tint = LocalCustomColors.current.searchOutlinedTextFieldColor,
                    modifier = Modifier.size(38.dp).padding(start = 8.dp)
                )
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchText = ""
                            onSearchByKeyword(searchText)
                        },
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                LocalCustomColors.current.languageSearchCancelButtonColor.copy(alpha = 0.3f),
                                CircleShape
                            )
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = LocalCustomColors.current.languageSearchCancelIconTintColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(48.dp),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    isLabelVisible = searchText.isEmpty()
                    focusManager.clearFocus()
                }
            ),
            singleLine = true
        )
    }
}