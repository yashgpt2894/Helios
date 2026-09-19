package com.helios.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.motion.HeliosMotion
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.WeatherCondition

/**
 * Shared primitives: identity, status, structure and the four states a surface can be
 * in (loading, empty, error, denied).
 *
 * Every component here reads semantic tokens only, so it renders correctly in Paper and
 * Carbon and under a white-label accent without a single call-site change.
 */

// ------------------------------------------------------------------ identity

/**
 * The helios mark: eight blades around a hollow centre.
 *
 * Decorative by default: the accessible name comes from the surrounding text, so a
 * screen reader does not read "helios mark" next to the word helios. Pass
 * [contentDescription] only where the mark is the sole identity on screen.
 */
@Composable
fun HeliosMark(
    size: Dp = 28.dp,
    color: Color = LocalHeliosSemanticColors.current.accentPrimary,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    val decorated = if (contentDescription == null) {
        modifier.clearAndSetSemantics {}
    } else {
        modifier.semantics { this.contentDescription = contentDescription }
    }
    Canvas(modifier = decorated.size(size)) {
        val centre = Offset(this.size.width / 2f, this.size.height / 2f)
        val outer = this.size.minDimension / 2f
        val inner = outer * 0.34f
        repeat(8) { index ->
            rotate(degrees = index * 45f, pivot = centre) {
                drawLine(
                    color = color,
                    start = Offset(centre.x, centre.y - inner),
                    end = Offset(centre.x, centre.y - outer),
                    strokeWidth = outer * 0.22f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * A white-label text mark: one letter in the accent colour. Used when
 * `Brand.mark == "text"`.
 */
@Composable
fun BrandTextMark(
    text: String,
    size: Dp = 28.dp,
    color: Color = LocalHeliosSemanticColors.current.accentPrimary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(HeliosShape.full)
            .border(1.dp, color.copy(alpha = 0.4f), HeliosShape.full),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = HeliosTypography.headline,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Choose the mark the brand asks for. */
@Composable
fun BrandMark(
    brandName: String,
    textMark: String?,
    usesRadialMark: Boolean,
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    if (usesRadialMark) {
        HeliosMark(size = size, modifier = modifier)
    } else {
        BrandTextMark(text = textMark ?: brandName.take(1), size = size, modifier = modifier)
    }
}

// ------------------------------------------------------------------ status

/** The word shown for each status. Status never relies on colour alone. */
fun HeliosStatusKind.label(): String = when (this) {
    HeliosStatusKind.PRODUCING -> "Producing"
    HeliosStatusKind.STANDBY -> "Standby"
    HeliosStatusKind.CURTAILED -> "Curtailed"
    HeliosStatusKind.NIGHT -> "Night"
    HeliosStatusKind.FAULT -> "Fault"
    HeliosStatusKind.OFFLINE -> "Offline"
    HeliosStatusKind.DEMO -> "Demo system"
}

/**
 * Inverter status: a dot plus a word, on its own container.
 *
 * Variants: producing, standby, curtailed, night, fault, offline, demo. When [onClick]
 * is set the pill grows to the 48 dp minimum touch target and announces itself as a
 * button (it opens the Connection sheet).
 */
@Composable
fun StatusPill(
    kind: HeliosStatusKind,
    modifier: Modifier = Modifier,
    label: String = kind.label(),
    onClick: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val interactive = if (onClick != null) {
        modifier
            .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget)
            .clickable(role = Role.Button, onClick = onClick)
    } else {
        modifier
    }
    Row(
        modifier = interactive
            .clip(HeliosShape.full)
            .background(colors.status.background(kind))
            .padding(horizontal = HeliosSpacing.space3, vertical = HeliosSpacing.space2)
            .semantics(mergeDescendants = true) { contentDescription = "Inverter status: $label" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(colors.status.foreground(kind))
        )
        Text(
            text = label,
            style = HeliosTypography.caption2,
            color = colors.status.foreground(kind),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Age of the displayed reading, and the entry point to the Connection sheet.
 *
 * Variants: live, aging (with wall clock), stale, offline, demo. It never says live when
 * the reading is older than 5 s, which is the rule the whole dashboard depends on.
 */
@Composable
fun FreshnessStamp(
    text: String,
    kind: HeliosStatusKind,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val interactive = if (onClick != null) {
        modifier
            .defaultMinSize(minHeight = 32.dp)
            .clickable(role = Role.Button, onClick = onClick)
    } else {
        modifier
    }
    Row(
        modifier = interactive.padding(vertical = HeliosSpacing.space1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(colors.status.foreground(kind))
        )
        Text(
            text = text,
            style = HeliosTypography.caption,
            color = colors.status.foreground(kind)
        )
    }
}

/**
 * Persistent failure notice with a reason and one recovery action.
 *
 * Variants: offline with a classified reason (F1-F7), reconnecting, recovered (which
 * auto-dismisses after 4 s in the caller). It is a polite live region so a screen reader
 * announces a change of state once, not on every poll.
 */
@Composable
fun ConnectionBanner(
    state: ConnectionBannerState,
    message: String,
    reason: String?,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onOpenConnection: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val background = when (state) {
        ConnectionBannerState.OFFLINE -> colors.alertSubtle
        ConnectionBannerState.RECONNECTING -> colors.accentSubtle
        ConnectionBannerState.RECOVERED -> colors.flowSubtle
    }
    val foreground = when (state) {
        ConnectionBannerState.OFFLINE -> colors.alertStrong
        ConnectionBannerState.RECONNECTING -> colors.onSubtle
        ConnectionBannerState.RECOVERED -> colors.flowStrong
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(background)
            .border(HeliosElevation.MaterialTokens.innerStrokeWidth, colors.separatorHairline, HeliosShape.md)
            .padding(HeliosSpacing.cardPadding)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(foreground)
            )
            Spacer(Modifier.width(HeliosSpacing.space2))
            Text(
                text = message,
                style = HeliosTypography.headline,
                color = foreground
            )
        }
        if (reason != null) {
            Spacer(Modifier.height(HeliosSpacing.space1))
            Text(
                text = reason,
                style = HeliosTypography.subheadline,
                color = foreground.copy(alpha = 0.85f)
            )
        }
        if (onRetry != null || onOpenConnection != null) {
            Spacer(Modifier.height(HeliosSpacing.space3))
            Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
                if (onRetry != null) {
                    HeliosSecondaryButton(text = "Retry now", onClick = onRetry)
                }
                if (onOpenConnection != null) {
                    HeliosGhostButton(text = "Connection", onClick = onOpenConnection)
                }
            }
        }
    }
}

enum class ConnectionBannerState { OFFLINE, RECONNECTING, RECOVERED }

// ------------------------------------------------------------------ structure

/**
 * Section header: optional eyebrow, a title, optional trailing content.
 * It replaces the private helper that used to live inside DashboardScreen.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    style = HeliosTypography.caption2,
                    color = colors.textTertiary
                )
            }
            Text(
                text = title,
                style = HeliosTypography.headline,
                color = colors.textPrimary
            )
        }
        trailing?.invoke()
    }
}

/** A small labelled chip, for filters and secondary choices. */
@Composable
fun HeliosChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    Box(
        modifier = modifier
            .clip(HeliosShape.full)
            .background(if (selected) colors.accentSubtle else colors.backgroundTertiary)
            .border(
                HeliosElevation.MaterialTokens.innerStrokeWidth,
                colors.separatorHairline,
                HeliosShape.full
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = HeliosSpacing.space3, vertical = HeliosSpacing.space2)
    ) {
        Text(
            text = label,
            style = HeliosTypography.caption,
            color = if (selected) colors.onSubtle else colors.textSecondary,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

// ------------------------------------------------------------------ buttons

/** Primary action: accent fill, one per view. */
@Composable
fun HeliosPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    disabledReason: String? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val clickable = enabled && !loading
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget)
            .clip(HeliosShape.md)
            .background(if (clickable) colors.accentPrimary else colors.backgroundTertiary)
            .clickable(enabled = clickable, role = Role.Button, onClick = onClick)
            .padding(horizontal = HeliosSpacing.space5, vertical = HeliosSpacing.space3)
            .semantics {
                if (disabledReason != null && !clickable) contentDescription = "$text. $disabledReason"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = if (clickable) colors.onAccent else colors.textTertiary
            )
        }
        Text(
            text = text,
            style = HeliosTypography.headline,
            color = if (clickable) colors.onAccent else colors.textQuaternary
        )
    }
}

/** Secondary action: outlined, same geometry as the primary. */
@Composable
fun HeliosSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = LocalHeliosSemanticColors.current
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget)
            .clip(HeliosShape.md)
            .border(HeliosElevation.MaterialTokens.innerStrokeWidth, colors.separatorStrong, HeliosShape.md)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = HeliosSpacing.space5, vertical = HeliosSpacing.space3),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = HeliosTypography.headline,
            color = if (enabled) colors.textPrimary else colors.textQuaternary
        )
    }
}

