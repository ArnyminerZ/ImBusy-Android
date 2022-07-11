package com.arnyminerz.imbusy.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomSheetState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arnyminerz.imbusy.android.R
import com.arnyminerz.imbusy.android.data.EventData
import io.github.boguszpawlowski.composecalendar.CalendarState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import kotlinx.coroutines.launch

@Composable
private fun RowScope.NavItem(
    calendarState: CalendarState<DynamicSelectionState>,
    icon: ImageVector,
    imageDescription: String,
    label: String,
    canBeDisabled: Boolean = true,
    onClick: () -> Unit,
) {
    val enabled = !canBeDisabled || calendarState.selectionState.selection.size >= 2

    Column(
        modifier = Modifier
            .weight(1f)
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clickable(enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompositionLocalProvider(
            LocalContentAlpha provides if (enabled) ContentAlpha.high else ContentAlpha.disabled,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = imageDescription,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
                    .copy(alpha = LocalContentAlpha.current)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
                    .copy(alpha = LocalContentAlpha.current)
            )
        }
    }
}

@Composable
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
fun DatesSelectionBottomSheet(
    calendarState: CalendarState<DynamicSelectionState>,
    bottomSheetState: BottomSheetState,
    selectedEvent: EventData?,
    morningEnabledState: MutableState<Boolean>,
    afternoonEnabledState: MutableState<Boolean>,
) {
    if (selectedEvent == null)
        return

    val scope = rememberCoroutineScope()
    var morningEnabled by morningEnabledState
    var afternoonEnabled by afternoonEnabledState

    Row {
        NavItem(
            calendarState,
            Icons.Rounded.EventBusy,
            stringResource(R.string.image_desc_mark_busy),
            stringResource(R.string.action_mark_busy),
        ) { }
        NavItem(
            calendarState,
            Icons.Rounded.EventAvailable,
            stringResource(R.string.image_desc_mark_available),
            stringResource(R.string.action_mark_available),
        ) { }
        NavItem(
            calendarState,
            Icons.Rounded.Close,
            stringResource(R.string.image_desc_cancel_selection),
            stringResource(R.string.action_cancel_selection),
            canBeDisabled = false,
        ) {
            scope.launch {
                calendarState.selectionState.selection = emptyList()
                bottomSheetState.collapse()
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp),
    ) {
        val chipsEnabled = calendarState.selectionState.selection.size >= 2

        FilterChip(
            selected = morningEnabled,
            onClick = { morningEnabled = !morningEnabled },
            enabled = chipsEnabled,
            label = { Text(stringResource(R.string.event_date_modal_badge_morning)) },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.weather_sunset_up),
                    contentDescription = stringResource(R.string.image_desc_filter_morning),
                )
            },
            selectedIcon = {
                Icon(
                    painterResource(R.drawable.weather_sunset_up),
                    contentDescription = stringResource(R.string.image_desc_filter_morning),
                )
            },
            modifier = Modifier
                .padding(horizontal = 4.dp),
        )
        FilterChip(
            selected = afternoonEnabled,
            onClick = { afternoonEnabled = !afternoonEnabled },
            enabled = chipsEnabled,
            label = { Text(stringResource(R.string.event_date_modal_badge_afternoon)) },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.weather_sunset_down),
                    contentDescription = stringResource(R.string.image_desc_filter_afternoon),
                )
            },
            selectedIcon = {
                Icon(
                    painterResource(R.drawable.weather_sunset_down),
                    contentDescription = stringResource(R.string.image_desc_filter_afternoon),
                )
            },
            modifier = Modifier
                .padding(horizontal = 4.dp),
        )
    }
}
