package com.engblog.android.ui.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.engblog.shared.model.CANDIDATE_INTEREST_TAGS

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestTagChips(
    selectedTags: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(modifier = modifier) {
        CANDIDATE_INTEREST_TAGS.forEach { tag ->
            FilterChip(
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                selected = tag in selectedTags,
                onClick = { onToggle(tag) },
                label = { Text(tag) },
            )
        }
    }
}
