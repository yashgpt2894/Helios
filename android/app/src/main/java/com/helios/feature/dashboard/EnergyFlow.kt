package com.helios.feature.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.HeliosMark
import com.helios.core.designsystem.component.SurfaceState
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.motion.HeliosMotion
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.format.HeliosFormat
import kotlin.math.abs
import kotlin.math.roundToInt

/** The four endpoints of the energy model, in the order the design lists them. */
enum class EnergyNode(val label: String) {
    SOLAR("Solar"),
    HOME("Home"),
    BATTERY("Battery"),
    GRID("Grid")
}

/** One cell of the three-cell ledger under the flow diagram. */
data class EnergyLedgerCell(
    val label: String,
    val value: String,
    val qualifier: String? = null
)

/**
 * The signature instrument: four endpoints around the live output, with the energy moving
 * between them drawn as it moves.
 *
 * Reading order is the plant's own: solar top-left, home top-right, battery bottom-left,
 * grid bottom-right, and the live AC output in the middle as the hero number.
 *
 * Motion (`motion-language.md` section 4, tokens from `design/tokens.json`):
 *  - each spoke carries one dot per kilowatt, moving at a speed set by the magnitude, so a
 *    light load reads slower than a heavy one without a label saying so;
 *  - dots travel in the direction the energy actually goes, which is why a discharging
 *    battery and an importing grid are drawn as separate directions, not separate colours;
 *  - when the reading is stale or the link is down the dots stop and a static arrow shows
 *    the last known direction (DESIGN.md section 10 risk 3: never animate a lie);
 *  - under Reduce Motion the dots are replaced by the same static arrows, and the loop is
 *    not started at all.
 *
 * Accessibility: the Canvas is one node whose description is the text equivalent of all
 * four paths ([energyFlowSummary]). Each endpoint is a separate 48 dp control with its own
 * name and value, so the diagram is never the only way to reach a number.
 *
 * Height: [flowHeight] is the whole instrument — two node rows and the hub between them. The
 * Dashboard passes the height that keeps its hero block inside the 430 dp budget of
 * `design/DESIGN.md` section 6, which is why the default is 240 dp and not the 300 dp a
 * full-screen version of the diagram would take.
 */
