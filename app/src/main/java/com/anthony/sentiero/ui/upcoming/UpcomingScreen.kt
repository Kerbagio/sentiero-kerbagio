// UI layer (screen): draws the list of upcoming hikes from UpcomingViewModel's state.
// It holds no data of its own; taps are passed up as callbacks to the NavHost.
package com.anthony.sentiero.ui.upcoming

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anthony.sentiero.ui.RelativeDay
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.anthony.sentiero.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(
    viewModel: UpcomingViewModel,
    onNewHike: () -> Unit,
    onOpenHike: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.app_full_name),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(stringResource(R.string.upcoming_title), style = MaterialTheme.typography.headlineSmall)
                    }
                },
            )
        },
        floatingActionButton = {
            if (state is UpcomingUiState.Content) {
                ExtendedFloatingActionButton(
                    onClick = onNewHike,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.new_hike)) },
                    // DESIGN.md: the FAB is the one filled primary action (Material defaults to primaryContainer).
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
    ) { padding ->
        when (val s = state) {
            UpcomingUiState.Loading -> Unit // draw nothing for the split second Room needs

            UpcomingUiState.Empty -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.upcoming_empty), style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onNewHike, shape = MaterialTheme.shapes.medium, modifier = Modifier.padding(top = 16.dp)) {
                    Text(stringResource(R.string.new_hike))
                }
            }

            is UpcomingUiState.Content -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 88.dp, // room for the FAB
                ),
            ) {
                items(s.rows, key = { it.id }) { row ->
                    HikeRow(row, onClick = { onOpenHike(row.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun HikeRow(row: UpcomingRow, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            row.routeName,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            stringResource(R.string.upcoming_row_meta, row.difficulty, row.dateLabel, relativeText(row.relative)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(
            progress = { if (row.total == 0) 0f else row.packed.toFloat() / row.total },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Text(
            stringResource(R.string.packed_count, row.packed, row.total),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

// Words for each RelativeDay, from strings.xml.
@Composable
private fun relativeText(relative: RelativeDay): String = when (relative) {
    RelativeDay.Today -> stringResource(R.string.relative_today)
    RelativeDay.Tomorrow -> stringResource(R.string.relative_tomorrow)
    is RelativeDay.InDays -> pluralStringResource(R.plurals.relative_in_days, relative.days, relative.days)
    RelativeDay.Passed -> stringResource(R.string.relative_passed)
}
