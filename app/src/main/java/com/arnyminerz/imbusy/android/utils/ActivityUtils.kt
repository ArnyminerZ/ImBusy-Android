package com.arnyminerz.imbusy.android.utils

import android.app.Activity

fun Activity.restart() {
    val intent = intent
    finish()
    startActivity(intent)
}
