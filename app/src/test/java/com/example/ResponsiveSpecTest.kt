package com.example

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WindowWidthTier
import com.example.ui.theme.computeResponsiveSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponsiveSpecTest {

    @Test
    fun compactSmallScreen_scalesDownSpacingAndColumns() {
        val spec = computeResponsiveSpec(availableWidth = 320.dp, availableHeight = 640.dp)
        assertEquals(WindowWidthTier.COMPACT_SMALL, spec.widthTier)
        assertTrue(spec.isCompactSmall)
        assertFalse(spec.useNavRail)
        assertEquals(2, spec.gridColumns)
        assertEquals(2, spec.quickActionColumns)
        assertEquals(2, spec.categoryColumns)
        assertEquals(2, spec.adminStatColumns)
        assertEquals(Dp.Unspecified, spec.browseMaxWidth)
        assertEquals(Dp.Unspecified, spec.formMaxWidth)
        assertEquals(0.92f, spec.fontScaleFactor, 0.001f)
    }

    @Test
    fun compactPhoneScreen_usesStandardPhoneMetrics() {
        val spec = computeResponsiveSpec(availableWidth = 411.dp, availableHeight = 915.dp)
        assertEquals(WindowWidthTier.COMPACT, spec.widthTier)
        assertFalse(spec.isCompactSmall)
        assertFalse(spec.useNavRail)
        assertFalse(spec.isLandscape)
        assertEquals(2, spec.gridColumns)
        assertEquals(3, spec.quickActionColumns)
        assertEquals(2, spec.categoryColumns)
        assertEquals(2, spec.adminStatColumns)
        assertEquals(1.0f, spec.fontScaleFactor, 0.001f)
    }

    @Test
    fun mediumFoldableOrTablet_enablesNavRailAndMaxWidthConstraints() {
        val spec = computeResponsiveSpec(availableWidth = 720.dp, availableHeight = 960.dp)
        assertEquals(WindowWidthTier.MEDIUM, spec.widthTier)
        assertTrue(spec.isMediumOrLarger)
        assertFalse(spec.isExpanded)
        assertTrue(spec.useNavRail)
        assertEquals(3, spec.gridColumns)
        assertEquals(6, spec.quickActionColumns)
        assertEquals(3, spec.categoryColumns)
        assertEquals(2, spec.adminStatColumns)
        assertEquals(1120.dp, spec.browseMaxWidth)
        assertEquals(720.dp, spec.formMaxWidth)
        assertEquals(1.04f, spec.fontScaleFactor, 0.001f)
    }

    @Test
    fun expandedTablet_usesFourColumnsAndFourAdminStats() {
        val spec = computeResponsiveSpec(availableWidth = 1024.dp, availableHeight = 768.dp)
        assertEquals(WindowWidthTier.EXPANDED, spec.widthTier)
        assertTrue(spec.isExpanded)
        assertTrue(spec.isLandscape)
        assertTrue(spec.useNavRail)
        assertEquals(4, spec.gridColumns)
        assertEquals(6, spec.quickActionColumns)
        assertEquals(3, spec.categoryColumns)
        assertEquals(4, spec.adminStatColumns)
        assertEquals(1.08f, spec.fontScaleFactor, 0.001f)
    }

    @Test
    fun shortLandscapePhone_capsDetailHeroHeight() {
        val spec = computeResponsiveSpec(availableWidth = 780.dp, availableHeight = 390.dp)
        assertTrue(spec.isLandscape)
        assertEquals(200.dp, spec.detailHeroHeight)
    }
}
