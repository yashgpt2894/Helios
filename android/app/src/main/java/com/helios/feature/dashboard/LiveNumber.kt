package com.helios.feature.dashboard

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography

/**
 * Value ticker.
 *
 * States: updating (pulse, ticker on), static (no pulse), stale (dimmed to 60 percent, no
 * ticker, age stated by the caller), demo (accent pulse plus the demo wording elsewhere on
 * screen). The accessible name is stable: it is the label plus value plus unit as one
 * node, so a screen reader never reads a number mid-animation.
 */
enum class LiveNumberState { UPDATING, STATIC, STALE, DEMO }

@Composable
fun LiveNumber(
    label: String,
    value: String,
    unit: String = "",
    isLive: Boolean = false,
    modifier: Modifier = Modifier,
    state: LiveNumberState = if (isLive) LiveNumberState.UPDATING else LiveNumberState.STATIC
) {
    val colors = LocalHeliosSemanticColors.current
    val accent = when (state) {
        LiveNumberState.DEMO -> colors.accentPrimary
        else -> colors.solarPrimary
    }
    val dimmed = state == LiveNumberState.STALE
    val valueColor = if (dimmed) colors.textPrimary.copy(alpha = 0.6f) else colors.textPrimary
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "$label $value $unit".trim()
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            style = HeliosTypography.caption2,
            color = colors.textTertiary
        )
        Spacer(Modifier.height(HeliosSpacing.space1))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = HeliosTypography.title1,
                fontWeight = FontWeight.Light,
                color = valueColor
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(HeliosSpacing.space1))
                Text(
                    text = unit,
                    style = HeliosTypography.callout,
                    color = colors.textTertiary
                )
            }
        }
        if (state != LiveNumberState.STATIC) {
            Spacer(Modifier.height(HeliosSpacing.space1))
            PulsingDot(accent, size = 8.dp)
        }
    }
}

/** A soft pulse used for live and demo states. Static under Reduce Motion. */
@Composable
fun PulsingDot(
    color: androidx.compose.ui.graphics.Color,
    size: Dp = 8.dp,
    animate: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
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
