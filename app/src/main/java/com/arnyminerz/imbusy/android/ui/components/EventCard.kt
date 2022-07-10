package com.arnyminerz.imbusy.android.ui.components

import android.icu.util.Calendar
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.arnyminerz.imbusy.android.R
import com.arnyminerz.imbusy.android.data.EventData
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
@ExperimentalMaterial3Api
fun EventCard(
    userUid: String,
    eventData: EventData,
    onEditRequested: () -> Unit,
    onViewRequested: () -> Unit,
) {
    val isCreator = eventData.creator == userUid
    val now = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Calendar.getInstance().time
    } else {
        java.util.Calendar.getInstance().time
    }
    val eventStart = eventData.startDate
    val eventEnd = eventData.endDate

    val pastEvent = eventEnd < now

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .padding(8.dp),
        ) {
            Text(
                eventData.name,
                style = MaterialTheme.typography.titleLarge,
                textDecoration = if (pastEvent) TextDecoration.LineThrough else TextDecoration.None,
                fontStyle = if (pastEvent) FontStyle.Italic else FontStyle.Normal,
                modifier = Modifier
                    .weight(1f),
            )
            Text(
                if (isCreator)
                    stringResource(R.string.event_type_creator)
                else
                    stringResource(R.string.event_type_member),
                fontStyle = if (isCreator) FontStyle.Normal else FontStyle.Italic,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Text(
            stringResource(R.string.event_field_date_start)
                .format(
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(eventStart)
                ),
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .fillMaxWidth(),
        )
        Text(
            stringResource(R.string.event_field_date_end)
                .format(
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(eventEnd)
                ),
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isCreator)
                IconButton(onClick = onEditRequested) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.image_desc_edit_event),
                    )
                }
            OutlinedButton(onClick = onViewRequested) {
                Text(stringResource(R.string.action_view))
            }
        }
    }
}
