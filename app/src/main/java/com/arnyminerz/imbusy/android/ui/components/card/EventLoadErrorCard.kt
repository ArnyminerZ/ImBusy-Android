package com.arnyminerz.imbusy.android.ui.components.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arnyminerz.imbusy.android.R

@Composable
@ExperimentalMaterial3Api
fun EventLoadErrorCard(error: String?, onLoadRequested: () -> Unit) {
    AnimatedVisibility(error != null) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                stringResource(R.string.error_events_load_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp),
            )
            Text(
                stringResource(R.string.error_events_load_msg)
                    .format(error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(8.dp),
            )
            OutlinedButton(
                onClick = onLoadRequested,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                modifier = Modifier.padding(
                    start = 8.dp,
                    bottom = 8.dp
                ),
            ) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}
