package com.arnyminerz.imbusy.android.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Runs the code inside [block] suspended in the IO thread ([Dispatchers.IO]).
 * @author Arnau Mora
 * @since 20220711
 * @param block The block of code to run.
 */
fun doAsync(block: suspend CoroutineScope.() -> Unit) =
    CoroutineScope(Dispatchers.IO).launch(
        block = block
    )