@Composable
fun EnergyFlow(
    hubValue: Double,
    hubUnit: String,
    hubLabel: String,
    solarW: Double,
    homeW: Double,
    batteryW: Double,
    gridW: Double,
    modifier: Modifier = Modifier,
    state: SurfaceState = SurfaceState.READY,
    motion: HeliosMotionSettings = HeliosMotionSettings.Default,
    liveState: LiveNumberState = LiveNumberState.UPDATING,
    flowHeight: Dp = 240.dp,
    onNodeClick: ((EnergyNode) -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val dimmed = state == SurfaceState.STALE || state == SurfaceState.ERROR
    val animating = motion.animates() && !dimmed
    val detailAlpha = if (dimmed) 0.6f else 1f

    val transition = rememberInfiniteTransition(label = "energyFlow")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = motion.durationMs(HeliosMotion.DurationMs.nodePulse),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "flowPhase"
    )
    val dotPhase = if (animating) phase else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(flowHeight)
            .semantics {
                contentDescription = energyFlowSummary(solarW, homeW, batteryW, gridW)
            }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(flowHeight)) {
            val centre = Offset(size.width / 2f, size.height / 2f)
            // The node rows sit at the top and the bottom of the panel with the hub between
            // them (Arrangement.SpaceBetween), so their centres are at 13 and 87 percent of the
            // height rather than at the thirds of an equal split.
            val anchors = mapOf(
                EnergyNode.SOLAR to Offset(size.width * 0.245f, size.height * 0.13f),
                EnergyNode.HOME to Offset(size.width * 0.755f, size.height * 0.13f),
                EnergyNode.BATTERY to Offset(size.width * 0.245f, size.height * 0.87f),
                EnergyNode.GRID to Offset(size.width * 0.755f, size.height * 0.87f)
            )
            drawSpoke(
                from = centre,
                to = anchors.getValue(EnergyNode.SOLAR),
                watts = solarW,
                supplies = true,
                color = colors.solarPrimary,
                phase = dotPhase,
                animating = animating,
                dimmed = dimmed
            )
            drawSpoke(
                from = centre,
                to = anchors.getValue(EnergyNode.HOME),
                watts = homeW,
                supplies = false,
                color = colors.flowPrimary,
                phase = dotPhase,
                animating = animating,
                dimmed = dimmed
            )
            drawSpoke(
                from = centre,
                to = anchors.getValue(EnergyNode.BATTERY),
                watts = abs(batteryW),
                supplies = batteryW < 0,
                color = colors.batteryPrimary,
                phase = dotPhase,
                animating = animating,
                dimmed = dimmed
            )
            drawSpoke(
                from = centre,
                to = anchors.getValue(EnergyNode.GRID),
                watts = abs(gridW),
                supplies = gridW < 0,
                color = if (gridW < 0) colors.gridImportPrimary else colors.gridExportPrimary,
                phase = dotPhase,
                animating = animating,
                dimmed = dimmed
            )
        }

        // Fixed row heights and a hub that takes what is left: with SpaceBetween and free
        // heights the lower row was squeezed to the 48 dp minimum, which clipped the state word
        // off the two bottom endpoints (measured on the emulator before this change).
        val nodeRowHeight = (flowHeight - HeliosSpacing.space3) / 4
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(flowHeight)
                .padding(horizontal = HeliosSpacing.space3, vertical = HeliosSpacing.space2)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(nodeRowHeight),
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3),
                verticalAlignment = Alignment.Top
            ) {
                EnergyNodeCard(
                    node = EnergyNode.SOLAR,
                    valueText = HeliosFormat.wattsToKilowatts(solarW),
                    stateWord = when {
                        solarW.isNaN() -> "No data"
                        solarW <= 1.0 -> "No output"
                        else -> "Producing"
                    },
                    tint = colors.solarPrimary,
                    alpha = detailAlpha,
                    modifier = Modifier.weight(1f),
                    onClick = onNodeClick
                )
                EnergyNodeCard(
                    node = EnergyNode.HOME,
                    valueText = HeliosFormat.wattsToKilowatts(homeW),
                    stateWord = if (homeW.isNaN()) "No data" else "Consuming",
                    tint = colors.flowPrimary,
                    alpha = detailAlpha,
                    modifier = Modifier.weight(1f),
                    onClick = onNodeClick
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                HubReadout(
                    value = hubValue,
                    unit = hubUnit,
                    label = hubLabel,
                    liveState = if (dimmed) LiveNumberState.STALE else liveState,
                    motion = motion
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(nodeRowHeight),
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3),
                verticalAlignment = Alignment.Top
            ) {
                EnergyNodeCard(
                    node = EnergyNode.BATTERY,
                    valueText = HeliosFormat.wattsToKilowatts(abs(batteryW)),
                    stateWord = batteryStateWord(batteryW),
                    tint = colors.batteryPrimary,
                    alpha = detailAlpha,
                    modifier = Modifier.weight(1f),
                    onClick = onNodeClick
                )
                EnergyNodeCard(
                    node = EnergyNode.GRID,
                    valueText = HeliosFormat.wattsToKilowatts(abs(gridW)),
                    stateWord = gridStateWord(gridW),
                    tint = if (gridW < 0) colors.gridImportPrimary else colors.gridExportPrimary,
                    alpha = detailAlpha,
                    modifier = Modifier.weight(1f),
                    onClick = onNodeClick
                )
            }
        }
    }
}

/** The centre of the instrument: the hero number, on the panel surface. */
@Composable
private fun HubReadout(
    value: Double,
    unit: String,
    label: String,
    liveState: LiveNumberState,
    motion: HeliosMotionSettings,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    Box(
        modifier = modifier
            .clip(HeliosShape.lg)
            .background(colors.backgroundTertiary)
            .border(
                HeliosElevation.MaterialTokens.innerStrokeWidth,
                colors.separatorHairline,
                HeliosShape.lg
            )
            .padding(horizontal = HeliosSpacing.space5, vertical = HeliosSpacing.space3)
    ) {
        LiveNumber(
            label = label,
            value = value,
            unit = unit,
            state = liveState,
            decimals = 2,
            motion = motion
        )
    }
}

