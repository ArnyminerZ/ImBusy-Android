package com.arnyminerz.imbusy.android.utils

import androidx.compose.runtime.MutableState

fun <T> Boolean.ifElse(ifTrue: T, ifFalse: T) =
    if (this)
        ifTrue
    else
        ifFalse

fun List<MutableState<*>>.assertStates(
    stringCheck: (string: String) -> Boolean = { it.isNotEmpty() },
) = this
    .map { it.value }
    .assert(stringCheck)

fun List<Any?>.assert(
    stringCheck: (string: String) -> Boolean,
): Boolean {
    for (item in this)
        if (item == null)
            return false
        else if (item is String && !stringCheck(item))
            return false
    return true
}
