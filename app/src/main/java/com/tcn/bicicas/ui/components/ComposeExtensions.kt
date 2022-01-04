package com.tcn.bicicas.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection

fun PaddingValues.plus(
    paddingValues: PaddingValues,
    layoutDirection: LayoutDirection
): PaddingValues {
    return PaddingValues(
        top = calculateTopPadding() + paddingValues.calculateTopPadding(),
        bottom = calculateBottomPadding() + paddingValues.calculateBottomPadding(),
        start = calculateStartPadding(layoutDirection)
                + paddingValues.calculateStartPadding(layoutDirection),
        end = calculateEndPadding(layoutDirection)
                + paddingValues.calculateEndPadding(layoutDirection),
    )
}