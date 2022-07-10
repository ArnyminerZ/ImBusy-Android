package com.arnyminerz.imbusy.android.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.arnyminerz.imbusy.android.data.EventData
import com.arnyminerz.imbusy.android.exception.AuthException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MainViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    val loading = mutableStateOf(false)

    val memberEvents = mutableStateOf<List<EventData>>(emptyList())
    val creatorEvents = mutableStateOf<List<EventData>>(emptyList())

    @Throws(AuthException::class)
    fun loadUserEvents() {
        if (memberEvents.value.isNotEmpty() || creatorEvents.value.isNotEmpty())
            return

        val user = auth.currentUser ?: throw AuthException("User not logged in")

        viewModelScope.launch {
            loading.value = true

            try {
                Timber.i("Loading events where user is creator...")
                creatorEvents.value = db.collection("events")
                    .whereEqualTo("creator", user.uid)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { EventData.build(it) }

                Timber.i("Loading events where user is member...")
                memberEvents.value = db.collection("events")
                    .whereArrayContains("members", user.uid)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { EventData.build(it) }
            } catch (e: FirebaseException) {
                Timber.e(e, "Could not get data from server.")
                // TODO: Show error to the user
            } finally {
                loading.value = false
            }
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel() as T
    }
}