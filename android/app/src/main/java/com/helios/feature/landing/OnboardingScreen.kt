package com.helios.feature.landing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.helios.core.data.service.AppServices
import com.helios.core.data.service.FailureKind
import com.helios.core.data.service.HeliosThemeMode
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.ServiceFailure
import com.helios.core.data.service.ServiceGraph
import com.helios.core.data.service.valueOrNull
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.motion.HeliosMotion
import com.helios.core.domain.model.ConnectionConfig
import com.helios.core.format.HeliosFormat
import com.helios.feature.settings.ConnectionDraft
import com.helios.feature.settings.CoordinateEntrySheet
import com.helios.feature.settings.DEFAULT_POLL_INTERVAL_MS
import com.helios.feature.settings.LocationOutcome
import com.helios.feature.settings.locationOutcome
import com.helios.feature.settings.openAppSettings
import com.helios.feature.settings.validateConnectionDraft
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * FLW-01 first run, as one surface with real steps and real back behaviour.
 *
 * The order is the design's, and the point of it is that nothing is asked for before
 * something works: page 1 shows a live reading and offers the demo path in one tap, page 2
 * explains what leaves the device, page 3 asks for the inverter but can be skipped for the
 * demo system, page 4 shows what the probe actually proved, and only then is the location
 * asked for - skippable, with the permission requested from the button that needs it, and
 * with a denied result that keeps a working default.
 *
 * Back moves one step and never leaves the app before the first page; the system back
 * gesture and the returned step are the same code path. Nothing is persisted here: the flow
 * reports an [OnboardingOutcome] to its host, which owns first-run state, and the service
 * calls it makes (`setDemoMode`, `connect`, `setManual`) are the same ones Settings uses.
 */
