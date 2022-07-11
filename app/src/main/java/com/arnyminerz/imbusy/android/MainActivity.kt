package com.arnyminerz.imbusy.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.arnyminerz.imbusy.android.activity.IntroActivity
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity.Companion.EXTRA_KEY_CREATED
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity.Companion.EXTRA_KEY_EVENT
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity.Companion.EXTRA_KEY_INVALID
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity.Companion.RESULT_CODE_CANCEL
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity.Companion.RESULT_CODE_INVALID
import com.arnyminerz.imbusy.android.activity.dialog.EventSelectorActivity.Companion.RESULT_CODE_SELECTION
import com.arnyminerz.imbusy.android.data.EventData
import com.arnyminerz.imbusy.android.data.UserData
import com.arnyminerz.imbusy.android.pref.Keys
import com.arnyminerz.imbusy.android.ui.components.DatesSelectionBottomSheet
import com.arnyminerz.imbusy.android.ui.components.bar.MainTopBar
import com.arnyminerz.imbusy.android.ui.components.calendar.DayView
import com.arnyminerz.imbusy.android.ui.components.card.EventLoadErrorCard
import com.arnyminerz.imbusy.android.ui.components.card.NoEventSelectedCard
import com.arnyminerz.imbusy.android.ui.components.dialog.ProfileDialog
import com.arnyminerz.imbusy.android.ui.theme.ImBusyTheme
import com.arnyminerz.imbusy.android.ui.viewmodel.EventLoader
import com.arnyminerz.imbusy.android.utils.dataStore
import com.arnyminerz.imbusy.android.utils.doAsync
import com.arnyminerz.imbusy.android.utils.getLegacyParcelableExtra
import com.arnyminerz.imbusy.android.utils.restart
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.placeholder.placeholder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import io.github.boguszpawlowski.composecalendar.CalendarState
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import io.github.boguszpawlowski.composecalendar.selection.SelectionMode
import kotlinx.coroutines.flow.distinctUntilChanged
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

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
        ExperimentalPagerApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        if (auth.currentUser == null)
            return

        setContent {
            ImBusyTheme {
                val user by firebaseUser

                var showingProfileDialog by remember { mutableStateOf(false) }
                var tabIndex by remember { mutableStateOf(0) }

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

                val bottomSheetMorningEnabled = remember { mutableStateOf(true) }
                val bottomSheetAfternoonEnabled = remember { mutableStateOf(true) }

                val scope = rememberCoroutineScope()
                val bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
                val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
                    bottomSheetState = bottomSheetState,
                )
                val pagerState = rememberPagerState()

                LaunchedEffect(Unit) {
                    snapshotFlow { bottomSheetState.currentValue }
                        .collect {
                            if (it == BottomSheetValue.Collapsed) {
                                // When collapsing, clear all selection
                                calendarState.selectionState.selection = emptyList()
                                bottomSheetMorningEnabled.value = true
                                bottomSheetAfternoonEnabled.value = true
                            } else {
                                // If the bottom sheet is being shown, but there's no selected
                                // event, close it, selection is not valid
                                if (selectedEvent == null)
                                    bottomSheetState.collapse()
                            }
                        }
                }
                LaunchedEffect(Unit) {
                    snapshotFlow { pagerState.currentPage }
                        .distinctUntilChanged()
                        .collect { page -> tabIndex = page }
                }

                BottomSheetScaffold(
                    sheetBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    sheetContent = {
                        DatesSelectionBottomSheet(
                            calendarState,
                            bottomSheetState,
                            selectedEvent,
                            bottomSheetMorningEnabled,
                            bottomSheetAfternoonEnabled,
                        )
                    },
                    sheetGesturesEnabled = true,
                    sheetShape = RoundedCornerShape(8.dp),
                    sheetPeekHeight = 0.dp,
                    scaffoldState = bottomSheetScaffoldState,
                ) {
                    Scaffold(
                        topBar = {
                            MainTopBar(
                                viewModelError,
                                loading,
                                user,
                                onEventChosen = { chooseEvent(user, memberEvents + creatorEvents) },
                                onProfileDialogShowRequested = { showingProfileDialog = true }
                            )
                        },
                        bottomBar = {
                            if (viewModelError == null && selectedEvent != null) {
                                BottomAppBar {
                                    listOf(
                                        R.string.event_toolbar_calendar to Icons.Rounded.Event,
                                        R.string.event_toolbar_summary to Icons.Rounded.Article,
                                        R.string.event_toolbar_members to Icons.Rounded.People,
                                        R.string.event_toolbar_properties to Icons.Rounded.Tune,
                                    ).forEachIndexed { index, (labelRes, icon) ->
                                        val label = stringResource(labelRes)
                                        NavigationBarItem(
                                            selected = tabIndex == index,
                                            onClick = {
                                                tabIndex = index
                                                scope.launch { pagerState.animateScrollToPage(index) }
                                            },
                                            label = { Text(label) },
                                            icon = { Icon(icon, label) },
                                        )
                                    }
                                }
                            }
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

                                EventLoadErrorCard(viewModelError) { viewModel.loadUserEvents() }

                                NoEventSelectedCard(
                                    selectedEvent == null && !loading && viewModelError == null,
                                ) { shouldCreate ->
                                    chooseEvent(
                                        user,
                                        memberEvents + creatorEvents,
                                        startCreating = shouldCreate,
                                    )
                                }

                                doAsync {
                                    UserData.bulkGet(Firebase.functions, listOf(user!!.uid))
                                        .forEach {
                                            Timber.i("User: $it")
                                        }
                                }

                                AnimatedVisibility(selectedEvent != null && viewModelError == null) {
                                    MainPager(
                                        pagerState,
                                        calendarState,
                                        bottomSheetScaffoldState,
                                        listOf(calendarView, placeholder, membersList, placeholder),
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

    @Composable
    @ExperimentalPagerApi
    @ExperimentalMaterialApi
    @ExperimentalMaterial3Api
    fun MainPager(
        pagerState: PagerState,
        calendarState: CalendarState<DynamicSelectionState>,
        bottomSheetScaffoldState: BottomSheetScaffoldState,
        pages: List<@Composable (
            calendarState: CalendarState<DynamicSelectionState>,
            bottomSheetScaffoldState: BottomSheetScaffoldState,
        ) -> Unit>,
    ) {
        HorizontalPager(count = pages.size, state = pagerState) { page ->
            pages[page].invoke(calendarState, bottomSheetScaffoldState)
        }
    }

    @ExperimentalMaterialApi
    @ExperimentalMaterial3Api
    private val calendarView = @Composable { calendarState: CalendarState<DynamicSelectionState>,
                                             bottomSheetScaffoldState: BottomSheetScaffoldState ->
        val scope = rememberCoroutineScope()

        SelectableCalendar(
            calendarState = calendarState,
            modifier = Modifier
                .padding(horizontal = 8.dp),
            dayContent = {
                DayView(it) {
                    scope.launch {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    }
                }
            }
        )
    }

    @ExperimentalMaterialApi
    @ExperimentalMaterial3Api
    private val membersList = @Composable { _: CalendarState<DynamicSelectionState>,
                                            _: BottomSheetScaffoldState ->
        val peopleData by viewModel.peopleData

        LazyColumn {
            // Creator card
            item {
                Card(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        Text(
                            peopleData?.creator?.displayName ?: "Loading...",
                            modifier = Modifier
                                .weight(1f)
                                .placeholder(
                                    visible = peopleData?.creator == null,
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                        )
                        Badge {
                            Text("Creator")
                        }
                    }
                }
            }

            items(viewModel.selectedEvent.value!!.members.size) { index ->
                Card(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        Text(
                            peopleData?.membersData?.getOrNull(index)?.displayName ?: "Loading...",
                            modifier = Modifier
                                .weight(1f)
                                .placeholder(
                                    visible = peopleData?.membersData == null,
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                        )
                    }
                }
            }
        }

        viewModel.loadUserDataFromSelectedEvent()
    }

    @ExperimentalMaterialApi
    private val placeholder = @Composable { _: CalendarState<DynamicSelectionState>,
                                            _: BottomSheetScaffoldState ->
        Text("This is a placeholder")
    }
}
