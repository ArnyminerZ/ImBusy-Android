package com.arnyminerz.imbusy.android.data

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserData(
    val uid: String,
    val displayName: String,
    val photoUrl: String,
) {
    companion object {
        @Throws(FirebaseFunctionsException::class)
        suspend fun bulkGet(functions: FirebaseFunctions, uids: List<String>): List<UserData> =
            suspendCoroutine { cont ->
                functions.getHttpsCallable("batchUserDataGet")
                    .call(hashMapOf("uids" to uids))
                    .addOnSuccessListener { result ->
                        val data = result.data as HashMap<*, *>
                        val usersData = data["data"] as List<HashMap<String, *>>
                        cont.resume(
                            usersData.mapIndexed { index, userData ->
                                UserData(
                                    uids[index],
                                    userData,
                                )
                            }
                        )
                    }
                    .addOnFailureListener { e ->
                        throw e
                    }
            }
    }

    constructor(uid: String, data: HashMap<String, *>) : this(
        uid,
        data.getValue("displayName") as String,
        data.getValue("photoURL") as String,
    )

    override fun toString(): String = "{uid=$uid, displayName=$displayName, photoURL=$photoUrl}"
}
