package com.arnyminerz.imbusy.android.utils

import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Date.format(format: String): String = SimpleDateFormat(format, Locale.getDefault()).format(this)

/**
 * Gets the current [Date] from [Calendar].
 * @author Arnau Mora
 * @since 20220711
 * @return A [Date] instance set to the current date and time.
 */
fun now(): Date = Calendar.getInstance().time

fun DatePickerDialog.updateDate(calendar: Calendar) = this.updateDate(
    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
)

fun DatePickerDialog.updateDate(date: Date) = updateDate(Calendar.getInstance().apply {
        time = date
    })
