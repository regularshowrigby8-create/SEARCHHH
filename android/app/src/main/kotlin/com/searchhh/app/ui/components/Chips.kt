package com.searchhh.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.searchhh.app.data.model.CategoryCount
import com.searchhh.app.data.model.SortOption

/** Horizontally scrolling category facets with counts. */
@Composable
fun CategoryChipRow(
    categories: List<CategoryCount>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        item(key = "all") {
            FacetChip(
                label = "All",
                selected = selected.isNullOrBlank(),
                onClick = { onSelect(null) },
            )
        }
        items(categories, key = { it.name }) { category ->
            FacetChip(
                label = "${category.name} · ${category.count}",
                selected = selected == category.name,
                onClick = { onSelect(if (selected == category.name) null else category.name) },
            )
        }
    }
}

/** Sort options for the results screen. */
@Composable
fun SortChipRow(
    selected: SortOption,
    onSelect: (SortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(SortOption.entries, key = { it.name }) { option ->
            FacetChip(
                label = option.label,
                selected = option == selected,
                onClick = { onSelect(option) },
            )
        }
    }
}

/** Query chips (recent + server suggestions). */
@Composable
fun QueryChipRow(
    queries: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (queries.isEmpty()) return
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(queries, key = { it }) { query ->
            FacetChip(label = query, selected = false, onClick = { onSelect(query) })
        }
    }
}

@Composable
private fun FacetChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        shape = MaterialTheme.shapes.large,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}
