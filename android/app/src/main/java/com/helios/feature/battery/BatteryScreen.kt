package com.helios.feature.battery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.ConnectionBanner
import com.helios.core.designsystem.component.ConnectionBannerState
import com.helios.core.designsystem.component.DeniedPermissionState
import com.helios.core.designsystem.component.EmptyState
import com.helios.core.designsystem.component.HeliosErrorState
import com.helios.core.designsystem.component.SkeletonBlock
import com.helios.core.designsystem.component.SurfaceState
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography

/**
 * SCR-10 Battery (tab destination 4 of 5), in the selected direction.
 *
 * Order: what the pack is doing, how long it would carry the house, what condition it is
 * in, and which strategy it is following. The state argument carries every variant, so the
 * screen renders loading, empty, partial, stale, offline, failed, unreadable-readiness and
 * applying states without a device.
 *
 * The screen owns no data: it reads [BatteryScreenState] and reports intent back through
 * its callbacks. `HeliosNavGraph` calls `BatteryScreen()` with the fixture state; a shell
 * that owns state calls `BatteryScreen(state = ...)`.
 *
 * Actions: charge mode row (ACT-050) opens the confirmation dialog, confirm (ACT-051)
 * reports through [onChargeModeConfirmed], readiness disclosure (ACT-053) expands in place,
 * and the freshness stamp or the banner opens the Connection sheet through
 * [onOpenConnection] when a caller supplies it. No control is present without an outcome:
 * the freshness stamp is only interactive when the caller can open the sheet.
 */
