package com.example.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class WindowWidthTier {
    COMPACT_SMALL, // < 360.dp (small phones, narrow split-screen)
    COMPACT,       // 360.dp .. < 600.dp (standard phones)
    MEDIUM,        // 600.dp .. < 840.dp (foldables, small tablets, landscape phones)
    EXPANDED       // >= 840.dp (large tablets, desktops, ChromeOS)
}

@Immutable
data class ResponsiveSpec(
    val availableWidth: Dp,
    val availableHeight: Dp,
    val widthTier: WindowWidthTier,
    val isLandscape: Boolean,
    val useNavRail: Boolean,
    val gridColumns: Int,
    val quickActionColumns: Int,
    val categoryColumns: Int,
    val adminStatColumns: Int,
    val horizontalPadding: Dp,
    val cardPadding: Dp,
    val sectionSpacing: Dp,
    val popularCardWidth: Dp,
    val productImageHeight: Dp,
    val detailHeroHeight: Dp,
    val browseMaxWidth: Dp,
    val formMaxWidth: Dp,
    val fontScaleFactor: Float
) {
    val isCompactSmall: Boolean get() = widthTier == WindowWidthTier.COMPACT_SMALL
    val isMediumOrLarger: Boolean get() = widthTier == WindowWidthTier.MEDIUM || widthTier == WindowWidthTier.EXPANDED
    val isExpanded: Boolean get() = widthTier == WindowWidthTier.EXPANDED

    fun sp(base: Float): TextUnit = (base * fontScaleFactor).sp
    fun sp(base: Int): TextUnit = (base * fontScaleFactor).sp
}

fun computeResponsiveSpec(availableWidth: Dp, availableHeight: Dp): ResponsiveSpec {
    val tier = when {
        availableWidth < 360.dp -> WindowWidthTier.COMPACT_SMALL
        availableWidth < 600.dp -> WindowWidthTier.COMPACT
        availableWidth < 840.dp -> WindowWidthTier.MEDIUM
        else -> WindowWidthTier.EXPANDED
    }
    val isLandscape = availableWidth > availableHeight
    val useNavRail = availableWidth >= 600.dp

    val gridColumns = when {
        availableWidth >= 900.dp -> 4
        availableWidth >= 600.dp -> 3
        else -> 2
    }

    val quickActionColumns = when {
        availableWidth < 330.dp -> 2
        availableWidth >= 720.dp -> 6
        else -> 3
    }

    val categoryColumns = if (availableWidth >= 600.dp) 3 else 2
    val adminStatColumns = if (availableWidth >= 840.dp) 4 else 2

    val horizontalPadding = when (tier) {
        WindowWidthTier.COMPACT_SMALL -> 12.dp
        WindowWidthTier.COMPACT -> 16.dp
        WindowWidthTier.MEDIUM -> 24.dp
        WindowWidthTier.EXPANDED -> 28.dp
    }

    val cardPadding = when (tier) {
        WindowWidthTier.COMPACT_SMALL -> 12.dp
        WindowWidthTier.COMPACT -> 16.dp
        WindowWidthTier.MEDIUM -> 18.dp
        WindowWidthTier.EXPANDED -> 20.dp
    }

    val sectionSpacing = when (tier) {
        WindowWidthTier.COMPACT_SMALL -> 10.dp
        WindowWidthTier.COMPACT -> 14.dp
        WindowWidthTier.MEDIUM -> 16.dp
        WindowWidthTier.EXPANDED -> 18.dp
    }

    val popularCardWidth = when (tier) {
        WindowWidthTier.COMPACT_SMALL -> 154.dp
        WindowWidthTier.COMPACT -> 178.dp
        WindowWidthTier.MEDIUM -> 202.dp
        WindowWidthTier.EXPANDED -> 216.dp
    }

    val productImageHeight = when (tier) {
        WindowWidthTier.COMPACT_SMALL -> 110.dp
        WindowWidthTier.COMPACT -> 132.dp
        WindowWidthTier.MEDIUM -> 148.dp
        WindowWidthTier.EXPANDED -> 160.dp
    }

    val detailHeroHeight = when {
        isLandscape && availableHeight < 520.dp -> 200.dp
        tier == WindowWidthTier.COMPACT_SMALL -> 220.dp
        tier == WindowWidthTier.COMPACT -> 270.dp
        tier == WindowWidthTier.MEDIUM -> 310.dp
        else -> 340.dp
    }

    val browseMaxWidth = if (useNavRail) 1120.dp else Dp.Unspecified
    val formMaxWidth = if (useNavRail) 720.dp else Dp.Unspecified

    val fontScaleFactor = when (tier) {
        WindowWidthTier.COMPACT_SMALL -> 0.92f
        WindowWidthTier.COMPACT -> 1.0f
        WindowWidthTier.MEDIUM -> 1.04f
        WindowWidthTier.EXPANDED -> 1.08f
    }

    return ResponsiveSpec(
        availableWidth = availableWidth,
        availableHeight = availableHeight,
        widthTier = tier,
        isLandscape = isLandscape,
        useNavRail = useNavRail,
        gridColumns = gridColumns,
        quickActionColumns = quickActionColumns,
        categoryColumns = categoryColumns,
        adminStatColumns = adminStatColumns,
        horizontalPadding = horizontalPadding,
        cardPadding = cardPadding,
        sectionSpacing = sectionSpacing,
        popularCardWidth = popularCardWidth,
        productImageHeight = productImageHeight,
        detailHeroHeight = detailHeroHeight,
        browseMaxWidth = browseMaxWidth,
        formMaxWidth = formMaxWidth,
        fontScaleFactor = fontScaleFactor
    )
}

val LocalResponsiveSpec = staticCompositionLocalOf {
    computeResponsiveSpec(400.dp, 800.dp)
}

@Composable
fun ProvideResponsiveLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val spec = computeResponsiveSpec(
            availableWidth = maxWidth,
            availableHeight = maxHeight
        )
        CompositionLocalProvider(LocalResponsiveSpec provides spec) {
            content()
        }
    }
}

@Composable
fun ResponsiveCenteredBox(
    maxWidth: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        val innerModifier = if (maxWidth != Dp.Unspecified) {
            Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth)
        } else {
            Modifier.fillMaxWidth()
        }
        Box(modifier = innerModifier, content = content)
    }
}
