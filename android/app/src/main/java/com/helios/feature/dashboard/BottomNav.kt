package com.helios.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.HeliosMark
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography

/**
 * Bottom navigation with five destinations: Home, Solar, Insights, Battery, Settings.
 *
 * States: selected (label in the accent colour, a filled indicator, and an icon that is
 * never the only signal because the label is always present), unselected, and a badge for
 * a destination that needs attention.
 *
 * This composable is complete but is not composed by any screen in this pass: navigation
 * belongs to a later step (core/nav is out of scope here).
 */
data class NavTab(
    val label: String,
    val route: String,
    val badgeCount: Int? = null
)

/** The five destinations, in order. */
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

@Composable
fun BottomNav(
    tabs: List<NavTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.backgroundSecondary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = HeliosSpacing.space2),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                val color = if (selected) colors.accentPrimary else colors.textTertiary
                Column(
                    modifier = Modifier
                        .clickable(role = Role.Tab) { onTabSelected(index) }
                        .padding(horizontal = HeliosSpacing.space2, vertical = HeliosSpacing.space1)
                        .semantics(mergeDescendants = true) {
                            contentDescription = buildString {
                                append(tab.label)
                                if (selected) append(", selected")
                                if (tab.badgeCount != null) append(", ${tab.badgeCount} need attention")
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        HeliosMark(size = 20.dp, color = color)
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
                    if (selected) {
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 20.dp, height = 2.dp)
                                .clip(HeliosShape.full)
                                .background(colors.accentPrimary)
                        )
                    }
                }
            }
        }
    }
}
