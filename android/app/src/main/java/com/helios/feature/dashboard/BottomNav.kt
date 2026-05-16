package com.helios.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.HeliosColor
import com.helios.core.designsystem.shape.HeliosShape

/**
 * BottomNav: fixed 4-tab bottom navigation bar.
 */
data class NavTab(
    val label: String,
    val icon: ImageVector? = null,
    val route: String
)

@Composable
fun BottomNav(
    tabs: List<NavTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = true
    val selectedColor = HeliosColor.Solar.ramp(500, isDark)
    val unselectedColor = HeliosColor.Neutral.ramp(600, isDark)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 0.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(index) },
                    icon = {
                        if (tab.icon != null) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (selected) selectedColor else unselectedColor
                            )
                        } else {
                            // Placeholder for now—icons load lazily
                            Text(
                                text = tab.label.take(1),
                                color = if (selected) selectedColor else unselectedColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) selectedColor else unselectedColor,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = selectedColor.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}
