package com.arnyminerz.imbusy.android.ui.intro.data

class InteractionEvent<R> {
    private val receivers: MutableList<(result: R) -> Unit> = mutableListOf()

    fun addReceiver(block: (result: R) -> Unit) {
        receivers.add(block)
    }

    operator fun invoke(result: R) {
        for (receiver in receivers)
            receiver(result)
    }
}