package com.helios.feature.dashboard

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.motion.HeliosMotion
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.format.HeliosFormat

/**
 * What the ticker is doing, which the caller derives from the reading's freshness rather
 * than from a timer.
 *
 *  UPDATING a live reading that is still being polled: the value springs to the new
 *           number and a soft pulse runs
 *  STATIC   a reading that arrived but is not running: no pulse
 *  STALE    older than 15 s: dimmed to 60 percent, no pulse, age stated by the TopBar
 *  DEMO     the simulated system: accent pulse, "Demo data" wording, never the word live
 *  OFFLINE  the link is down: the last known value stays visible, labelled by the banner
 */
enum class LiveNumberState { UPDATING, STATIC, STALE, DEMO, OFFLINE }

/**
 * The largest number on a screen: value, unit and label, animated between readings.
 *
 * Motion: the value interpolates with the default token spring (snappy is wrong here, it
 * overshoots a power reading), so a 2 s poll reads as a moving instrument rather than a
 * flicker. Reduce Motion snaps it, and the animator duration scale still applies to the
 * cross-fade that replaces the spring.
 *
 * Type: hero 56/300, capped at 44 sp once the user scales text past 133 percent so the
 * number cannot push the unit off screen (DESIGN.md section 9). The unit is separate static
 * text, so a growing number never shifts it.
 *
 * Accessibility: one node, named "label value unit". It is not a live region: the screen
 * announces on a status change, never on every poll.
 */
@Composable
fun LiveNumber(
    label: String,
    value: Double,
    unit: String,
    state: LiveNumberState,
    modifier: Modifier = Modifier,
    decimals: Int = 2,
    motion: HeliosMotionSettings = HeliosMotionSettings.Default,
    valueStyle: TextStyle = HeliosTypography.hero,
    labelStyle: TextStyle = HeliosTypography.caption2,
    unitStyle: TextStyle = HeliosTypography.callout,
    dimAlpha: Float = 0.6f,
    valueColor: Color? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val accent = when (state) {
        LiveNumberState.DEMO -> colors.accentPrimary
        LiveNumberState.OFFLINE -> colors.alertPrimary
        else -> colors.solarPrimary
    }
    val dimmed = state == LiveNumberState.STALE || state == LiveNumberState.OFFLINE
    val target = if (value.isNaN()) 0f else value.toFloat()
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = when {
            value.isNaN() -> tween(durationMillis = 0)
            motion.animates() -> HeliosMotion.defaultSpring
            else -> motion.tweenMs(HeliosMotion.DurationMs.fast)
        },
        label = "liveNumber"
    )
    val formatted = if (value.isNaN()) HeliosFormat.NO_DATA else HeliosFormat.fixed(animated.toDouble(), decimals)
    val baseColor = valueColor ?: colors.textPrimary

    Column(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = if (value.isNaN()) {
                "$label not reported"
            } else {
                "$label ${HeliosFormat.fixed(value, decimals)} $unit"
            }
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            style = labelStyle,
            color = colors.textTertiary
        )
        Spacer(Modifier.height(HeliosSpacing.space1))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatted,
                style = cappedHero(valueStyle),
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                color = if (dimmed) baseColor.copy(alpha = dimAlpha) else baseColor
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(HeliosSpacing.space1))
                Text(
                    text = unit,
                    style = unitStyle,
                    color = colors.textTertiary
                )
            }
        }
        if (state != LiveNumberState.STATIC) {
            Spacer(Modifier.height(HeliosSpacing.space1))
            PulsingDot(
                color = accent,
                size = 8.dp,
                animate = motion.animates() && state != LiveNumberState.OFFLINE,
                motion = motion
            )
        }
    }
}

/** The hero style, capped so 200 percent text scaling cannot break the hero block. */
@Composable
private fun cappedHero(style: TextStyle): TextStyle {
    val fontScale = LocalDensity.current.fontScale
    val cap = HeliosSpacing.LayoutMetrics.heroCapSp
    return if (style.fontSize.value > cap && fontScale >= 1.33f) {
        style.copy(fontSize = cap.sp, lineHeight = (cap + 4).sp)
    } else {
        style
    }
}

/**
 * A soft pulse for a live or demo reading. It stops when Reduce Motion is on, when the
 * reading is stale, or when the device has animation switched off.
 */
@Composable
fun PulsingDot(
    color: Color,
    size: Dp = 8.dp,
    animate: Boolean = true,
    motion: HeliosMotionSettings = HeliosMotionSettings.Default
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = motion.durationMs(HeliosMotion.DurationMs.chargeGlowPulse),
                easing = HeliosMotion.Curves.linear
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = if (animate) alpha else 1f))
    )
}
