package com.arnyminerz.imbusy.android.ui.intro

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.imbusy.android.R
import com.arnyminerz.imbusy.android.ui.intro.data.OptionsData

@Composable
fun IntroPage(
    title: String,
    subtitle: String,
    emoji: String,
    optionsData: OptionsData?,
    buttons: List<@Composable () -> Unit>,
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        // Image Emoji
        Text(
            emoji,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 116.dp),
            style = MaterialTheme.typography.labelLarge,
            fontSize = 116.sp,
            textAlign = TextAlign.Center,
        )

        Column(
            modifier = Modifier
                .padding(start = 48.dp, end = 48.dp, bottom = 130.dp)
                .align(Alignment.BottomCenter),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(bottom = 4.dp),
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .apply {
                        if (optionsData != null && optionsData.isNotEmpty || buttons.isNotEmpty())
                            padding(bottom = 16.dp)
                    }
            )

            // Dropdown menu
            if (optionsData != null && optionsData.isNotEmpty) {
                var selectedValue by remember { mutableStateOf(optionsData.getKey(0)) }
                var expanded by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopEnd)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                            .padding(8.dp),
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = optionsData.contentDescription,
                        )
                        Text(
                            optionsData[selectedValue] ?: stringResource(R.string.dropdown_choose),
                            modifier = Modifier
                                .weight(1f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        for ((key, value) in optionsData)
                            DropdownMenuItem(
                                onClick = {
                                    selectedValue = key
                                    expanded = false
                                },
                                text = {
                                    Text(value)
                                },
                            )
                    }
                }
            }

            // Buttons
            for (buttonData in buttons)
                buttonData()
        }
    }
}
