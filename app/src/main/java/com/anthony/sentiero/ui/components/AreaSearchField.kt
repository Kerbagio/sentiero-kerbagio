// UI layer (component): the "Area (optional)" field with its search results. It is stateless:
// it draws an AreaFieldUi and reports taps upward, and AreaSearchState decides what happens.
// The New hike form uses it now and the Add-area sheet (2.4) reuses it.
package com.anthony.sentiero.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.anthony.sentiero.R
import com.anthony.sentiero.domain.Place

@Composable
fun AreaSearchField(
    ui: AreaFieldUi,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onPick: (Place) -> Unit,
    onClear: () -> Unit,
    unpickedHint: String?,
    modifier: Modifier = Modifier,
) {
    when (ui) {
        is AreaFieldUi.Picked -> OutlinedTextField(
            value = ui.area.label,
            onValueChange = {},
            readOnly = true, // to change it: clear, then search again
            label = { Text(stringResource(R.string.area_label)) },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.area_clear))
                }
            },
            modifier = modifier.fillMaxWidth(),
        )

        is AreaFieldUi.Editing -> EditingField(ui, onTextChange, onSubmit, onRetry, onPick, unpickedHint, modifier)
    }
}

@Composable
private fun EditingField(
    ui: AreaFieldUi.Editing,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onPick: (Place) -> Unit,
    unpickedHint: String?,
    modifier: Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val submit = {
        keyboard?.hide() // so the results aren't hidden behind the keyboard
        onSubmit()
    }
    val helper = stringResource(R.string.area_helper)
    // First match wins: the too-short message, the helper on a blank field, else the "not picked" hint.
    val supporting = when {
        ui.tooShort -> stringResource(R.string.area_too_short)
        ui.text.isBlank() -> helper
        else -> unpickedHint ?: helper
    }

    Column(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = ui.text,
            onValueChange = onTextChange,
            label = { Text(stringResource(R.string.area_label)) },
            singleLine = true,
            supportingText = { Text(supporting) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { submit() }),
            trailingIcon = {
                IconButton(onClick = submit) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.area_search))
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        when (val phase = ui.phase) {
            SearchPhase.Idle -> Unit
            SearchPhase.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
            is SearchPhase.Results -> phase.places.forEachIndexed { index, place ->
                if (index > 0) HorizontalDivider()
                ListItem(
                    headlineContent = { Text(place.name) },
                    supportingContent = if (place.subtitle.isNotEmpty()) {
                        { Text(place.subtitle) }
                    } else {
                        null
                    },
                    modifier = Modifier.clickable { onPick(place) }, // the whole row is the target
                )
            }
            is SearchPhase.Empty -> StateText(stringResource(R.string.area_no_match, phase.query))
            SearchPhase.Error -> Row(verticalAlignment = Alignment.CenterVertically) {
                StateText(stringResource(R.string.area_search_failed), Modifier.weight(1f))
                TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
        }
    }
}

@Composable
private fun StateText(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(vertical = 8.dp),
    )
}
