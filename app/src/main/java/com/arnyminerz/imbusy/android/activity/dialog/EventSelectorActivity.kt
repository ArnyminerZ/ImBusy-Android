package com.arnyminerz.imbusy.android.activity.dialog

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.BuildCompat
import com.arnyminerz.imbusy.android.R
import com.arnyminerz.imbusy.android.data.EventData
import com.arnyminerz.imbusy.android.ui.components.EventCard
import com.arnyminerz.imbusy.android.ui.theme.ImBusyTheme
import com.arnyminerz.imbusy.android.ui.viewmodel.EventCreator
import com.arnyminerz.imbusy.android.utils.assertStates
import com.arnyminerz.imbusy.android.utils.format
import com.arnyminerz.imbusy.android.utils.ifElse
import com.arnyminerz.imbusy.android.utils.now
import com.arnyminerz.imbusy.android.utils.updateDate
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Param.CONTENT_TYPE
import com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.Date

class EventSelectorActivity : AppCompatActivity() {
    companion object {
        const val RESULT_CODE_SELECTION = 0
        const val RESULT_CODE_CANCEL = 1
        const val RESULT_CODE_INVALID = 2

        const val EXTRA_KEY_EVENTS = "events"
        const val EXTRA_KEY_USER = "user"
        const val EXTRA_KEY_CREATING = "creating"

        const val EXTRA_KEY_INVALID = "AttrInvalid"
        const val EXTRA_KEY_EVENT = "EventId"
        const val EXTRA_KEY_CREATED = "EventCreated"
    }

    private val analytics = Firebase.analytics

    private val viewModel by viewModels<EventCreator> { EventCreator.Factory() }

