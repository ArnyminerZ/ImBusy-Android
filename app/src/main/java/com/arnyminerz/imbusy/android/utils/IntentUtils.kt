package com.arnyminerz.imbusy.android.utils

import android.content.Intent
import android.os.Build

/**
 * Runs the [Intent.getParcelableExtra] method depending on the current SDK level.
 * @author Arnau Mora
 * @since 20220711
 * @param key The key of the Parcelable to get.
 */
inline fun <reified T> Intent.getLegacyParcelableExtra(key: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getParcelableExtra(key, T::class.java)
    else
        @Suppress("DEPRECATION")
        getParcelableExtra(key) as T?
