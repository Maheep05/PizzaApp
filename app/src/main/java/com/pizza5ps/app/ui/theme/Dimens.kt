package com.pizza5ps.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Spacing scale — a 4dp base grid.
 *
 * Every gap in the app comes from this scale. Consistent rhythm is the least
 * glamorous and most effective part of looking designed: it is what stops a screen
 * from feeling like it was nudged into place by hand.
 */
object Space {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 48.dp

    /** Standard screen side gutter. */
    val gutter = 20.dp
}

/**
 * Corner radii. Larger containers get proportionally larger radii so curvature
 * looks optically even rather than mathematically equal.
 */
object Radius {
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 22.dp
    val xl = 28.dp
    val xxl = 34.dp
    val pill = 100.dp
}

object Elevation {
    val flat = 0.dp
    val raised = 3.dp
    val card = 8.dp
    val floating = 16.dp
    val sheet = 24.dp
}

val PizzeriaShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.xs),
    small = RoundedCornerShape(Radius.sm),
    medium = RoundedCornerShape(Radius.md),
    large = RoundedCornerShape(Radius.lg),
    extraLarge = RoundedCornerShape(Radius.xl)
)
