package com.arnyminerz.imbusy.android.activity.dialog

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.BuildCompat
import com.arnyminerz.imbusy.android.R
import com.arnyminerz.imbusy.android.data.EventData
import com.arnyminerz.imbusy.android.ui.components.EventCard
import com.arnyminerz.imbusy.android.ui.theme.ImBusyTheme
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Param.CONTENT_TYPE
import com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class EventSelectorActivity : AppCompatActivity() {
    companion object {
        const val RESULT_CODE_SELECTION = 0
        const val RESULT_CODE_CANCEL = 1
        const val RESULT_CODE_INVALID = 2

        const val EXTRA_KEY_EVENTS = "events"
        const val EXTRA_KEY_USER = "user"

        const val EXTRA_KEY_INVALID = "AttrInvalid"
        const val EXTRA_KEY_EVENT = "EventId"
    }

    private val analytics = Firebase.analytics

    @SuppressLint("UnsafeOptInUsageError")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val now = Calendar.getInstance().time

        val extras = intent.extras ?: run {
            setResultAndClose(
                RESULT_CODE_INVALID,
                invalidCode = "extras_empty",
            )
            return
        }
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

        setContent {
            ImBusyTheme {
                Scaffold(
                    topBar = {
                        SmallTopAppBar(
                            title = {
                                Text(
                                    stringResource(R.string.events_list_title),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { onBackPressedCallback() }) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        stringResource(R.string.image_desc_close_dialog),
                                    )
                                }
                            }
                        )
                    },
                    content = { paddingValues ->
                        var creatorFilter by remember { mutableStateOf(true) }
                        var memberFilter by remember { mutableStateOf(true) }
                        var pastFilter by remember { mutableStateOf(false) }

                        LazyColumn(
                            modifier = Modifier.padding(paddingValues)
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
                )
            }
        }
    }

    private fun onBackPressedCallback() {
        setResultAndClose(RESULT_CODE_CANCEL)
    }

    private fun setResultAndClose(
        resultCode: Int,
        invalidCode: String? = null,
        eventId: String? = null,
    ) {
        if (invalidCode == null && eventId == null)
            setResult(resultCode)
        else
            setResult(
                resultCode,
                Intent()
                    .putExtra(EXTRA_KEY_INVALID, invalidCode)
                    .putExtra(EXTRA_KEY_EVENT, eventId)
            )
        finish()
    }
}