@Composable
fun BatteryScreen(
    state: BatteryScreenState = rememberBatteryFixtureState(),
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onChargeModeConfirmed: (ChargeMode) -> Unit = {},
    onOpenConnection: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    var readinessExpanded by remember { mutableStateOf(state.readinessExpanded) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .verticalScroll(rememberScrollState())
    ) {
        BatteryHeader(state)

        val failure = state.failure

        when {
            state.surfaceState == SurfaceState.LOADING -> BatterySkeleton()

            state.surfaceState == SurfaceState.EMPTY -> Block {
                EmptyState(
                    title = "No battery on this system",
                    message = "The inverter reports no battery cabinet, so state of charge and " +
                        "backup reserve are unavailable. Production and grid readings still work."
                )
            }

            /*
             * A classified failure with nothing to fall back on. The surface is not
             * rendered from an empty reading: the alert row of FLW-03 says why the link
             * failed, and the error surface says which reading is missing and when it was
             * last good. Showing "No data" tiles in place of a battery reading would dress
             * a failure up as a value (FLW-03 hard rules).
             */
            failure != null && state.reading == null -> {
                Block {
                    ConnectionBanner(
                        state = if (failure.kind.linkDown) {
                            ConnectionBannerState.OFFLINE
                        } else {
                            ConnectionBannerState.RECONNECTING
                        },
                        message = failure.message,
                        reason = listOfNotNull(failure.detail, state.lastGoodLabel)
                            .joinToString(" \u00B7 ")
                            .ifEmpty { null },
                        onRetry = onRetry,
                        onOpenConnection = onOpenConnection
                    )
                }
                Block {
                    if (failure.kind.needsPermission) {
                        DeniedPermissionState(
                            title = failure.kind.defaultMessage,
                            message = "The reading needs the inverter link. The last known values " +
                                "and the rest of the app keep working.",
                            actionLabel = failure.action,
                            onAction = onRetry
                        )
                    } else {
                        HeliosErrorState(
                            title = "Battery reading unavailable",
                            message = failure.message,
                            detail = state.lastGoodLabel
                                ?: "The inverter did not answer the last poll.",
                            actionLabel = failure.action,
                            onAction = onRetry
                        )
                    }
                }
            }

            else -> {
                if (state.dimmed) {
                    Block {
                        ConnectionBanner(
                            state = if (state.failure != null) {
                                ConnectionBannerState.OFFLINE
                            } else {
                                ConnectionBannerState.RECONNECTING
                            },
                            message = state.failureMessage ?: "Showing the last good reading",
                            reason = listOfNotNull(state.failureDetail, state.lastGoodLabel)
                                .joinToString(" \u00B7 ")
                                .ifEmpty { null },
                            onRetry = onRetry,
                            onOpenConnection = onOpenConnection
                        )
                    }
                }

                StaggeredEntrance(step = 0, reducedMotion = state.reducedMotion) {
                    Block {
                        BatteryHeroCard(
                            state = state,
                            onOpenConnection = onOpenConnection
                        )
                    }
                }

                StaggeredEntrance(step = 1, reducedMotion = state.reducedMotion) {
                    Block {
                        BackupReadinessCard(
                            state = state,
                            expanded = readinessExpanded,
                            onExpandedChange = { readinessExpanded = it }
                        )
                    }
                }

                StaggeredEntrance(step = 1, reducedMotion = state.reducedMotion) {
                    Block { BatteryHealthGrid(state = state) }
                }

                StaggeredEntrance(step = 2, reducedMotion = state.reducedMotion) {
                    Block {
                        ChargeModeSection(
                            state = state,
                            onConfirm = onChargeModeConfirmed,
                            onRetry = onRetry
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(HeliosSpacing.LayoutMetrics.bottomNavHeight))
    }
}

/** Identity and the pack line: capacity, cycles and health in one sentence (SCR-10). */
@Composable
private fun BatteryHeader(state: BatteryScreenState) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = HeliosSpacing.gutter,
                end = HeliosSpacing.gutter,
                top = HeliosSpacing.space5,
                bottom = HeliosSpacing.sectionRhythm
            )
    ) {
        Text(
            text = "ENERGY STORAGE",
            style = HeliosTypography.caption2,
            color = colors.textTertiary
        )
        Spacer(Modifier.height(HeliosSpacing.space2))
        Text(
            text = "Battery",
            style = HeliosTypography.title2,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(HeliosSpacing.space1))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = state.packLine,
                style = HeliosTypography.callout,
                color = colors.textSecondary
            )
            state.sourceQualifier?.let { qualifier ->
                Spacer(Modifier.size(HeliosSpacing.space2))
                Text(
                    text = qualifier,
                    style = HeliosTypography.caption2,
                    color = colors.textTertiary
                )
            }
        }
    }
}

/** One section of the screen, on the shared gutter and section rhythm. */
@Composable
private fun Block(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HeliosSpacing.gutter, vertical = HeliosSpacing.space3)
    ) {
        content()
    }
}

/** The loading shape: the ring, the strip, the readiness bar and the mode rows. */
@Composable
private fun BatterySkeleton() {
    val colors = LocalHeliosSemanticColors.current
    Block {
        BatteryCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(216.dp)
                        .clip(HeliosShape.full)
                        .background(colors.skeleton.base.copy(alpha = 0.35f))
                )
                Spacer(Modifier.height(HeliosSpacing.space3))
                SkeletonBlock(height = 12.dp, modifier = Modifier.size(width = 120.dp, height = 12.dp))
            }
            BatteryHairline()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(3) {
                    SkeletonBlock(height = 14.dp, modifier = Modifier.size(width = 72.dp, height = 14.dp))
                }
            }
        }
    }
    Block {
        SkeletonBlock(height = 120.dp, shape = HeliosShape.lg)
    }
    Block {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
        ) {
            SkeletonBlock(height = 96.dp, shape = HeliosShape.md, modifier = Modifier.weight(1f))
            SkeletonBlock(height = 96.dp, shape = HeliosShape.md, modifier = Modifier.weight(1f))
        }
    }
    Block {
        SkeletonBlock(height = 70.dp, shape = HeliosShape.md)
    }
    Block {
        SkeletonBlock(height = 70.dp, shape = HeliosShape.md)
    }
}
