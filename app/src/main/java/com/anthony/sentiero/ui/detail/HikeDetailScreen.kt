// UI layer (screen): the saved hike and its packing checklist. It only draws
// HikeDetailViewModel's state and reports ticks; it never writes to the database itself.
package com.anthony.sentiero.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anthony.sentiero.domain.PackingItem
import androidx.compose.ui.res.stringResource
import com.anthony.sentiero.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HikeDetailScreen(
    viewModel: HikeDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            HikeDetailUiState.Loading -> Unit

            HikeDetailUiState.NotFound -> Column(Modifier.padding(padding).padding(16.dp)) {
                Text(stringResource(R.string.hike_not_found))
                Button(onClick = onBack, shape = MaterialTheme.shapes.medium, modifier = Modifier.padding(top = 16.dp)) { Text(stringResource(R.string.back)) }
            }

            is HikeDetailUiState.Content -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
            ) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(s.title, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            s.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            stringResource(R.string.packing_progress, s.packed, s.total),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 24.dp),
                        )
                        LinearProgressIndicator(
                            progress = { if (s.total == 0) 0f else s.packed.toFloat() / s.total },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        )
                    }
                }
                items(s.items, key = { it.id }) { item ->
                    PackingRow(item, onTick = { checked -> viewModel.onTick(item.id, checked) })
                }
            }
        }
    }
}

/** The whole row is one toggle, so it is easy to tap and TalkBack reads it as one element. */
@Composable
private fun PackingRow(item: PackingItem, onTick: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = item.isPacked, role = Role.Checkbox, onValueChange = onTick)
            .padding(horizontal = 16.dp),
    ) {
        Checkbox(checked = item.isPacked, onCheckedChange = null)
        Text(
            item.label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (item.isPacked) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}
