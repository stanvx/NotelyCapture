package com.module.notelycompose.notes.ui.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.module.notelycompose.resources.vectors.IcFile
import com.module.notelycompose.resources.vectors.IcStar
import com.module.notelycompose.resources.vectors.IcRecorderSmall
import com.module.notelycompose.resources.vectors.Images
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.date_tab_bar_all
import com.module.notelycompose.resources.date_tab_bar_starred
import com.module.notelycompose.resources.date_tab_bar_voices
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterTabBar(
    onFilterTabItemClicked: (String) -> Unit,
    selectedTabTitle: String,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current
    
    // Optimized filter list for single row - reduced to 3 most important filters
    val filters = listOf(
        FilterData(
            title = stringResource(Res.string.date_tab_bar_all),
            icon = Images.Icons.IcFile
        ),
        FilterData(
            title = stringResource(Res.string.date_tab_bar_starred),
            icon = Images.Icons.IcStar
        ),
        FilterData(
            title = stringResource(Res.string.date_tab_bar_voices),
            icon = Images.Icons.IcRecorderSmall
        )
    )
    
    val selectedTitle = selectedTabTitle.ifEmpty { filters[0].title }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(filters) { index, filter ->
            val isSelected = filter.title == selectedTitle
            
            // Material 3 Motion: Physics-based animations
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.02f else 1f,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 300f
                ),
                label = "chip_scale_$index"
            )
            
            FilterChip(
                selected = isSelected,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFilterTabItemClicked(filter.title)
                    focusManager.clearFocus()
                },
                label = {
                    Text(
                        text = filter.title,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = "${filter.title} filter",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.scale(scale)
            )
        }
    }
}

private data class FilterData(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