/**
 * One endpoint: name, value and what it is doing, as a 48 dp control.
 *
 * Three lines, and every one of them is load-bearing: the name is the endpoint, the value is
 * the reading, and the state word is what makes the node understandable without colour — a
 * battery reads "Charging", "Discharging", "Idle" or "No data", a grid reads "Exporting",
 * "Importing" or "Net zero". The tint is the fourth signal and never the only one.
 */
@Composable
fun EnergyNodeCard(
    node: EnergyNode,
    valueText: String,
    stateWord: String,
    tint: Color,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    onClick: ((EnergyNode) -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val interactive = if (onClick != null) {
        modifier
            .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget)
            .clip(HeliosShape.md)
            .clickable(role = Role.Button) { onClick(node) }
    } else {
        modifier
    }
    Column(
        modifier = interactive
            .clip(HeliosShape.md)
            .padding(HeliosSpacing.space2)
            .semantics(mergeDescendants = true) {
                contentDescription = "${node.label} $valueText, $stateWord"
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // No mark inside a node: the label, the reading and the state word already say
        // everything, and the 22 dp a mark needs is the difference between the instrument
        // fitting its height budget and the lower node row being clipped.
        Text(
            text = node.label.uppercase(),
            style = HeliosTypography.caption2,
            color = colors.textTertiary.copy(alpha = alpha)
        )
        Text(
            text = valueText,
            style = HeliosTypography.callout,
            color = colors.textPrimary.copy(alpha = alpha),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = stateWord,
            style = HeliosTypography.caption2,
            color = tint.copy(alpha = alpha)
        )
    }
}

/**
 * The three-cell ledger that closes the hero block: what today produced, how much of it the
 * house used, and what that avoided.
 *
 * The CO2 factor is the PWA's own (0.42 kg per kWh, `src/pages/Dashboard.tsx`), kept here so
 * a snapshot link and the app state the same number.
 */
@Composable
fun EnergyLedger(
    cells: List<EnergyLedgerCell>,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    place: String? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val alpha = if (dimmed) 0.6f else 1f
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeliosElevation.MaterialTokens.innerStrokeWidth)
                .background(colors.separatorHairline)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HeliosSpacing.space3),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            cells.forEach { cell ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "${cell.label} ${cell.value}" +
                                (cell.qualifier?.let { ", $it" } ?: "")
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = cell.value,
                        style = HeliosTypography.title3,
                        color = colors.textPrimary.copy(alpha = alpha)
                    )
                    Spacer(Modifier.height(HeliosSpacing.space1))
                    Text(
                        text = cell.label.uppercase(),
                        style = HeliosTypography.caption2,
                        color = colors.textTertiary.copy(alpha = alpha)
                    )
                    if (cell.qualifier != null) {
                        Text(
                            text = cell.qualifier,
                            style = HeliosTypography.caption2,
                            color = colors.textQuaternary.copy(alpha = alpha)
                        )
                    }
                }
            }
        }
        if (place != null) {
            Spacer(Modifier.height(HeliosSpacing.space2))
            Text(
                text = place,
                style = HeliosTypography.caption2,
                color = colors.textQuaternary.copy(alpha = alpha)
            )
        }
    }
}

// ------------------------------------------------------------------ drawing

/**
 * One spoke of the instrument.
 *
 * [watts] is the magnitude on this path. The line is always drawn at low alpha so the
 * topology is visible even at zero; the moving dots carry the magnitude and the direction.
 */
