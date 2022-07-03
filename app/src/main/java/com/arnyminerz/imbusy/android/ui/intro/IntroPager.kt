package com.arnyminerz.imbusy.android.ui.intro

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.arnyminerz.imbusy.android.R
import com.arnyminerz.imbusy.android.ui.intro.data.IntroPageData
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
@ExperimentalPagerApi
@ExperimentalMaterial3Api
fun IntroPager(
    pages: List<IntroPageData> = listOf(),
    onFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val pageLocked by remember { mutableStateOf(false) }

    fun anyMorePages(): Boolean = pagerState.currentPage + 1 < pages.size

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // If there are no more pages
                    if (!anyMorePages())
                        onFinished()
                    // If there are more pages, go to the next one
                    else scope.launch {
                        pagerState.animateScrollToPage(
                            pagerState.currentPage + 1
                        )
                    }
                },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (anyMorePages())
                            Icons.Rounded.ChevronRight
                        else
                            Icons.Rounded.Check,
                        contentDescription = stringResource(
                            if (anyMorePages())
                                R.string.fab_desc_intro_next
                            else
                                R.string.fab_desc_intro_end
                        ),
                    )
                    Text(
                        stringResource(
                            if (anyMorePages())
                                R.string.action_next
                            else
                                R.string.action_done
                        )
                    )
                }
            }
        },
    ) { paddingValues ->
        HorizontalPager(
            count = pages.size,
            userScrollEnabled = !pageLocked,
            state = pagerState,
            modifier = Modifier.padding(paddingValues),
        ) { pageIndex ->
            val page = pages[pageIndex]
            IntroPage(
                title = page.title,
                subtitle = page.subtitle,
                emoji = page.emoji,
                optionsData = page.options,
                buttons = page.buttons,
            )
        }
    }
}
