package com.arnyminerz.imbusy.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import coil.compose.AsyncImage
import com.arnyminerz.imbusy.android.activity.IntroActivity
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity.Companion.EXTRA_KEY_CREATED
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity.Companion.EXTRA_KEY_EVENT
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity.Companion.EXTRA_KEY_INVALID
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity.Companion.RESULT_CODE_CANCEL
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity.Companion.RESULT_CODE_INVALID
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity.Companion.RESULT_CODE_SELECTION
import com.arnyminerz.imbusy.android.data.EventData
import com.arnyminerz.imbusy.android.pref.Keys
import com.arnyminerz.imbusy.android.ui.components.calendar.DayView
import com.arnyminerz.imbusy.android.ui.components.dialog.ProfileDialog
import com.arnyminerz.imbusy.android.ui.theme.ImBusyTheme
import com.arnyminerz.imbusy.android.ui.viewmodel.EventLoader
import com.arnyminerz.imbusy.android.utils.dataStore
import com.arnyminerz.imbusy.android.utils.doAsync
import com.arnyminerz.imbusy.android.utils.format
import com.arnyminerz.imbusy.android.utils.getLegacyParcelableExtra
import com.arnyminerz.imbusy.android.utils.restart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import io.github.boguszpawlowski.composecalendar.selection.SelectionMode
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    private var firebaseUser: MutableState<FirebaseUser?> = mutableStateOf(null)

    private val viewModel: EventLoader by viewModels { EventLoader.Factory(application) }

    private val eventSelectionCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val invalid = data?.getStringExtra(EXTRA_KEY_INVALID)
        val eventId = data?.getStringExtra(EXTRA_KEY_EVENT)
        val createdEvent = data?.getLegacyParcelableExtra<EventData>(EXTRA_KEY_CREATED)

        if (eventId != null)
            doAsync {
                Timber.i("Setting event id preference...")
                dataStore.edit {
                    it[Keys.SelectedEvent] = eventId
                }
            }

        when (result.resultCode) {
            RESULT_CODE_SELECTION -> {
                if (createdEvent != null) {
                    Timber.i("Created new event, storing in view model...")
                    viewModel.notifyEventCreation(createdEvent)
                }

                Timber.i("Selected event with id: $eventId")
                viewModel.select(eventId)
            }
            RESULT_CODE_CANCEL -> Timber.i("Selection cancelled")
            RESULT_CODE_INVALID -> Timber.e("Request invalid: $invalid")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        setContent {
            ImBusyTheme {
                val user by firebaseUser

                val scope = rememberCoroutineScope()
                var showingProfileDialog by remember { mutableStateOf(false) }

                val viewModelError by viewModel.error
                val loading by viewModel.loading
                val creatorEvents by viewModel.creatorEvents
                val memberEvents by viewModel.memberEvents
                val selectedEvent by viewModel.selectedEvent

                if (showingProfileDialog)
                    ProfileDialog(
                        user!!,
                        { showingProfileDialog = false },
                        {
                            auth.signOut()
                            restart()
                        },
                    )

                val calendarState = rememberSelectableCalendarState()
                calendarState.selectionState.selectionMode = SelectionMode.Period

                val bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
                val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
                    bottomSheetState = bottomSheetState,
                )

                LaunchedEffect(Unit) {
                    snapshotFlow { bottomSheetState.currentValue }
                        .collect {
                            if (it == BottomSheetValue.Collapsed)
                                calendarState.selectionState.selection = emptyList()
                            else {
                                // If the bottom sheet is being shown, but there's no selected
                                // event, close it, selection is not valid
                                if (selectedEvent == null)
                                    bottomSheetState.collapse()
                            }
                        }
                }

                BottomSheetScaffold(
                    sheetBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    sheetContent = {
                        if (selectedEvent == null)
                            return@BottomSheetScaffold

                        Row {
                            @Composable
                            fun NavItem(
                                icon: ImageVector,
                                imageDescription: String,
                                label: String,
                                canBeDisabled: Boolean = true,
                                onClick: () -> Unit,
                            ) {
                                val enabled =
                                    !canBeDisabled || calendarState.selectionState.selection.size >= 2

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

                            NavItem(
                                Icons.Rounded.EventBusy,
                                stringResource(R.string.image_desc_mark_busy),
                                stringResource(R.string.action_mark_busy),
                            ) { }
                            NavItem(
                                Icons.Rounded.EventAvailable,
                                stringResource(R.string.image_desc_mark_available),
                                stringResource(R.string.action_mark_available),
                            ) { }
                            NavItem(
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

                        Text(
                            stringResource(R.string.event_field_date_start)
                                .format(selectedEvent?.startDate?.format("yyyy-MM-dd"))
                        )
                        Text(
                            stringResource(R.string.event_field_date_end)
                                .format(selectedEvent?.endDate?.format("yyyy-MM-dd"))
                        )
                    },
                    sheetGesturesEnabled = true,
                    sheetShape = RoundedCornerShape(8.dp),
                    sheetPeekHeight = 0.dp,
                    scaffoldState = bottomSheetScaffoldState,
                ) {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(text = stringResource(R.string.app_name))
                                },
                                navigationIcon = {
                                    AnimatedVisibility(viewModelError == null && !loading) {
                                        IconButton(
                                            onClick = {
                                                chooseEvent(user, memberEvents + creatorEvents)
                                            },
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
                                modifier = Modifier
                                    .padding(paddingValues),
                            ) {
                                AnimatedVisibility(loading) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    )
                                }

                                AnimatedVisibility(viewModelError != null) {
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
                                                .format(viewModelError),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(8.dp),
                                        )
                                        OutlinedButton(
                                            onClick = { viewModel.loadUserEvents() },
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

                                AnimatedVisibility(
                                    selectedEvent == null && !loading && viewModelError == null,
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
                                                    onClick = {
                                                        chooseEvent(
                                                            user,
                                                            memberEvents + creatorEvents
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(end = 4.dp)
                                                ) {
                                                    Text(
                                                        stringResource(R.string.action_choose)
                                                    )
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        chooseEvent(
                                                            user,
                                                            memberEvents + creatorEvents,
                                                            startCreating = true,
                                                        )
                                                    },
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

                                AnimatedVisibility(selectedEvent != null && viewModelError == null) {
                                    SelectableCalendar(
                                        calendarState = calendarState,
                                        dayContent = {
                                            DayView(it) {
                                                scope.launch {
                                                    bottomSheetScaffoldState.bottomSheetState.expand()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    )
                }

                viewModel.loadUserEvents()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        firebaseUser.value = auth.currentUser
        if (auth.currentUser == null)
            startActivity(Intent(this, IntroActivity::class.java))
    }

    private fun chooseEvent(
        user: FirebaseUser?,
        events: List<EventData>,
        startCreating: Boolean = false,
    ) {
        eventSelectionCallback.launch(
            Intent(this, EventSelectorActivity::class.java)
                .putExtra(
                    EventSelectorActivity.EXTRA_KEY_USER,
                    user?.uid,
                )
                .putExtra(
                    EventSelectorActivity.EXTRA_KEY_EVENTS,
                    events.toTypedArray()
                )
                .putExtra(
                    EventSelectorActivity.EXTRA_KEY_CREATING,
                    startCreating,
                )
        )
    }
}
