package com.arnyminerz.imbusy.android.utils

import androidx.compose.runtime.MutableState

/**
 * Adds the object to the list, and returns itself after doing so.
 * @author Arnau Mora
 * @since 20220711
 * @param obj The object to add.
 */
fun <T, L : MutableList<T>> L.append(obj: T): L = apply {
    add(obj)
}

/**
 * Adds all the objects to the list, and returns itself after doing so.
 * @author Arnau Mora
 * @since 20220711
 * @param objects The objects to add.
 */
fun <T, L : MutableList<T>> L.appendAll(objects: Collection<T>): L = apply {
    addAll(objects)
}

/**
 * Adds the object to the state's list, and updates its value.
 * @author Arnau Mora
 * @since 20220711
 * @param obj The object to add.
 */
fun <T, L : List<T>, S : MutableState<L>> S.append(obj: T) {
    @Suppress("UNCHECKED_CAST")
    this.value = value.toMutableList().append(obj).toList() as L
}
