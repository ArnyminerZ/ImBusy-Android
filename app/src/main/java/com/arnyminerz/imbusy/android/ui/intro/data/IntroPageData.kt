package com.arnyminerz.imbusy.android.ui.intro.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

data class IntroPageData(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val requires: MutableState<Boolean>? = null,
    val options: OptionsData? = null,
    val buttons: List<@Composable () -> Unit> = emptyList(),
)
