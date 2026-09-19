package com.helios.feature.battery

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.motion.HeliosMotion
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import kotlinx.coroutines.delay

/**
 * The one card surface the Battery screen uses, so every block on the screen has the same
 * radius, hairline and filler. It is local to this feature because
 * `core/designsystem` is owned by another step; when the design system grows a `Card`,
 * this composable is what it replaces.
 */
@Composable
fun BatteryCard(
    modifier: Modifier = Modifier,
    shape: Shape = HeliosShape.lg,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.backgroundTertiary)
            .border(HeliosElevation.MaterialTokens.innerStrokeWidth, colors.separatorHairline, shape)
            .padding(HeliosSpacing.cardPadding),
        content = content
    )
}

/** A hairline divider that spans the card, used between blocks in the hero. */
@Composable
fun BatteryHairline(modifier: Modifier = Modifier) {
    val colors = LocalHeliosSemanticColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = HeliosSpacing.space3)
            .background(colors.separatorHairline)
            .clip(HeliosShape.xs)
    )
}

/**
 * `HeliosMotion.gentle` is a `SpringSpec<Float>`, and a vertical expand needs an `IntSize`
 * spec. The token values are the gentle spring's, written once here; when `HeliosMotion`
 * grows a generic spring this constant goes away.
 */
val HeliosGentleSize: FiniteAnimationSpec<IntSize> =
    spring<IntSize>(dampingRatio = 0.85f, stiffness = 120f)

/**
 * Screen entrance from motion-language section 3: hero first, then stats with a stagger,
 * then section headers, then the lists. Reduce Motion collapses it to the 200 ms opacity
 * cross-fade and drops the offset, which is the only difference between the two paths.
 *
 * [step] 0 hero, 1 stats row, 2 section header, 3 list or chart. Every delay and every
 * duration comes from [HeliosMotion], so no screen invents its own timing.
 */
@Composable
fun StaggeredEntrance(
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
        label = "batteryEntrance"
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
