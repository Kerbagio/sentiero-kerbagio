// UI layer (screen): the saved hike and its packing checklist. It only draws
// HikeDetailViewModel's state and reports ticks, adds and deletes; it never writes to the database itself.
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anthony.sentiero.R
import com.anthony.sentiero.domain.PackingItem
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.union
import androidx.compose.material3.ScaffoldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HikeDetailScreen(
    viewModel: HikeDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val addText by viewModel.addText.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Each event from the ViewModel becomes one snackbar.
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HikeDetailEvent.Deleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.item_deleted, event.item.label),
                        actionLabel = context.getString(R.string.undo),
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.onUndoDelete(event.item)
                }
                HikeDetailEvent.SaveFailed ->
                    snackbarHostState.showSnackbar(context.getString(R.string.save_failed))
            }
        }
    }

    Scaffold(
        // Also leave room for the keyboard, so the "+ Add item" field at the bottom stays visible.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        if (s.total == 0) {
                            // Zero items (DR-2B): no "0 of 0" header, just an invitation to add.
                            Text(
                                stringResource(R.string.list_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                            )
                        } else {
                            Text(
                                stringResource(R.string.packing_progress, s.packed, s.total),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 24.dp),
                            )
                            LinearProgressIndicator(
                                progress = { s.packed.toFloat() / s.total },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            )
                        }
                    }
                }
                items(s.items, key = { it.id }) { item ->
                    PackingRow(
                        item,
                        onTick = { checked -> viewModel.onTick(item.id, checked) },
                        onDelete = { viewModel.onDelete(item.id) },
                    )
                }
                // A fixed key keeps the field (and its focus) when rows are added above it.
                item(key = "add-item") {
                    AddItemField(
                        text = addText,
                        onTextChange = viewModel::onAddTextChange,
                        onSubmit = viewModel::onAddSubmit,
                    )
                }
            }
        }
    }
}

/**
 * The checkbox and label are one toggle (easy to tap, read by TalkBack as one element);
 * the ✕ beside it is its own 48dp button.
 */
@Composable
private fun PackingRow(item: PackingItem, onTick: (Boolean) -> Unit, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp)
                .toggleable(value = item.isPacked, role = Role.Checkbox, onValueChange = onTick),
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
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.delete_item, item.label),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** "+ Add item": Done on the keyboard or the + button adds; the keyboard stays open for the next one. */
@Composable
private fun AddItemField(text: String, onTextChange: (String) -> Unit, onSubmit: () -> Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        placeholder = { Text(stringResource(R.string.add_item_placeholder)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        trailingIcon = {
            IconButton(onClick = onSubmit) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_item))
            }
        },
        supportingText = if (text.length >= HikeDetailViewModel.SHOW_COUNTER_FROM) {
            { Text(stringResource(R.string.char_counter, text.length, HikeDetailViewModel.MAX_ITEM_LABEL)) }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
