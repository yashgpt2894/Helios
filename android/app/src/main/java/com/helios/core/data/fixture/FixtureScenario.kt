package com.helios.core.data.fixture

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The nine situations every screen has to survive. They are the acceptance list for the
 * state model in `design/ux-flows.md` section 0, plus long content, which is the shape
 * that breaks layouts rather than the data:
 *
 *  LOADING            no value yet, so the screen shows a skeleton in the content's shape
 *  LIVE               a reading 5 s old or less; tickers run
 *  EMPTY              the query is valid and there is genuinely nothing to show
 *  PARTIAL            some fields did not report (F7); failures are never shown as zero
 *  STALE              more than 15 s old; values dim and insights are suppressed
 *  OFFLINE            the link is down (F1-F6); last-known values are labelled
 *  ERROR              one surface failed while the rest works (F8 inline forecast error)
 *  DENIED_PERMISSION  location denied (F9); the default location keeps working
 *  LONG_CONTENT       long labels, 24 insights, 30 forecast days, long host names
 */
enum class FixtureScenario(val label: String) {
    LOADING("Loading"),
    LIVE("Live"),
    EMPTY("Empty"),
    PARTIAL("Partial"),
    STALE("Stale"),
    OFFLINE("Offline"),
    ERROR("Error"),
    DENIED_PERMISSION("Denied permission"),
    LONG_CONTENT("Long content")
}

/**
 * One shared scenario switch for every fixture adapter, so a screen cannot end up half
 * live and half offline by accident.
 */
object FixtureState {

    private val _scenario = MutableStateFlow(FixtureScenario.LIVE)
    val scenario: StateFlow<FixtureScenario> = _scenario.asStateFlow()

    fun select(next: FixtureScenario) {
        _scenario.value = next
    }

    fun current(): FixtureScenario = _scenario.value
}
