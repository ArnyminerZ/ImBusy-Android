package com.arnyminerz.imbusy.android.utils

val <K, V> Map<K, V>.keysList: List<K>
    get() = keys.toList()