/** Ghost action: text only, for the second choice inside a banner or a card. */
@Composable
fun HeliosGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = LocalHeliosSemanticColors.current
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = HeliosSpacing.space3, vertical = HeliosSpacing.space3),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = HeliosTypography.callout,
            color = if (enabled) colors.accentStrong else colors.textQuaternary,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Destructive action, always paired with the words that say what will be removed. */
@Composable
fun HeliosDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = LocalHeliosSemanticColors.current
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget)
            .clip(HeliosShape.md)
            .background(colors.alertSubtle)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = HeliosSpacing.space5, vertical = HeliosSpacing.space3),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = HeliosTypography.headline,
            color = colors.alertStrong
        )
    }
}

// ------------------------------------------------------------------ states

/** Empty: an explanatory line plus the action that fixes it, never a blank card. */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.backgroundSecondary)
            .padding(HeliosSpacing.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeliosMark(size = 32.dp, color = colors.textQuaternary)
        Spacer(Modifier.height(HeliosSpacing.space3))
        Text(text = title, style = HeliosTypography.headline, color = colors.textPrimary)
        Spacer(Modifier.height(HeliosSpacing.space1))
        Text(text = message, style = HeliosTypography.callout, color = colors.textSecondary)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(HeliosSpacing.space4))
            HeliosSecondaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

