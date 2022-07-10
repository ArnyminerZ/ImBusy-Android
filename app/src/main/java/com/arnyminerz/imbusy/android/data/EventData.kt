package com.arnyminerz.imbusy.android.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
class EventData(
    val id: String,
    val name: String,
    private val _startDate: Timestamp,
    private val _endDate: Timestamp,
    val creator: String,
    val members: List<String>,
) : Parcelable {
    companion object {
        fun build(snapshot: DocumentSnapshot): EventData? =
            try {
                @Suppress("UNCHECKED_CAST")
                EventData(
                    snapshot.id,
                    snapshot.getString("name")!!,
                    snapshot.getTimestamp("start_date")!!,
                    snapshot.getTimestamp("end_date")!!,
                    snapshot.getString("creator")!!,
                    snapshot.get("members") as List<String>
                )
            } catch (e: NullPointerException) {
                Timber.w("Event with id ${snapshot.id} doesn't have a valid structure.")
                null
            }
    }

    @IgnoredOnParcel
    val startDate = _startDate.toDate()

    @IgnoredOnParcel
    val endDate = _endDate.toDate()
}
