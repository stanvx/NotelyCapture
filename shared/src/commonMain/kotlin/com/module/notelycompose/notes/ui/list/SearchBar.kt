package com.module.notelycompose.notes.ui.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.search_bar_search_description
import com.module.notelycompose.resources.search_bar_search_text
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    onSearchByKeyword: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit = {},
    externalActivation: Boolean = false
) {
    var searchText by remember { mutableStateOf("") }
    var isActive by remember(externalActivation) { mutableStateOf(externalActivation) }

    DockedSearchBar(
        query = searchText,
        onQueryChange = { newText ->
            searchText = newText
            onSearchByKeyword(newText)
        },
        onSearch = { query ->
            onSearchByKeyword(query)
            isActive = false
        },
        active = isActive,
        onActiveChange = { active ->
            isActive = active
            onActiveChange(active)
        },
        placeholder = {
            Text(
                text = stringResource(Res.string.search_bar_search_text),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(Res.string.search_bar_search_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                IconButton(
                    onClick = {
                        searchText = ""
                        onSearchByKeyword("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            dividerColor = MaterialTheme.colorScheme.outline
        ),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Content shown when search bar is active/expanded
        // This could include search suggestions, recent searches, etc.
        // For now, keeping it empty as per the original implementation
    }
}