/** Error: the reason in words plus one recovery action. */
@Composable
fun HeliosErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = "Retry now",
    detail: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.alertSubtle)
            .border(HeliosElevation.MaterialTokens.innerStrokeWidth, colors.separatorHairline, HeliosShape.md)
            .padding(HeliosSpacing.cardPadding)
    ) {
        Text(text = title, style = HeliosTypography.headline, color = colors.alertStrong)
        Spacer(Modifier.height(HeliosSpacing.space1))
        Text(text = message, style = HeliosTypography.callout, color = colors.alertStrong.copy(alpha = 0.9f))
        if (detail != null) {
            Spacer(Modifier.height(HeliosSpacing.space2))
            Text(text = detail, style = HeliosTypography.caption, color = colors.textSecondary)
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(HeliosSpacing.space4))
            HeliosSecondaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

/** Denied permission: it names the permission and keeps a working path. */
@Composable
fun DeniedPermissionState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String = "Open settings",
    onAction: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.accentSubtle)
            .padding(HeliosSpacing.cardPadding)
    ) {
        Text(text = title, style = HeliosTypography.headline, color = colors.onSubtle)
        Spacer(Modifier.height(HeliosSpacing.space1))
        Text(text = message, style = HeliosTypography.callout, color = colors.onSubtle)
        if (onAction != null) {
            Spacer(Modifier.height(HeliosSpacing.space4))
            HeliosSecondaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

/**
 * Skeleton block. Shape and height must match the content it replaces; the sweep is the
 * one place a linear easing is correct (motion-language section 10).
 */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    shape: androidx.compose.ui.graphics.Shape = HeliosShape.sm,
    shimmer: Boolean = true
) {
    val colors = LocalHeliosSemanticColors.current
    val transition = rememberInfiniteTransition(label = "skeleton")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(HeliosMotion.DurationMs.shimmer, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeletonSweep"
    )
    val brush = if (shimmer) {
        Brush.horizontalGradient(
            colors = listOf(colors.skeleton.base, colors.skeleton.highlight, colors.skeleton.base),
            startX = progress * 400f,
            endX = progress * 400f + 200f
        )
    } else {
        androidx.compose.ui.graphics.SolidColor(colors.skeleton.base)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(brush)
    )
}

/** A stack of skeleton lines that matches a paragraph. */
@Composable
fun SkeletonLines(
    lines: Int = 3,
    modifier: Modifier = Modifier,
    lineHeight: Dp = 12.dp
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
    ) {
        repeat(lines) { index ->
            SkeletonBlock(
                height = lineHeight,
                modifier = if (index == lines - 1) Modifier.fillMaxWidth(0.6f) else Modifier
            )
        }
    }
}

// ------------------------------------------------------------------ weather

/**
 * Condition to icon, colour and words. The words are always available to accessibility,
 * so the glyph is never the only signal (DESIGN.md 9).
 */
@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    label: String? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val tint = when (condition) {
        WeatherCondition.clear, WeatherCondition.mostlyClear -> colors.solarPrimary
        WeatherCondition.partlyCloudy -> colors.solarStrong
        WeatherCondition.overcast, WeatherCondition.fog -> colors.textTertiary
        WeatherCondition.drizzle, WeatherCondition.rain, WeatherCondition.heavyRain -> colors.gridExportStrong
        WeatherCondition.snow -> colors.textSecondary
        WeatherCondition.thunderstorm -> colors.alertStrong
    }
    val words = label ?: WeatherConditions.label(condition)
    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = words }
    ) {
        drawWeather(condition, tint)
    }
}

/** The 15 weather codes the PWA maps, plus the label each one shows. */
object WeatherConditions {

