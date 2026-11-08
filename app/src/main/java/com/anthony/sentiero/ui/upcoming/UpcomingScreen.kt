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
                            "Sentiero Kerbagio",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text("Upcoming", style = MaterialTheme.typography.headlineSmall)
                    }
                },
            )
        },
        floatingActionButton = {
            if (state is UpcomingUiState.Content) {
                ExtendedFloatingActionButton(
                    onClick = onNewHike,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("New hike") },
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
                Text("No hikes planned. Plan your next one.", style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onNewHike, modifier = Modifier.padding(top = 16.dp)) {
                    Text("New hike")
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
            "${row.difficulty} · ${row.dateLabel}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(
            progress = { if (row.total == 0) 0f else row.packed.toFloat() / row.total },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Text(
            "${row.packed} of ${row.total} packed",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
