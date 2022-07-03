package com.arnyminerz.imbusy.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.arnyminerz.imbusy.android.activity.IntroActivity
import com.arnyminerz.imbusy.android.ui.components.dialog.ProfileDialog
import com.arnyminerz.imbusy.android.ui.theme.ImBusyTheme
import com.arnyminerz.imbusy.android.utils.restart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    private var firebaseUser: MutableState<FirebaseUser?> = mutableStateOf(null)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        setContent {
            ImBusyTheme {
                val user by firebaseUser

                var showingProfileDialog by remember { mutableStateOf(false) }

                if (showingProfileDialog)
                    ProfileDialog(
                        user!!,
                        { showingProfileDialog = false },
                        {
                            auth.signOut()
                            restart()
                        },
                    )

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(text = stringResource(R.string.app_name))
                            },
                            actions = {
                                if (user != null)
                                    IconButton(onClick = { showingProfileDialog = true }) {
                                        AsyncImage(
                                            model = user?.photoUrl,
                                            contentDescription = stringResource(R.string.fab_desc_profile_image),
                                        )
                                    }
                            }
                        )
                    },
                    content = { paddingValues ->
                        Column(
                            modifier = Modifier.padding(paddingValues)
                        ) {

                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        firebaseUser.value = auth.currentUser
        if (auth.currentUser == null)
            startActivity(Intent(this, IntroActivity::class.java))
    }
}
