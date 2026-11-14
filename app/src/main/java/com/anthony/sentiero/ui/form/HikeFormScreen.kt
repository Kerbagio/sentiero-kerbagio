// UI layer (screen): the New hike form. It shows HikeFormViewModel's state and sends
// every change back to it; the only state kept here is whether the date dialog is open.
package com.anthony.sentiero.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anthony.sentiero.domain.Difficulty
import com.anthony.sentiero.domain.label
import com.anthony.sentiero.domain.templateFor
import com.anthony.sentiero.ui.shortLabel
import java.time.Instant
import java.time.ZoneOffset
import androidx.compose.ui.res.stringResource
import com.anthony.sentiero.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HikeFormScreen(
    viewModel: HikeFormViewModel,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val saveFailedText = stringResource(R.string.save_failed)

    // Show "Couldn't save" once, then tell the ViewModel it was seen.
    LaunchedEffect(state.saveFailed) {
        if (state.saveFailed) {
            snackbarHostState.showSnackbar(saveFailedText)
            viewModel.onSaveErrorShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_hike)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.routeName,
                onValueChange = viewModel::onRouteNameChange,
                label = { Text(stringResource(R.string.route_name)) },
                singleLine = true,
                supportingText = if (state.routeName.length >= HikeFormViewModel.SHOW_COUNTER_FROM) {
                    { Text(stringResource(R.string.char_counter, state.routeName.length, HikeFormViewModel.MAX_ROUTE_NAME)) }
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // A read-only field that opens the date dialog when tapped.
            Box {
                OutlinedTextField(
                    value = state.date?.shortLabel() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.date)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.matchParentSize(),
                ) {}
            }

            Text(stringResource(R.string.difficulty), style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                Difficulty.entries.forEachIndexed { index, difficulty ->
                    SegmentedButton(
                        selected = state.difficulty == difficulty,
                        onClick = { viewModel.onDifficultyPicked(difficulty) },
                        shape = SegmentedButtonDefaults.itemShape(index, Difficulty.entries.size),
                    ) { Text(difficulty.label) }
                }
            }

            Text(stringResource(R.string.list_starts_from), style = MaterialTheme.typography.labelLarge)
            val count = state.difficulty?.let { templateFor(it).size }
            FilterChip(
                selected = true,
                onClick = {},
                label = {
                    Text(
                        if (count == null) stringResource(R.string.template_basics)
                        else stringResource(R.string.template_basics_count, count)
                    )
                },
            )

            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = state.canSave,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.save_hike)) }
        }
    }

    if (showDatePicker) {
        // The picker works in UTC milliseconds, so convert with UTC (never the phone's zone),
        // otherwise the saved day can be off by one.
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        viewModel.onDatePicked(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
