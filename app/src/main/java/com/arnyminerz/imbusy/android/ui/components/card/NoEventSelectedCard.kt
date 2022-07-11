package com.arnyminerz.imbusy.android.ui.components.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnyminerz.imbusy.android.R

@Composable
fun NoEventSelectedCard(
    shouldShow: Boolean,
    onRequestEventChoose: (create: Boolean) -> Unit,
) {
    AnimatedVisibility(
        shouldShow,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(.7f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.event_view_no_selection),
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    OutlinedButton(
                        onClick = { onRequestEventChoose(false) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text(
                            stringResource(R.string.action_choose)
                        )
                    }
                    OutlinedButton(
                        onClick = { onRequestEventChoose(true) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text(
                            stringResource(R.string.action_create)
                        )
                    }
                }
            }
        }
    }
}
