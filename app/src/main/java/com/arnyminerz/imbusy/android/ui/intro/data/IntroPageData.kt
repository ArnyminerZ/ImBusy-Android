package com.arnyminerz.imbusy.android.ui.intro.data

import androidx.compose.runtime.Composable

data class IntroPageData(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val onInteraction: InteractionEvent<Boolean>? = null,
    val options: OptionsData? = null,
    val buttons: List<@Composable () -> Unit> = emptyList(),
)