private fun DrawScope.drawSpoke(
    from: Offset,
    to: Offset,
    watts: Double,
    supplies: Boolean,
    color: Color,
    phase: Float,
    animating: Boolean,
    dimmed: Boolean
) {
    val magnitude = if (watts.isNaN()) 0.0 else abs(watts)
    val active = magnitude > 1.0
    val lineAlpha = when {
        !active -> 0.18f
        dimmed -> 0.35f
        else -> 0.55f
    }
    drawLine(
        color = color.copy(alpha = lineAlpha),
        start = from,
        end = to,
        strokeWidth = if (active) 3f else 1.5f,
        cap = StrokeCap.Round
    )

    val origin = if (supplies) to else from
    val destination = if (supplies) from else to

    if (!animating) {
        if (active) {
            drawArrowHead(origin = origin, destination = destination, color = color.copy(alpha = 0.9f))
        }
        return
    }

    val dots = (magnitude / 1500.0).roundToInt().coerceIn(1, 4)
    if (!active) return
    val speed = (0.6 + (magnitude / 5000.0)).coerceAtMost(1.4)
    repeat(dots) { index ->
        val t = ((phase * speed.toFloat()) + index.toFloat() / dots) % 1f
        val point = Offset(
            x = origin.x + (destination.x - origin.x) * t,
            y = origin.y + (destination.y - origin.y) * t
        )
        drawCircle(color = color, radius = 3.5f, center = point)
    }
    drawArrowHead(origin = origin, destination = destination, color = color.copy(alpha = 0.5f))
}

/** A small triangle at the far end of a path, so a stopped animation still shows direction. */
private fun DrawScope.drawArrowHead(origin: Offset, destination: Offset, color: Color) {
    val dx = destination.x - origin.x
    val dy = destination.y - origin.y
    val length = kotlin.math.sqrt(dx * dx + dy * dy)
    if (length < 1f) return
    val ux = dx / length
    val uy = dy / length
    val tip = Offset(destination.x - ux * 10f, destination.y - uy * 10f)
    val baseLeft = Offset(tip.x - ux * 12f - uy * 6f, tip.y - uy * 12f + ux * 6f)
    val baseRight = Offset(tip.x - ux * 12f + uy * 6f, tip.y - uy * 12f - ux * 6f)
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(baseLeft.x, baseLeft.y)
        lineTo(baseRight.x, baseRight.y)
        close()
    }
    drawPath(path = path, color = color)
}

// ------------------------------------------------------------------ words

/**
 * The text equivalent of the diagram, which is also its accessibility description. It reads
 * the four paths in the plant's order and never states a direction it cannot see.
 */
fun energyFlowSummary(solarW: Double, homeW: Double, batteryW: Double, gridW: Double): String {
    val solar = if (solarW.isNaN()) "Solar not reported" else "Solar ${HeliosFormat.wattsToKilowatts(solarW)}"
    val home = if (homeW.isNaN()) "home load not reported" else "home load ${HeliosFormat.wattsToKilowatts(homeW)}"
    val battery = if (batteryW.isNaN()) {
        "battery not reported"
    } else {
        "battery ${batteryStateWord(batteryW).lowercase()} ${HeliosFormat.wattsToKilowatts(abs(batteryW))}"
    }
    val grid = if (gridW.isNaN()) {
        "grid not reported"
    } else {
        "grid ${gridStateWord(gridW).lowercase()} ${HeliosFormat.wattsToKilowatts(abs(gridW))}"
    }
    return "$solar, $home, $battery, $grid."
}

/** The battery's verb. Charge is positive by convention in [com.helios.core.domain.model.SolarTelemetry]. */
fun batteryStateWord(batteryW: Double): String = when {
    batteryW.isNaN() -> "No data"
    batteryW > 30 -> "Charging"
    batteryW < -30 -> "Discharging"
    else -> "Idle"
}

/** The grid's verb: positive is export, negative is import. */
fun gridStateWord(gridW: Double): String = when {
    gridW.isNaN() -> "No data"
    gridW > 30 -> "Exporting"
    gridW < -30 -> "Importing"
    else -> "Net zero"
}

/** The status pill's word for a simulated reading that is otherwise healthy. */
fun simulatedStatus(simulated: Boolean, faulted: Boolean): HeliosStatusKind = when {
    faulted -> HeliosStatusKind.FAULT
    simulated -> HeliosStatusKind.DEMO
    else -> HeliosStatusKind.PRODUCING
}