@Composable
fun OnboardingScreen(
    services: AppServices = ServiceGraph.current,
    modifier: Modifier = Modifier,
    onFinished: ((OnboardingOutcome) -> Unit)? = null,
    initialStep: OnboardingStep = OnboardingStep.WELCOME
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    // RTE-02 to RTE-06 are addressable routes, so a host may open the flow at a step: the
    // settings about screen re-runs setup, and a deep link can target the connect page.
    var step by rememberSaveable { mutableStateOf(initialStep) }
    var visitedResult by rememberSaveable { mutableStateOf(initialStep == OnboardingStep.RESULT) }
    var draft by rememberSaveable(stateSaver = connectionDraftSaver) { mutableStateOf(ConnectionDraft()) }
    var probe by remember { mutableStateOf<ProbeState>(ProbeState.Idle) }
    var testedConfig by remember { mutableStateOf<ConnectionConfig?>(null) }
    var locationState by remember { mutableStateOf<LocationOutcome?>(null) }
    var coordinatesOpen by remember { mutableStateOf(false) }
    var outcome by remember { mutableStateOf<OnboardingOutcome?>(null) }
    var elapsedSeconds by remember { mutableStateOf(0) }

    val reading by services.telemetry.telemetry.collectAsState(initial = Loadable.Loading)
    val storedLocation by services.location.location.collectAsState(initial = Loadable.Loading)
    val brand by services.brand.brand.collectAsState(initial = Loadable.Loading)
    val schedule = remember { services.connection.retrySchedule() }
    val draftErrors = remember(draft) { validateConnectionDraft(draft) }
    val testing = probe as? ProbeState.Testing

    LaunchedEffect(testing) {
        if (testing == null) {
            elapsedSeconds = 0
            return@LaunchedEffect
        }
        while (true) {
            elapsedSeconds = ((System.currentTimeMillis() - testing.startedAt) / 1000).toInt()
            delay(250)
        }
    }

    fun notify(message: String) {
        scope.launch { snackbarHost.showSnackbar(message) }
    }

    fun previousStep(): OnboardingStep? = when (step) {
        OnboardingStep.WELCOME -> null
        OnboardingStep.LOCAL_FIRST -> OnboardingStep.WELCOME
        OnboardingStep.CONNECT -> OnboardingStep.LOCAL_FIRST
        OnboardingStep.RESULT -> OnboardingStep.CONNECT
        OnboardingStep.LOCATION -> if (visitedResult) OnboardingStep.RESULT else OnboardingStep.CONNECT
        OnboardingStep.COMPLETE -> OnboardingStep.LOCATION
    }

    fun complete() {
        val resolved = OnboardingOutcome(
            source = if (services.telemetry.isDemo) OnboardingSource.DEMO_SYSTEM else OnboardingSource.INVERTER,
            config = testedConfig,
            location = (locationState as? LocationOutcome.Resolved)?.location ?: storedLocation.valueOrNull(),
            pollIntervalMs = draft.pollIntervalMs,
            completedAt = System.currentTimeMillis()
        )
        outcome = resolved
        step = OnboardingStep.COMPLETE
    }

    fun startDemoFromWelcome() {
        scope.launch {
            services.telemetry.setDemoMode(true)
            complete()
        }
    }

    fun submitProbe() {
        val candidate = draft.copy(host = draft.host.trim())
        val validation = validateConnectionDraft(candidate)
        if (!validation.isValid) {
            notify("Check the marked fields before testing")
            return
        }
        step = OnboardingStep.CONNECT
        probe = ProbeState.Testing(System.currentTimeMillis())
        scope.launch {
            val config = candidate.toConfig(id = 1)
            val answer = withTimeoutOrNull(PROBE_TIMEOUT_MS) { services.connection.test(config) }
                ?: Loadable.Failed(
                    ServiceFailure.of(
                        FailureKind.TIMEOUT,
                        detail = HeliosFormat.hostPort(config.host, config.port) + " did not answer within 6 s",
                        attempts = 1
                    )
                )
            probe = ProbeState.Done(answer)
            visitedResult = true
            step = OnboardingStep.RESULT
            if (answer.valueOrNull() != null) {
                testedConfig = config
                val stored = services.connection.connect(config)
                if (stored.valueOrNull() == null) {
                    // The probe answered but the configuration could not be kept: say so on
                    // the result surface instead of pretending the link is saved.
                    probe = ProbeState.Done(stored)
                } else {
                    services.telemetry.setDemoMode(false)
                }
            }
        }
    }

    fun useMyLocation() {
        locationState = LocationOutcome.Resolving
        scope.launch {
            val mapped = locationOutcome(services.location.useMyLocation())
            locationState = mapped
            when (mapped) {
                is LocationOutcome.Resolved -> {
                    services.forecast.load(mapped.location)
                    notify("Location set from this device")
                }
                is LocationOutcome.Denied -> notify("Using the default location")
                is LocationOutcome.Failed -> notify("The lookup failed. Retry, or enter coordinates")
                LocationOutcome.Resolving -> Unit
            }
        }
    }

    val backTarget = previousStep()
    BackHandler(enabled = backTarget != null) {
        backTarget?.let { step = it }
    }

    val animations = remember { android.animation.ValueAnimator.areAnimatorsEnabled() }

    Surface(color = LocalHeliosSemanticColors.current.backgroundPrimary) {
        Box(modifier = modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val enter = if (animations) {
                        fadeIn(animationSpec = HeliosMotion.gentle) + scaleIn(
                            animationSpec = HeliosMotion.gentle,
                            initialScale = 0.98f
                        )
                    } else {
                        fadeIn(animationSpec = tween(HeliosMotion.ReduceMotion.crossfadeMs))
                    }
                    val leave = if (animations) {
                        fadeOut(animationSpec = tween(HeliosMotion.DurationMs.fast, easing = HeliosMotion.Curves.accelerate))
                    } else {
                        fadeOut(animationSpec = tween(HeliosMotion.ReduceMotion.crossfadeMs))
                    }
                    enter togetherWith leave
                },
                label = "onboardingStep",
                modifier = Modifier.fillMaxSize()
            ) { current ->
                when (current) {
                    OnboardingStep.WELCOME -> WelcomePage(
                        reading = reading,
                        demoSource = services.telemetry.isDemo,
                        onSetUp = { step = OnboardingStep.LOCAL_FIRST },
                        onDemo = { startDemoFromWelcome() },
                        modifier = Modifier.statusBarsPadding()
                    )

                    OnboardingStep.LOCAL_FIRST -> LocalFirstPage(
                        onContinue = { step = OnboardingStep.CONNECT },
                        modifier = Modifier.statusBarsPadding()
                    )

                    OnboardingStep.CONNECT -> ConnectPage(
                        draft = draft,
                        errors = draftErrors,
                        probe = probe,
                        scheduleMs = schedule,
                        onDraftChange = { draft = it },
                        onSubmit = { submitProbe() },
                        onDemo = { startDemoFromWelcome() },
                        modifier = Modifier.statusBarsPadding()
                    )

                    OnboardingStep.RESULT -> ResultPage(
                        result = (probe as? ProbeState.Done)?.result ?: Loadable.Loading,
                        config = testedConfig ?: draft.toConfig(),
                        reading = reading,
                        demoSource = services.telemetry.isDemo,
                        onContinue = { step = OnboardingStep.LOCATION },
                        onRetry = { submitProbe() },
                        onEditDetails = { step = OnboardingStep.CONNECT },
                        onDemo = { startDemoFromWelcome() },
                        onCopyDiagnostic = { text ->
                            clipboard.setText(AnnotatedString(text))
                            notify("Diagnostic copied")
                        },
                        modifier = Modifier.statusBarsPadding()
                    )

                    OnboardingStep.LOCATION -> LocationPage(
                        outcome = locationState,
                        stored = storedLocation,
                        onUseMyLocation = { useMyLocation() },
                        onEnterCoordinates = { coordinatesOpen = true },
                        onRetry = { useMyLocation() },
                        onNotNow = { complete() },
                        onContinue = { complete() },
                        modifier = Modifier.statusBarsPadding()
                    )

                    OnboardingStep.COMPLETE -> {
                        val finished = outcome
                        if (finished != null) {
                            CompletePage(
                                outcome = finished,
                                reading = reading,
                                brandName = brand.valueOrNull()?.name ?: "helios",
                                onOpenDashboard = onFinished?.let { callback -> { callback(finished) } },
                                modifier = Modifier.statusBarsPadding()
                            )
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHost,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(HeliosSpacing.space4)
            )
        }
    }

    if (coordinatesOpen) {
        CoordinateEntrySheet(
            onDismiss = { coordinatesOpen = false },
            onApply = { lat, lng, label ->
                coordinatesOpen = false
                scope.launch {
                    val result = services.location.setManual(lat, lng, label)
                    val location = result.valueOrNull()
                    if (location != null) {
                        locationState = LocationOutcome.Resolved(location)
                        services.forecast.load(location)
                        notify("Coordinates set")
                    } else {
                        locationState = locationOutcome(result)
                        notify("Those coordinates could not be stored")
                    }
                }
            }
        )
    }
}

/** The 6 s bound of ACT-012, applied here so a hanging probe still reports class F2. */
private const val PROBE_TIMEOUT_MS = 6_000L

/** The onboarding draft survives process recreation, so a typed host is not lost. */
private val connectionDraftSaver: Saver<ConnectionDraft, Any> = listSaver(
    save = { draft ->
        listOf(draft.protocol, draft.host, draft.port, draft.unitId, draft.pollIntervalMs.toString())
    },
    restore = { values ->
        ConnectionDraft(
            protocol = values[0] as String,
            host = values[1] as String,
            port = values[2] as String,
            unitId = values[3] as String,
            pollIntervalMs = (values[4] as String).toIntOrNull() ?: DEFAULT_POLL_INTERVAL_MS
        )
    }
)
