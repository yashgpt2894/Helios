package com.helios.feature.landing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.helios.core.data.service.AppServices
import com.helios.core.data.service.ServiceGraph
import com.helios.core.designsystem.color.LocalHeliosSemanticColors

/**
 * The app's start surface: first-run onboarding (FLW-01, SCR-02 to SCR-06).
 *
 * It replaces the earlier 44-line welcome stub, which had no visible action at all.
 *
 * Light and dark are the shell's decision, not this surface's: `MainActivity` (and the step
 * that owns `core/nav`) applies `HeliosTheme` from the stored theme, and a screen that
 * re-applied it would fight that host - a capture host that asks for the light palette would
 * get the dark one. So this surface reads the palette it is given and only paints its own
 * background with it. The stored theme is still applied before the first frame; it is applied
 * at the root, which is where a single application can be guaranteed.
 *
 * [onFinished] is the shell's navigation: the app passes a lambda that opens the dashboard
 * (SCR-07). When no lambda is supplied - a preview, a capture host, or a build whose
 * navigation is not wired yet - the completion step shows the summary and its dashboard
 * action is omitted rather than rendered as a control that does nothing.
 */
@Composable
fun LandingScreen(
    modifier: Modifier = Modifier,
    onFinished: ((OnboardingOutcome) -> Unit)? = null,
    services: AppServices = ServiceGraph.current,
    initialStep: OnboardingStep = OnboardingStep.WELCOME
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = LocalHeliosSemanticColors.current.backgroundPrimary
    ) {
        OnboardingScreen(
            services = services,
            onFinished = onFinished,
            initialStep = initialStep
        )
    }
}
