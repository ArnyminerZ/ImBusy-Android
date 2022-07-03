package com.arnyminerz.imbusy.android.ui.intro.data

import androidx.annotation.UiThread
import com.arnyminerz.imbusy.android.utils.keysList

abstract class OptionsData(
    val contentDescription: String,
) : Iterable<Pair<String, String>> {
    /**
     * The key is the value that gets returned when selecting the option, the value of the dictionary
     * matches the text displayed to the user.
     * @author Arnau Mora
     * @since 20220307
     */
    abstract val options: Map<String, String>

    @UiThread
    abstract fun onChosenOption(option: String)

    val isEmpty: Boolean
        get() = options.isEmpty()

    val isNotEmpty: Boolean
        get() = options.isNotEmpty()

    fun getKey(index: Int) = options.keysList[index]

    override fun iterator(): Iterator<Pair<String, String>> =
        options
            .entries
            .map { it.key to it.value }
            .iterator()

    operator fun get(value: String): String? = options[value]
}
