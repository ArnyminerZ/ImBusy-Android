package com.arnyminerz.imbusy.android.ui.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.arnyminerz.imbusy.android.R

@Composable
fun SignOutDialog(onDismissRequest: () -> Unit, onSignOutRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(stringResource(R.string.dialog_sign_out_title))
        },
        text = {
            Text(stringResource(R.string.dialog_sign_out_message))
        },
        confirmButton = {
            Button(onClick = onSignOutRequest) {
                Text(text = stringResource(R.string.action_sign_out))
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.action_close))
            }
        }
    )
}