    /** Open-Meteo WMO code to condition, mirroring `src/lib/weather.ts`. */
    fun conditionFor(code: Int): WeatherCondition = when (code) {
        0 -> WeatherCondition.clear
        1 -> WeatherCondition.mostlyClear
        2 -> WeatherCondition.partlyCloudy
        3 -> WeatherCondition.overcast
        45, 48 -> WeatherCondition.fog
        51, 53, 55 -> WeatherCondition.drizzle
        56, 57 -> WeatherCondition.drizzle
        61, 63, 65 -> if (code == 65) WeatherCondition.heavyRain else WeatherCondition.rain
        66, 67 -> WeatherCondition.rain
        71, 73, 75, 77 -> WeatherCondition.snow
        80, 81, 82 -> WeatherCondition.rain
        85, 86 -> WeatherCondition.snow
        95 -> WeatherCondition.thunderstorm
        96, 99 -> WeatherCondition.thunderstorm
        else -> WeatherCondition.overcast
    }

    fun label(condition: WeatherCondition): String = when (condition) {
        WeatherCondition.clear -> "Clear"
        WeatherCondition.mostlyClear -> "Mostly clear"
        WeatherCondition.partlyCloudy -> "Partly cloudy"
        WeatherCondition.overcast -> "Overcast"
        WeatherCondition.fog -> "Fog"
        WeatherCondition.drizzle -> "Drizzle"
        WeatherCondition.rain -> "Rain"
        WeatherCondition.heavyRain -> "Heavy rain"
        WeatherCondition.snow -> "Snow"
        WeatherCondition.thunderstorm -> "Thunderstorm"
    }
}

/** The glyphs. Drawn, not shipped as assets, so one family stays consistent. */
private fun DrawScope.drawWeather(condition: WeatherCondition, tint: Color) {
    val w = size.width
    val h = size.height
    val stroke = w * 0.09f

    fun sun(radius: Float = w * 0.18f, rays: Int = 8) {
        drawCircle(tint, radius = radius, center = Offset(w / 2f, h / 2f))
        repeat(rays) { index ->
            rotate(degrees = index * (360f / rays), pivot = Offset(w / 2f, h / 2f)) {
                drawLine(
                    color = tint,
                    start = Offset(w / 2f, h / 2f - radius * 1.5f),
                    end = Offset(w / 2f, h / 2f - radius * 2.2f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    fun cloud() {
        drawCircle(tint, radius = w * 0.17f, center = Offset(w * 0.36f, h * 0.56f))
        drawCircle(tint, radius = w * 0.22f, center = Offset(w * 0.56f, h * 0.52f))
        drawCircle(tint, radius = w * 0.15f, center = Offset(w * 0.72f, h * 0.58f))
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.24f, h * 0.56f),
            size = androidx.compose.ui.geometry.Size(w * 0.54f, h * 0.2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(h * 0.1f)
        )
    }

    fun drops(count: Int, length: Float) {
        repeat(count) { index ->
            val x = w * (0.32f + index * 0.18f)
            drawLine(
                color = tint,
                start = Offset(x, h * 0.78f),
                end = Offset(x - w * 0.04f, h * (0.78f + length)),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }

    when (condition) {
        WeatherCondition.clear -> sun()
        WeatherCondition.mostlyClear -> sun(radius = w * 0.15f, rays = 5)
        WeatherCondition.partlyCloudy -> {
            sun(radius = w * 0.12f, rays = 5)
            cloud()
        }
        WeatherCondition.overcast -> cloud()
        WeatherCondition.fog -> {
            repeat(3) { index ->
                val y = h * (0.34f + index * 0.18f)
                drawLine(tint, Offset(w * 0.2f, y), Offset(w * 0.8f, y), strokeWidth = stroke, cap = StrokeCap.Round)
            }
        }
        WeatherCondition.drizzle -> {
            cloud()
            drops(2, 0.06f)
        }
        WeatherCondition.rain -> {
            cloud()
            drops(3, 0.12f)
        }
        WeatherCondition.heavyRain -> {
            cloud()
            drops(3, 0.18f)
        }
        WeatherCondition.snow -> {
            cloud()
            repeat(3) { index ->
                drawCircle(tint, radius = w * 0.05f, center = Offset(w * (0.32f + index * 0.18f), h * 0.88f))
            }
        }
        WeatherCondition.thunderstorm -> {
            cloud()
            val bolt = Path().apply {
                moveTo(w * 0.52f, h * 0.7f)
                lineTo(w * 0.4f, h * 0.92f)
                lineTo(w * 0.52f, h * 0.92f)
                lineTo(w * 0.46f, h * 1.05f)
                lineTo(w * 0.66f, h * 0.84f)
                lineTo(w * 0.54f, h * 0.84f)
                close()
            }
            drawPath(bolt, tint)
        }
    }
}
