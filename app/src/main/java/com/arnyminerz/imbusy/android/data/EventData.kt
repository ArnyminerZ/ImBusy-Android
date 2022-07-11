package com.arnyminerz.imbusy.android.data

import android.os.Parcelable
import com.arnyminerz.imbusy.android.utils.appendAll
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.Date

@Parcelize
class EventData(
    val id: String,
    val name: String,
    val startDate: Date,
    val endDate: Date,
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
                    snapshot.getTimestamp("start_date")!!.toDate(),
                    snapshot.getTimestamp("end_date")!!.toDate(),
                    snapshot.getString("creator")!!,
                    snapshot.get("members") as List<String>
                )
            } catch (e: NullPointerException) {
                Timber.w("Event with id ${snapshot.id} doesn't have a valid structure.")
                null
            }
    }

    suspend fun getUsersData(functions: FirebaseFunctions): PeopleData =
        UserData.bulkGet(
            functions,
            mutableListOf(creator).appendAll(members),
        ).let {
            PeopleData(it[0], it.subList(1, it.size))
        }

    data class PeopleData(
        val creator: UserData,
        val membersData: List<UserData>,
    )
}
