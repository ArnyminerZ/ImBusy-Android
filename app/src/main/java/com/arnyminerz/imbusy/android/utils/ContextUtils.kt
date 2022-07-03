package com.arnyminerz.imbusy.android.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes

@IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
annotation class ToastDuration

fun toast(context: Context, text: String, @ToastDuration duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(context, text, duration).show()

fun toast(
    context: Context,
    @StringRes textResource: Int,
    @ToastDuration duration: Int = Toast.LENGTH_SHORT
) =
    toast(context, context.getString(textResource), duration)