    @SuppressLint("UnsafeOptInUsageError")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras ?: run {
            setResultAndClose(
                RESULT_CODE_INVALID,
                invalidCode = "extras_empty",
            )
            return
        }
        val creating = extras.getBoolean(EXTRA_KEY_CREATING, false)
        val userUid = extras.getString(EXTRA_KEY_USER) ?: run {
            setResultAndClose(
                RESULT_CODE_INVALID,
                invalidCode = "user_empty",
            )
            return
        }
        val eventsExtra =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelableArray(EXTRA_KEY_EVENTS, EventData::class.java)
            } else {
                @Suppress("DEPRECATION")
                extras.getParcelableArray(EXTRA_KEY_EVENTS)
            }?.map { it as EventData }
                ?: run {
                    setResultAndClose(
                        RESULT_CODE_INVALID,
                        invalidCode = "events_empty",
                    )
                    return
                }

        val calendar = Calendar.getInstance()
        val nowYear = calendar.get(Calendar.YEAR)
        val nowMonth = calendar.get(Calendar.MONTH)
        val nowDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, _, _, _ -> },
            nowYear,
            nowMonth,
            nowDay,
        )

        setContent {
            ImBusyTheme {
                val scope = rememberCoroutineScope()
                val pagerState = rememberPagerState(creating.ifElse(1, 0))
                val snackbarHostState = remember { SnackbarHostState() }

                val createEventName = remember { mutableStateOf("") }
                val createEventStart = remember { mutableStateOf<Date?>(null) }
                val createEventEnd = remember { mutableStateOf<Date?>(null) }

                fun onBackPressedCallback() {
                    if (pagerState.currentPage == 0)
                        setResultAndClose(RESULT_CODE_CANCEL)
                    else scope.launch {
                        createEventName.value = ""
                        createEventStart.value = null
                        createEventEnd.value = null

                        pagerState.animateScrollToPage(0)
                    }
                }

                // Add the back listener
                if (BuildCompat.isAtLeastT())
                    onBackInvokedDispatcher.registerOnBackInvokedCallback(
                        OnBackInvokedDispatcher.PRIORITY_DEFAULT
                    ) { onBackPressedCallback() }
                else
                    onBackPressedDispatcher.addCallback(
                        this, // lifecycle owner
                        object : OnBackPressedCallback(true) {
                            override fun handleOnBackPressed() {
                                onBackPressedCallback()
                            }
                        },
                    )

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        SmallTopAppBar(
                            title = {
                                Text(
                                    stringResource(
                                        if (pagerState.currentPage == 0)
                                            R.string.events_list_title
                                        else
                                            R.string.events_creator_title
                                    ),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { onBackPressedCallback() }) {
                                    if (pagerState.currentPage == 0)
                                        Icon(
                                            Icons.Rounded.Close,
                                            stringResource(R.string.image_desc_close_dialog),
                                        )
                                    else
                                        Icon(
                                            Icons.Rounded.ChevronLeft,
                                            stringResource(R.string.image_desc_cancel_creation),
                                        )
                                }
                            },
                            actions = {
                                AnimatedVisibility(pagerState.currentPage == 0) {
                                    IconButton(
                                        onClick = {
                                            scope.launch { pagerState.animateScrollToPage(1) }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Rounded.Add,
                                            stringResource(R.string.image_desc_create_event),
                                        )
                                    }
                                }
                            },
                        )
                    },
                    floatingActionButton = {
                        AnimatedVisibility(pagerState.currentPage == 1) {
                            ExtendedFloatingActionButton(
                                text = { Text(stringResource(R.string.action_create)) },
                                icon = {
                                    Icon(
                                        Icons.Rounded.Add,
                                        stringResource(R.string.fab_desc_create_event),
                                    )
                                },
                                expanded = listOf(createEventName, createEventStart, createEventEnd)
                                    .assertStates(),
                                onClick = {
                                    if (listOf(createEventName, createEventStart, createEventEnd)
                                            .assertStates()
                                    )
                                        viewModel.createEvent(
                                            creator = userUid,
                                            name = createEventName.value,
                                            startDate = createEventStart.value!!,
                                            endDate = createEventEnd.value!!,
                                        ) { eventData ->
                                            setResultAndClose(
                                                RESULT_CODE_SELECTION,
                                                eventId = eventData.id,
                                                createdEvent = eventData,
                                            )
                                        }
                                    else scope.launch {
                                        snackbarHostState.showSnackbar(
                                            getString(R.string.snackbar_error_event_create_empty)
                                        )
                                    }
                                },
                            )
                        }
                    },
                    content = { paddingValues ->
                        HorizontalPager(
                            count = 2,
                            state = pagerState,
                            userScrollEnabled = false,
                            modifier = Modifier
                                .padding(paddingValues),
                        ) { page ->
                            when (page) {
                                0 -> EventsList(eventsExtra, userUid)
                                1 -> EventCreator(
                                    datePickerDialog,
                                    createEventName,
                                    createEventStart,
                                    createEventEnd,
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    private fun setResultAndClose(
        resultCode: Int,
        invalidCode: String? = null,
        eventId: String? = null,
        createdEvent: EventData? = null,
    ) {
        if (invalidCode == null && eventId == null)
            setResult(resultCode)
        else
            setResult(
                resultCode,
                Intent()
                    .putExtra(EXTRA_KEY_INVALID, invalidCode)
                    .putExtra(EXTRA_KEY_EVENT, eventId)
                    .putExtra(EXTRA_KEY_CREATED, createdEvent)
            )
        finish()
    }

    @Composable
    @ExperimentalMaterial3Api
    fun EventsList(
        eventsExtra: List<EventData>,
        userUid: String,
    ) {
        val now = now()

        var creatorFilter by remember { mutableStateOf(true) }
        var memberFilter by remember { mutableStateOf(true) }
        var pastFilter by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    FilterChip(
                        selected = creatorFilter,
                        onClick = { creatorFilter = !creatorFilter },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        label = {
                            Text(stringResource(R.string.events_filter_creator))
                        },
                    )
                    FilterChip(
                        selected = memberFilter,
                        onClick = { memberFilter = !memberFilter },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        label = {
                            Text(stringResource(R.string.events_filter_members))
                        },
                    )
                    FilterChip(
                        selected = pastFilter,
                        onClick = { pastFilter = !pastFilter },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        label = {
                            Text(stringResource(R.string.events_filter_past))
                        },
                    )
                }
            }

            val events = eventsExtra
                .filter { it.creator != userUid || creatorFilter }
                .filter { !it.members.contains(userUid) || memberFilter }
                .filter { it.endDate > now || pastFilter }

            items(
                events
            ) {
                EventCard(
                    userUid,
                    it,
                    {
                        // TODO: Event Editing
                    }
                ) {
                    analytics.logEvent(
                        FirebaseAnalytics.Event.SELECT_CONTENT,
                        Bundle()
                            .apply {
                                putString(CONTENT_TYPE, "event")
                                putString(ITEM_ID, it.id)
                            }
                    )
                    setResultAndClose(
                        RESULT_CODE_SELECTION,
                        eventId = it.id,
                    )
                }
            }
            item {
                AnimatedVisibility(events.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            stringResource(R.string.events_list_empty),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun EventCreator(
        datePickerDialog: DatePickerDialog,
        eventNameState: MutableState<String>,
        eventStartDateState: MutableState<Date?>,
        eventEndDateState: MutableState<Date?>,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            var eventName by eventNameState
            var startDate by eventStartDateState
            var endDate by eventEndDateState

            val startDateFocusRequester = remember { FocusRequester() }

            OutlinedTextField(
                value = eventName,
                onValueChange = { eventName = it },
                label = {
                    Text(stringResource(R.string.event_creator_label_name))
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Title,
                        contentDescription = stringResource(R.string.image_desc_event_creator_name),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { startDateFocusRequester.requestFocus() },
                ),
            )
            DateField(
                datePickerDialog,
                date = startDate,
                minDate = now(),
                maxDate = endDate,
                labelRes = R.string.event_creator_label_start,
                leadingIcon = R.drawable.calendar_start,
                leadingIconDescription = R.string.image_desc_event_start,
                onDateSet = { startDate = it },
            )
            DateField(
                datePickerDialog,
                date = endDate,
                minDate = startDate ?: now(),
                maxDate = null,
                labelRes = R.string.event_creator_label_end,
                leadingIcon = R.drawable.calendar_end,
                leadingIconDescription = R.string.image_desc_event_end,
                onDateSet = { endDate = it },
            )

            // TODO: Add member invitation field
        }
    }

    @Composable
    fun DateField(
        datePickerDialog: DatePickerDialog,
        date: Date?,
        minDate: Date = now(),
        maxDate: Date?,
        @StringRes labelRes: Int,
        @DrawableRes leadingIcon: Int,
        @StringRes leadingIconDescription: Int,
        onDateSet: (date: Date) -> Unit,
    ) {
        val focusManager = LocalFocusManager.current
        val now = Calendar.getInstance()

        OutlinedTextField(
            value = date?.format("yyyy-MM-dd") ?: "----/--/--",
            onValueChange = { },
            label = {
                Text(stringResource(labelRes))
            },
            readOnly = true,
            leadingIcon = {
                Icon(
                    painterResource(leadingIcon),
                    contentDescription = stringResource(leadingIconDescription),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        datePickerDialog.updateDate(now)
                        datePickerDialog.datePicker.minDate = minDate.time
                        datePickerDialog.datePicker.maxDate = maxDate?.time ?: Long.MAX_VALUE
                        datePickerDialog.setOnCancelListener { focusManager.clearFocus() }
                        datePickerDialog.setOnDateSetListener { _, year, month, day ->
                            Timber.d("Selected date: $year/$month/$day")

                            focusManager.clearFocus()

                            onDateSet(
                                Calendar
                                    .getInstance()
                                    .apply {
                                        set(year, month, day)
                                    }
                                    .time
                            )
                        }
                        datePickerDialog.show()
                    }
                },
        )
    }
}