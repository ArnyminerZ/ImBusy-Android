package com.arnyminerz.imbusy.android.ui.components.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.arnyminerz.imbusy.android.R
import com.google.firebase.auth.FirebaseUser

@Composable
fun MainTopBar(
    viewModelError: String?,
    loading: Boolean,
    user: FirebaseUser?,
    onEventChosen: () -> Unit,
    onProfileDialogShowRequested: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(text = stringResource(R.string.app_name))
        },
        navigationIcon = {
            AnimatedVisibility(viewModelError == null && !loading) {
                IconButton(
                    onClick = onEventChosen,
                ) {
                    Icon(
                        Icons.Rounded.ListAlt,
                        stringResource(R.string.image_desc_events_list),
                    )
                }
            }
        },
        actions = {
            if (user != null)
                IconButton(onClick = onProfileDialogShowRequested) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = stringResource(R.string.fab_desc_profile_image),
                    )
                }
        }
    )
}
