package com.arnyminerz.imbusy.android.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.arnyminerz.imbusy.android.data.EventData
import com.arnyminerz.imbusy.android.exception.AuthException
import com.arnyminerz.imbusy.android.pref.Keys
import com.arnyminerz.imbusy.android.utils.append
import com.arnyminerz.imbusy.android.utils.dataStore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class EventLoader(application: Application) : AndroidViewModel(application) {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    val loading = mutableStateOf(true)

    val selectedEvent = mutableStateOf<EventData?>(null)

    val memberEvents = mutableStateOf<List<EventData>>(emptyList())
    val creatorEvents = mutableStateOf<List<EventData>>(emptyList())

    val error = mutableStateOf<String?>(null)

    @Throws(AuthException::class)
    fun loadUserEvents() {
        if (memberEvents.value.isNotEmpty() || creatorEvents.value.isNotEmpty())
            return

        val user = auth.currentUser ?: throw AuthException("User not logged in")

        viewModelScope.launch {
            error.value = null
            loading.value = true

            try {
                Timber.i("Loading events where user is creator...")
                creatorEvents.value = db
                    .collection("events")
                    .whereEqualTo("creator", user.uid)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { EventData.build(it) }
                    .also {
                        Timber.d("Got ${it.size} events created by the user.")
                    }

                Timber.i("Loading events where user is member...")
                memberEvents.value = db.collection("events")
                    .whereArrayContains("members", user.uid)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { EventData.build(it) }
                    .also {
                        Timber.d("Got ${it.size} events where the user is member.")
                    }

                Timber.i("Selecting event if applicable...")
                getApplication<Application>()
                    .dataStore
                    .data
                    .map { it[Keys.SelectedEvent] }
                    .first()
                    ?.let { select(it) }
            } catch (e: FirebaseFirestoreException) {
                Timber.e(e, "Could not get data from server.")
                error.value = e.code.name
            } finally {
                loading.value = false
            }
        }
    }

    /**
     * Selects an specific event from its id.
     * @author Arnau Mora
     * @since 20220711
     * @param eventId The ID of the event to select.
     */
    @Throws(IllegalArgumentException::class)
    fun select(eventId: String?) {
        val event = (creatorEvents.value + memberEvents.value)
            .find { it.id == eventId }
            ?: throw IllegalArgumentException("Could not find an event with id $eventId.")
        selectedEvent.value = event

        Timber.d("Selected event with id $eventId")
    }

    fun notifyEventCreation(eventData: EventData) {
        creatorEvents.append(eventData)
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EventLoader(application) as T
    }
}