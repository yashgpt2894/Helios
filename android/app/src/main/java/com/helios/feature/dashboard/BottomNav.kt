package com.helios.feature.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.HeliosMark
import com.helios.core.designsystem.haptics.HapticAction
import com.helios.core.designsystem.haptics.HeliosHaptics
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.motion.HeliosMotion
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography

/** One destination in the navigation bar. */
data class NavTab(
    val label: String,
    val route: String,
    val badgeCount: Int? = null
)

/** The five destinations, in the order the design fixes (FPM section 1). */
object HeliosDestinations {
    const val HOME = "home"
    const val SOLAR = "solar"
    const val INSIGHTS = "insights"
    const val BATTERY = "battery"
    const val SETTINGS = "settings"

    fun default(insightBadge: Int? = null): List<NavTab> = listOf(
        NavTab(label = "Home", route = HOME),
        NavTab(label = "Solar", route = SOLAR),
        NavTab(label = "Insights", route = INSIGHTS, badgeCount = insightBadge),
        NavTab(label = "Battery", route = BATTERY),
        NavTab(label = "Settings", route = SETTINGS)
    )
}

/**
 * Five destinations: Home, Solar, Insights, Battery, Settings.
 *
 * States: selected (accent label, a filled indicator behind the mark, and a rule under the
 * label so selection is never colour-only), unselected, and a badge on a destination that
 * needs attention. Every destination keeps its label at every text scale, because an
 * icon-only bar fails the "never colour or glyph alone" rule (DESIGN.md section 9).
 *
 * Motion: the indicator and the rule fade with the snappy token spring, and a tap fires the
 * selection haptic from the token table. Reduce Motion replaces the spring with the token
 * cross-fade; the animation scale still applies to it.
 *
 * The screen draws this bar only when its host supplies [onTabSelected]: navigation belongs
 * to the shell, and a bar whose taps went nowhere would be a dead control.
 */
@Composable
fun BottomNav(
    tabs: List<NavTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    motion: HeliosMotionSettings = HeliosMotionSettings.Default
) {
    val colors = LocalHeliosSemanticColors.current
    val view = LocalView.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.backgroundSecondary,
        tonalElevation = HeliosElevation.level3
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HeliosElevation.MaterialTokens.innerStrokeWidth)
                    .background(colors.separatorHairline)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = HeliosSpacing.space2),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = index == selectedIndex
                    val selection by animateFloatAsState(
                        targetValue = if (selected) 1f else 0f,
                        animationSpec = motion.springOr(HeliosMotion.snappy),
                        label = "tabSelection"
                    )
                    val color = if (selected) colors.accentPrimary else colors.textTertiary
                    Column(
                        modifier = Modifier
                            .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget)
                            .clip(HeliosShape.md)
                            .clickable(role = Role.Tab) {
                                HeliosHaptics.perform(view, HapticAction.TILE_TAP)
                                onTabSelected(index)
                            }
                            .padding(horizontal = HeliosSpacing.space2, vertical = HeliosSpacing.space1)
                            .semantics(mergeDescendants = true) {
                                this.selected = selected
                                contentDescription = buildString {
                                    append(tab.label)
                                    if (selected) append(", selected")
                                    if (tab.badgeCount != null && tab.badgeCount > 0) {
                                        append(", ${tab.badgeCount} need attention")
                                    }
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(colors.accentSubtle.copy(alpha = selection * 0.9f)),
                                contentAlignment = Alignment.Center
                            ) {
                                HeliosMark(size = 20.dp, color = color)
                            }
                            val badge = tab.badgeCount
                            if (badge != null && badge > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(colors.alertPrimary)
                                )
                            }
                        }
                        Spacer(Modifier.height(HeliosSpacing.space1))
                        Text(
                            text = tab.label,
                            style = HeliosTypography.caption2,
                            color = color,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(2.dp)
                                .clip(HeliosShape.full)
                                .background(
                                    if (selected) colors.accentPrimary.copy(alpha = selection)
                                    else colors.accentPrimary.copy(alpha = 0f)
                                )
                        )
                    }
                }
            }
        }
    }
}
