package com.arnyminerz.imbusy.android.ui.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.arnyminerz.imbusy.android.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun ProfileDialog(user: FirebaseUser, onDismissRequest: () -> Unit, onSignOutRequest: () -> Unit) {
    var showingSignOutDialog by remember { mutableStateOf(false) }

    if (showingSignOutDialog)
        SignOutDialog({ showingSignOutDialog = false }, onSignOutRequest)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = { showingSignOutDialog = true }) {
                Text(stringResource(R.string.action_sign_out))
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text(stringResource(R.string.action_close))
            }
        },
        title = {
            Text(user.displayName ?: stringResource(R.string.dialog_profile_title))
        },
        text = {
            Text(
                stringResource(R.string.dialog_profile_message)
                    .format(
                        when (user.providerId) {
                            GoogleAuthProvider.PROVIDER_ID -> "Google"
                            else -> "NaN"
                        }
                    )
            )
        },
    )
}
