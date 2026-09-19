package com.helios.debug.gallery

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.data.fixture.FixtureScenario
import com.helios.core.data.fixture.FixtureServicesFamily
import com.helios.core.designsystem.color.CarbonColors
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.color.PaperColors
import com.helios.core.designsystem.component.HeliosChip
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.HeliosMark
import com.helios.core.designsystem.component.SectionHeader
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.ui.theme.HeliosTheme

/**
 * Debug-only component gallery.
 *
 * Every reused component in every variant and state the design lists, with the fixture
 * services behind it, so a whole page is a review of a component rather than an example
 * of it. Nothing here can reach a release build: it lives in the debug source set, and it
 * does not touch navigation. The entry point is this composable; [GalleryActivity] is a
 * debug-only host that exists so a page can be opened and captured.
 *
 * The scenario switch drives the fixture family, which is how one screen demonstrates the
 * loading, empty, partial, stale, offline, error, denied-permission and long-content
 * states with no network, no inverter and no permission dialog.
 */
@Composable
fun ComponentGalleryScreen(
    page: Int = 0,
    onPageChange: (Int) -> Unit = {},
    initialScenario: FixtureScenario = FixtureScenario.LIVE,
    darkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    var scenario by remember { mutableStateOf(initialScenario) }
    var isDark by remember { mutableStateOf(darkTheme) }
    var demoMode by remember { mutableStateOf(true) }

    SideEffect {
        FixtureServicesFamily.INSTANCE.select(scenario)
        FixtureServicesFamily.INSTANCE.setDemoMode(demoMode)
    }

    HeliosTheme(darkTheme = isDark, colors = if (isDark) CarbonColors else PaperColors) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = LocalHeliosSemanticColors.current.backgroundPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                GalleryHeader(
                    page = page,
                    scenario = scenario,
                    darkTheme = isDark,
                    demoMode = demoMode,
                    onScenario = { scenario = it },
                    onTheme = { isDark = it },
                    onDemo = { demoMode = it },
                    onPageChange = onPageChange
                )
                val context = GalleryContext(scenario = scenario, darkTheme = isDark, demoMode = demoMode)
                galleryPages.getOrNull(page)?.forEach { section ->
                    GallerySection(title = section.title, context = context) {
                        section.content(context)
                    }
                }
                GalleryEndMarker(page = page)
            }
        }
    }
}

/** The last thing on a page: its presence is how a capture proves nothing was clipped. */
@Composable
fun GalleryEndMarker(page: Int) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = HeliosSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space1)
    ) {
        Text(
            text = "End of page ${page + 1} of $GALLERY_PAGE_COUNT",
            style = HeliosTypography.caption2,
            color = colors.textTertiary
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(GALLERY_END_MARKER_COLOR)
        )
        Text(
            text = "Marker colour #FF00FF is checked by the capture script.",
            style = HeliosTypography.caption2,
            color = colors.textTertiary
        )
    }
}

/** The capture marker. Nothing else in the gallery, or in the app, uses this colour. */
val GALLERY_END_MARKER_COLOR: Color = Color(0xFFFF00FF)

@Composable
private fun GalleryHeader(
    page: Int,
    scenario: FixtureScenario,
    darkTheme: Boolean,
    demoMode: Boolean,
    onScenario: (FixtureScenario) -> Unit,
    onTheme: (Boolean) -> Unit,
    onDemo: (Boolean) -> Unit,
    onPageChange: (Int) -> Unit
) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.backgroundSecondary)
            .padding(horizontal = HeliosSpacing.space4, vertical = HeliosSpacing.space3)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeliosMark(size = 22.dp)
            Spacer(Modifier.width(HeliosSpacing.space2))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Component gallery",
                    style = HeliosTypography.headline,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Page ${page + 1} of $GALLERY_PAGE_COUNT",
                    style = HeliosTypography.caption,
                    color = colors.textTertiary
                )
            }
            HeliosGhostButton(text = if (darkTheme) "Carbon" else "Paper", onClick = { onTheme(!darkTheme) })
            HeliosGhostButton(text = if (demoMode) "Demo on" else "Demo off", onClick = { onDemo(!demoMode) })
        }
        Spacer(Modifier.height(HeliosSpacing.space2))
        Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)) {
            FixtureScenario.entries.take(5).forEach { entry ->
                HeliosChip(
                    label = entry.label,
                    selected = entry == scenario,
                    onClick = { onScenario(entry) }
                )
            }
        }
        Spacer(Modifier.height(HeliosSpacing.space1))
        Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)) {
            FixtureScenario.entries.drop(5).forEach { entry ->
                HeliosChip(
                    label = entry.label,
                    selected = entry == scenario,
                    onClick = { onScenario(entry) }
                )
            }
        }
        Spacer(Modifier.height(HeliosSpacing.space1))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space1)
        ) {
            HeliosGhostButton(text = "Prev", onClick = { onPageChange((page - 1 + GALLERY_PAGE_COUNT) % GALLERY_PAGE_COUNT) })
            repeat(GALLERY_PAGE_COUNT.coerceAtMost(24)) { index ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (index == page) colors.accentPrimary else colors.backgroundTertiary)
                        .semantics { contentDescription = "Page ${index + 1}" }
                )
            }
            HeliosGhostButton(text = "Next", onClick = { onPageChange((page + 1) % GALLERY_PAGE_COUNT) })
        }
    }
}

/** Everything a section needs: the scenario, the theme and the demo flag. */
data class GalleryContext(
    val scenario: FixtureScenario,
    val darkTheme: Boolean,
    val demoMode: Boolean
)

/** A titled block, so a capture shows which component it is showing. */
@Composable
fun GallerySection(
    title: String,
    context: GalleryContext,
    content: @Composable () -> Unit
) {
    val colors = LocalHeliosSemanticColors.current
    Column(modifier = Modifier.padding(horizontal = HeliosSpacing.space4, vertical = HeliosSpacing.space2)) {
        SectionHeader(
            title = title,
            eyebrow = "Scenario: ${context.scenario.label}" + if (context.demoMode) " \u00B7 demo" else ""
        )
        Spacer(Modifier.height(HeliosSpacing.space2))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(HeliosShape.md)
                .background(colors.backgroundSecondary)
                .padding(HeliosSpacing.space3),
            verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
        ) {
            content()
        }
    }
}
