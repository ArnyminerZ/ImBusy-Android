package com.arnyminerz.imbusy.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arnyminerz.imbusy.android.data.EventData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.util.Date

class EventCreator : ViewModel() {

    private val db = Firebase.firestore

    fun createEvent(
        creator: String,
        name: String,
        startDate: Date,
        endDate: Date,
        onCreated: (event: EventData) -> Unit,
    ) {
        db.collection("events")
            .add(
                hashMapOf(
                    "name" to name,
                    "start_date" to startDate,
                    "end_date" to endDate,
                    "creator" to creator,
                )
            )
            .addOnSuccessListener { document ->
                Timber.i("Created event with id: ${document.id}")
                onCreated(
                    EventData(
                        document.id,
                        name,
                        startDate,
                        endDate,
                        creator,
                        emptyList(), // TODO: Implement members addition on creation
                    )
                )
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Could not create event.")
                // TODO: Warn the user about the error
            }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EventCreator() as T
    }
}