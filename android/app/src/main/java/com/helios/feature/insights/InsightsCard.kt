package com.helios.feature.insights

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.motion.HeliosMotion
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import kotlinx.coroutines.delay

/**
 * The card surface of this feature, and the screen entrance.
 *
 * Both are local on purpose: `core/designsystem` is owned by another step of this Quest,
 * so this feature carries its own copy rather than editing it. When the design system
 * grows a `Card` and a generic entrance, these two composables are what it replaces.
 */

/** A card: tertiary filler, hairline inner stroke, md radius by default. */
@Composable
fun InsightsSurface(
    modifier: Modifier = Modifier,
    shape: Shape = HeliosShape.md,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalHeliosSemanticColors.current
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.backgroundTertiary)
            .border(HeliosElevation.MaterialTokens.innerStrokeWidth, colors.separatorHairline, shape)
            .padding(HeliosSpacing.cardPadding),
        content = content
    )
}

/**
 * `HeliosMotion.gentle` is typed to `Float`, and a list entrance animates a `Float` too,
 * so the gentle token is reused directly here. This size spec exists for the callers that
 * need an `IntSize` spring and cannot use the token as it stands.
 */
val InsightsGentleSize: FiniteAnimationSpec<IntSize> =
    spring<IntSize>(dampingRatio = 0.85f, stiffness = 120f)

/**
 * Screen entrance from motion-language section 3: the highlight first, then the sections
 * at the stats, header and list delays. Reduce Motion collapses it to the 200 ms opacity
 * cross-fade with no offset.
 *
 * [step] 0 highlight, 1 stats and cards, 2 section headers, 3 lists.
 */
@Composable
fun InsightsEntrance(
    step: Int,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val delayMs = when (step) {
        0 -> HeliosMotion.Entrance.heroDelayMs
        1 -> HeliosMotion.Entrance.statsStaggerMs
        2 -> HeliosMotion.Entrance.sectionHeaderDelayMs
        else -> HeliosMotion.Entrance.chartDelayMs
    }
    LaunchedEffect(step, reducedMotion) {
        if (delayMs > 0) delay(delayMs.toLong())
        visible = true
    }
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (reducedMotion) {
            tween(HeliosMotion.DurationMs.reduceMotionCrossfade, easing = HeliosMotion.Curves.standard)
        } else {
            HeliosMotion.gentle
        },
        label = "insightsEntrance"
    )
    Box(
        modifier = modifier.graphicsLayer {
            alpha = progress
            translationY = if (reducedMotion) 0f else (1f - progress) * 12f
        }
    ) {
        content()
    }
}
