package com.helios.core.data.fixture

import com.helios.core.data.repository.SolarCurve
import com.helios.core.data.service.ConnectionSnapshot
import com.helios.core.data.service.FailureKind
import com.helios.core.data.service.Freshness
import com.helios.core.data.service.HeliosThemeMode
import com.helios.core.data.service.LinkState
import com.helios.core.data.service.LivePoint
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.ServiceFailure
import com.helios.core.data.service.SourceMeta
import com.helios.core.data.service.WeekPoint
import com.helios.core.domain.model.Brand
import com.helios.core.domain.model.ConnectionConfig
import com.helios.core.domain.model.ForecastDay
import com.helios.core.domain.model.HistoryPoint
import com.helios.core.domain.model.Insight
import com.helios.core.domain.model.InsightCategory
import com.helios.core.domain.model.InsightSeverity
import com.helios.core.domain.model.InverterStatus
import com.helios.core.domain.model.Location
import com.helios.core.domain.model.PanelString
import com.helios.core.domain.model.ProductionForecast
import com.helios.core.domain.model.SnapshotPayload
import com.helios.core.domain.model.SolarTelemetry
import com.helios.core.domain.model.WeatherCondition
import kotlin.math.abs
import kotlin.math.sin

/**
 * Deterministic fixture content. No randomness, no clock drift beyond "now", no network.
 *
 * Values are realistic for the reference system the design uses: a 9.6 kW array, a
 * 13.5 kWh battery, three strings, a SunSpec Modbus TCP inverter. Every scenario in
 * [FixtureScenario] maps onto these values in [FixtureData], so the gallery and any
 * screen can render all nine states offline.
 *
 * An unread register is [Double.NaN], never 0.0, because 0 W and "no data" mean
 * different things to someone diagnosing a fault.
 */
object FixtureData {

    const val RATED_W = 9600.0
    const val BATTERY_KWH = 13.5

    val heliosBrand = Brand(
        id = "helios",
        name = "helios\u00B0",
        legalName = "helios\u00B0 energy",
        accent = "#F0C674",
        accentLight = "#B8862E",
        mark = "helios",
        tagline = "Precision energy intelligence for your solar array."
    )

    /** Registry order matches `src/services/brand.ts`; a deep link carries these ids. */
    val brands: List<Brand> = listOf(
        heliosBrand,
        Brand(
            id = "voltcraft",
            name = "Voltcraft",
            legalName = "Voltcraft Solar, Inc.",
            accent = "#A78BFA",
            accentLight = "#6D28D9",
            mark = "text",
            textMark = "V",
            supportEmail = "support@voltcraft.example",
            tagline = "Your solar, refined."
        ),
        Brand(
            id = "sunworks",
            name = "SunWorks",
            legalName = "SunWorks Energy Co.",
            accent = "#38BDF8",
            accentLight = "#0369A1",
            mark = "text",
            textMark = "S",
            supportEmail = "help@sunworks.example",
            tagline = "Powering your home, smarter."
        ),
        Brand(
            id = "meridian",
            name = "Meridian",
            legalName = "Meridian Renewables",
            accent = "#FB923C",
            accentLight = "#C2410C",
            mark = "text",
            textMark = "M",
            supportEmail = "care@meridian.example",
            tagline = "Solar, perfectly tuned."
        )
    )

    val defaultLocation = Location(lat = 37.7749, lng = -122.4194, label = "San Francisco, CA", source = "default")
    val manualLocation = Location(lat = 38.7223, lng = -9.1393, label = "Lisbon, Portugal", source = "manual")
    val longLocation = Location(
        lat = 37.6535,
        lng = -122.4088,
        label = "Rooftop array at 1420 Willowbrook Terrace, Apartment 12B, South San Francisco, California 94080",
        source = "manual"
    )

    val connectionConfig = ConnectionConfig(
        id = 1,
        protocol = "sunspec-modbus-tcp",
        host = "192.168.1.42",
        port = 502,
        unitId = 1,
        pollIntervalMs = 2000,
        status = "connected"
    )

    val longConnectionConfig = connectionConfig.copy(
        host = "helios-inverter-garage-rack-04.local.helios.lan",
        port = 5020,
        unitId = 247
    )

    val retryScheduleMs = listOf(1_000L, 2_000L, 4_000L, 30_000L, 60_000L)

    // ---------------------------------------------------------------- telemetry

    private fun panels(dcPowerW: Double, hour: Double, dropLast: Boolean): List<PanelString> {
        val sunSkew = SolarCurve.clamp((hour - 12) / 6, -1.0, 1.0)
        val shapes = listOf(
            Triple("A", "Roof \u00B7 South-East", Triple(3200.0, 8, -0.15)),
            Triple("B", "Roof \u00B7 South-West", Triple(3200.0, 8, 0.15)),
            Triple("C", "Garage \u00B7 South", Triple(3200.0, 8, 0.0))
        )
        return shapes.mapIndexedNotNull { index, (id, label, params) ->
            if (dropLast && index == shapes.lastIndex) return@mapIndexedNotNull null
            val (ratedW, count, orientation) = params
            val fraction = (ratedW / RATED_W) * (1 + orientation * sunSkew)
            val power = SolarCurve.clamp(dcPowerW * fraction, 0.0, ratedW)
            val voltage = if (power > 30) 380.0 + id[0].code % 20 else 0.0
            PanelString(
                id = id,
                label = label,
                powerW = power,
                voltageV = voltage,
                currentA = if (voltage > 0) power / voltage else 0.0,
                ratedW = ratedW,
                panels = count
            )
        }
    }

    /** Daytime, producing, battery charging. The reading a screen should be built around. */
    fun daytimeTelemetry(nowMs: Long): SolarTelemetry {
        val hour = SolarCurve.nowAsHourFloat(nowMs)
        val irr = maxOf(120.0, SolarCurve.irradianceAt(hour.coerceIn(6.2, 19.8), 0.18))
        val dc = (irr / 1000.0) * RATED_W * 0.98
        val ac = dc * 0.964
        val home = SolarCurve.consumptionFractionAt(hour) * 4200
        val surplus = ac - home
        return SolarTelemetry(
            timestamp = nowMs,
            manufacturer = "helios\u00B0",
            model = "HX-9.6 Hybrid Inverter",
            serialNumber = "HX-2025-0F31A2",
            firmware = "4.12.1",
            status = InverterStatus.PRODUCING,
            acPowerW = 4231.0,
            acVoltageV = 240.4,
            acCurrentA = 17.6,
            acFrequencyHz = 60.01,
            dcPowerW = dc,
            dcVoltageV = 382.0,
            dcCurrentA = dc / 382.0,
            cabinetTempC = 41.2,
            heatsinkTempC = 47.6,
            energyTodayKwh = 18.4,
            energyMonthKwh = 412.7,
            energyLifetimeKwh = 18420.5,
            batterySoc = 62.0,
            batteryPowerW = surplus.coerceAtLeast(0.0).coerceAtMost(5000.0),
            batteryHealthPct = 97.4,
            batteryCycles = 312,
            batteryCapacityKwh = BATTERY_KWH,
            batteryTempC = 26.4,
            homeLoadW = home,
            gridImportW = 0.0,
            gridExportW = (ac - home - 1120.0).coerceAtLeast(0.0),
            irradianceWm2 = irr,
            ambientTempC = 24.6,
            cloudCoverPct = 18.0,
            panels = panels(dc, hour, dropLast = false)
        )
    }

    /** Night: no production, the home runs from the battery and then the grid. */
    fun nightTelemetry(nowMs: Long): SolarTelemetry = daytimeTelemetry(nowMs).copy(
        timestamp = nowMs,
        status = InverterStatus.NIGHT,
        acPowerW = 0.0,
        acCurrentA = 0.0,
        dcPowerW = 0.0,
        dcVoltageV = 0.0,
        dcCurrentA = 0.0,
        batterySoc = 41.0,
        batteryPowerW = -620.0,
        homeLoadW = 620.0,
        gridImportW = 0.0,
        gridExportW = 0.0,
        irradianceWm2 = 0.0,
        ambientTempC = 16.2,
        cloudCoverPct = 22.0,
        energyTodayKwh = 31.6,
        panels = panels(0.0, 12.0, dropLast = false)
    )

    /**
     * F7: the inverter answered, but part of the register block did not arrive. The
     * missing fields are NaN and named in [SourceMeta.missing], so the UI can say which
     * value is missing instead of printing a fabricated zero.
     */
    fun partialTelemetry(nowMs: Long): SolarTelemetry = daytimeTelemetry(nowMs).copy(
        dcVoltageV = Double.NaN,
        dcCurrentA = Double.NaN,
        cabinetTempC = Double.NaN,
        batteryTempC = Double.NaN,
        panels = panels(daytimeTelemetry(nowMs).dcPowerW, SolarCurve.nowAsHourFloat(nowMs), dropLast = true)
    )

    val partialMissingFields = listOf("dcVoltageV", "dcCurrentA", "cabinetTempC", "batteryTempC", "panels.C")

    /** Long content: a long model name, a long serial and a long site label. */
    fun longTelemetry(nowMs: Long): SolarTelemetry = daytimeTelemetry(nowMs).copy(
        model = "HX-9.6 Hybrid Inverter, three-phase backup, dual MPPT tracker, model year 2026",
        serialNumber = "HX-2026-0F31A2-4B77-9C10-DE55-0192837465AA",
        firmware = "4.12.1-rc7+build.20260919.abcdef1234567890",
        panels = panels(daytimeTelemetry(nowMs).dcPowerW, SolarCurve.nowAsHourFloat(nowMs), dropLast = false).map {
            it.copy(label = it.label + " \u00B7 8 panels \u00B7 400 W each \u00B7 installed March 2024")
        }
    )

    fun telemetryMeta(
        scenario: FixtureScenario,
        nowMs: Long,
        fetchedAt: Long = nowMs,
        simulated: Boolean = false
    ): SourceMeta {
        val age = (nowMs - fetchedAt).coerceAtLeast(0)
        return SourceMeta(
            freshness = when (scenario) {
                FixtureScenario.STALE -> Freshness.STALE
                FixtureScenario.OFFLINE -> Freshness.OFFLINE
                FixtureScenario.LIVE, FixtureScenario.EMPTY, FixtureScenario.LONG_CONTENT,
                FixtureScenario.DENIED_PERMISSION, FixtureScenario.ERROR -> Freshness.forAge(age)
                FixtureScenario.PARTIAL -> Freshness.AGING
                FixtureScenario.LOADING -> Freshness.LIVE
            },
            fetchedAt = fetchedAt,
            ageMs = age,
            partial = scenario == FixtureScenario.PARTIAL,
            missing = if (scenario == FixtureScenario.PARTIAL) partialMissingFields else emptyList(),
            note = when (scenario) {
                FixtureScenario.PARTIAL -> "3 of 4 values reported"
                FixtureScenario.STALE -> "Last good reading"
                else -> null
            },
            simulated = simulated
        )
    }

    fun telemetry(scenario: FixtureScenario, nowMs: Long = System.currentTimeMillis()): Loadable<SolarTelemetry> =
        when (scenario) {
            FixtureScenario.LOADING -> Loadable.Loading
            FixtureScenario.OFFLINE -> Loadable.Failed(
                failure = ServiceFailure.of(
                    FailureKind.NO_NETWORK,
                    detail = "No route to 192.168.1.42:502",
                    lastGoodAt = nowMs - 96_000,
                    attempts = 5
                ),
                meta = telemetryMeta(FixtureScenario.STALE, nowMs, nowMs - 96_000)
            )
            FixtureScenario.PARTIAL -> Loadable.Ready(
                partialTelemetry(nowMs),
                telemetryMeta(FixtureScenario.PARTIAL, nowMs, nowMs - 7_000)
            )
            FixtureScenario.STALE -> Loadable.Ready(
                daytimeTelemetry(nowMs),
                telemetryMeta(FixtureScenario.STALE, nowMs, nowMs - 42_000)
            )
            FixtureScenario.LONG_CONTENT -> Loadable.Ready(
                longTelemetry(nowMs),
                telemetryMeta(FixtureScenario.LONG_CONTENT, nowMs, nowMs - 1_000)
            )
            FixtureScenario.ERROR -> Loadable.Ready(
                daytimeTelemetry(nowMs),
                telemetryMeta(FixtureScenario.ERROR, nowMs, nowMs - 2_000)
            )
            else -> Loadable.Ready(
                daytimeTelemetry(nowMs),
                telemetryMeta(scenario, nowMs, nowMs - 1_400)
            )
        }

    fun nightTelemetry(scenario: FixtureScenario, nowMs: Long = System.currentTimeMillis()): Loadable<SolarTelemetry> =
        if (scenario == FixtureScenario.LOADING) Loadable.Loading
        else Loadable.Ready(nightTelemetry(nowMs), telemetryMeta(scenario, nowMs, nowMs - 1_400))

    // ---------------------------------------------------------------- series

    /** 49 half-hour points for the today curve (00:00 to 24:00). */
    fun todaySeries(nowMs: Long = System.currentTimeMillis()): List<HistoryPoint> {
        val points = mutableListOf<HistoryPoint>()
        val cloudCover = 0.2
        var h = 0.0
        while (h <= 24.0) {
            val irr = SolarCurve.irradianceAt(h, cloudCover)
            val ideal = (irr / 1000.0) * RATED_W * 0.964
            val production = ideal * (1 + sin(h * 3) * 0.05)
            val consumption = SolarCurve.consumptionFractionAt(h) * 4200
            val surplus = production - consumption
            val battery = SolarCurve.clamp(surplus, -5000.0, 5000.0) * 0.7
            points.add(
                HistoryPoint(
                    t = h,
                    productionW = production,
                    consumptionW = consumption,
                    batteryW = battery,
                    gridW = surplus - battery,
                    irradianceWm2 = irr
                )
            )
            h += 0.5
        }
        return points
    }

    /** 30 one-second samples for the live trail, ending at [nowMs]. */
    fun liveSeries(nowMs: Long = System.currentTimeMillis()): List<LivePoint> =
        (0 until 30).map { index ->
            val t = nowMs - (29 - index) * 2_000L
            val wobble = sin(index / 3.0) * 90.0
            LivePoint(
                t = t,
                acPowerW = 4231.0 + wobble,
                homeLoadW = 1870.0 + sin(index / 4.0) * 60.0,
                batteryW = 1120.0 - wobble,
                gridW = 1240.0,
                socPct = 62.0 + index * 0.02
            )
        }

    fun weekSeries(nowMs: Long = System.currentTimeMillis()): List<WeekPoint> {
        val produced = listOf(38.2, 41.6, 29.4, 35.8, 44.1, 39.7, 33.2)
        val consumed = listOf(28.4, 31.2, 26.8, 29.9, 33.4, 31.1, 27.6)
        val today = java.time.LocalDate.now()
        return produced.indices.map { index ->
            val date = today.minusDays((produced.size - 1 - index).toLong())
            WeekPoint(
                dayLabel = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.US),
                dateIso = date.toString(),
                producedKwh = produced[index],
                consumedKwh = consumed[index]
            )
        }
    }

    fun seriesLoadable(scenario: FixtureScenario, nowMs: Long = System.currentTimeMillis()): Loadable<List<HistoryPoint>> =
        when (scenario) {
            FixtureScenario.LOADING -> Loadable.Loading
            FixtureScenario.EMPTY -> Loadable.Empty(
                meta = SourceMeta(Freshness.LIVE, nowMs, 0),
                message = "No production recorded yet today",
                actionLabel = "Check the connection"
            )
            FixtureScenario.OFFLINE -> Loadable.Failed(
                ServiceFailure.of(FailureKind.TIMEOUT, detail = "192.168.1.42:502 after 2 s", attempts = 3),
                SourceMeta(Freshness.OFFLINE, nowMs - 96_000, 96_000)
            )
            FixtureScenario.PARTIAL -> Loadable.Ready(
                todaySeries(nowMs).filter { it.t <= 14.0 },
                SourceMeta(Freshness.AGING, nowMs - 7_000, 7_000, partial = true, missing = listOf("points after 14:00"))
            )
            FixtureScenario.STALE -> Loadable.Ready(
                todaySeries(nowMs),
                SourceMeta(Freshness.STALE, nowMs - 42_000, 42_000)
            )
            FixtureScenario.LONG_CONTENT -> Loadable.Ready(
                todaySeries(nowMs),
                SourceMeta(Freshness.LIVE, nowMs - 1_000, 1_000)
            )
            else -> Loadable.Ready(todaySeries(nowMs), SourceMeta(Freshness.LIVE, nowMs - 1_400, 1_400))
        }

    fun liveLoadable(scenario: FixtureScenario, nowMs: Long = System.currentTimeMillis()): Loadable<List<LivePoint>> =
        when (scenario) {
            FixtureScenario.LOADING -> Loadable.Loading
            FixtureScenario.EMPTY -> Loadable.Empty(
                meta = SourceMeta(Freshness.LIVE, nowMs, 0),
                message = "No live samples yet",
                actionLabel = "Retry now"
            )
            FixtureScenario.OFFLINE -> Loadable.Failed(
                ServiceFailure.of(FailureKind.NO_NETWORK, detail = "No route to 192.168.1.42:502", attempts = 5),
                SourceMeta(Freshness.OFFLINE, nowMs - 96_000, 96_000)
            )
            FixtureScenario.STALE -> Loadable.Ready(
                liveSeries(nowMs),
                SourceMeta(Freshness.STALE, nowMs - 42_000, 42_000)
            )
            FixtureScenario.PARTIAL -> Loadable.Ready(
                liveSeries(nowMs).mapIndexed { index, point -> if (index % 4 == 0) point.copy(gridW = Double.NaN) else point },
                SourceMeta(Freshness.AGING, nowMs - 7_000, 7_000, partial = true, missing = listOf("gridW on 8 samples"))
            )
            else -> Loadable.Ready(liveSeries(nowMs), SourceMeta(Freshness.LIVE, nowMs - 1_000, 1_000))
        }

    fun weekLoadable(scenario: FixtureScenario, nowMs: Long = System.currentTimeMillis()): Loadable<List<WeekPoint>> =
        when (scenario) {
            FixtureScenario.LOADING -> Loadable.Loading
            FixtureScenario.EMPTY -> Loadable.Empty(
                meta = SourceMeta(Freshness.LIVE, nowMs, 0),
                message = "No completed days yet",
                actionLabel = null
            )
            FixtureScenario.OFFLINE -> Loadable.Failed(
                ServiceFailure.of(FailureKind.MALFORMED_RESPONSE, detail = "Week history shorter than 7 days"),
                SourceMeta(Freshness.OFFLINE, nowMs - 96_000, 96_000)
            )
            FixtureScenario.PARTIAL -> Loadable.Ready(
                weekSeries(nowMs).take(4),
                SourceMeta(Freshness.AGING, nowMs - 300_000, 300_000, partial = true, missing = listOf("Fri", "Sat", "Sun"))
            )
            FixtureScenario.STALE -> Loadable.Ready(
                weekSeries(nowMs),
                SourceMeta(Freshness.STALE, nowMs - 42_000, 42_000)
            )
            else -> Loadable.Ready(weekSeries(nowMs), SourceMeta(Freshness.AGING, nowMs - 30_000, 30_000))
        }

    // ---------------------------------------------------------------- forecast

    private val conditionTable = listOf(
        Triple(0, WeatherCondition.clear, "Clear"),
        Triple(1, WeatherCondition.mostlyClear, "Mostly clear"),
        Triple(2, WeatherCondition.partlyCloudy, "Partly cloudy"),
        Triple(3, WeatherCondition.overcast, "Overcast"),
        Triple(45, WeatherCondition.fog, "Fog"),
        Triple(51, WeatherCondition.drizzle, "Drizzle"),
        Triple(61, WeatherCondition.rain, "Rain"),
        Triple(65, WeatherCondition.heavyRain, "Heavy rain"),
        Triple(71, WeatherCondition.snow, "Snow"),
        Triple(95, WeatherCondition.thunderstorm, "Thunderstorm")
    )

    fun forecastDays(count: Int = 7, nowMs: Long = System.currentTimeMillis()): List<ForecastDay> {
        val today = java.time.LocalDate.now()
        val expected = listOf(37.8, 34.2, 21.6, 12.4, 28.9, 36.1, 39.4)
        return (0 until count).map { index ->
            val (code, condition, label) = conditionTable[index % conditionTable.size]
            val day = today.plusDays(index.toLong())
            val base = expected[index % expected.size]
            ForecastDay(
                date = day.toString(),
                weatherCode = code,
                condition = condition,
                conditionLabel = label,
                tempHighC = 26.0 + (index % 5),
                tempLowC = 15.0 + (index % 4),
                precipitationMm = if (code >= 51) 4.2 else 0.0,
                shortwaveRadiationMJ = 17.28 - index * 0.4,
                cloudCoverPct = (code * 4).coerceAtMost(90).toDouble(),
                expectedKwh = base,
                expectedKwhVsTypical = (base - 32.0) / 32.0 * 100
            )
        }
    }

    fun forecast(scenario: FixtureScenario, nowMs: Long = System.currentTimeMillis()): Loadable<ProductionForecast> {
        val location = if (scenario == FixtureScenario.LONG_CONTENT) longLocation else defaultLocation
        fun build(days: List<ForecastDay>) = ProductionForecast(
            fetchedAt = nowMs,
            location = location,
            days = days,
            totalKwh = days.sumOf { it.expectedKwh },
            vsLastWeekPct = 13.44
        )
        return when (scenario) {
            FixtureScenario.LOADING -> Loadable.Loading
            FixtureScenario.EMPTY -> Loadable.Empty(
                meta = SourceMeta(Freshness.LIVE, nowMs, 0),
                message = "No forecast for this location",
                actionLabel = "Choose a place by name"
            )
            FixtureScenario.OFFLINE -> Loadable.Ready(
                build(forecastDays(7, nowMs)),
                SourceMeta(Freshness.STALE, nowMs - 900_000, 900_000, note = "Fetched before the link dropped")
            )
            FixtureScenario.ERROR -> Loadable.Failed(
                ServiceFailure.of(FailureKind.FORECAST_UNAVAILABLE, detail = "Open-Meteo request failed"),
                SourceMeta(Freshness.STALE, nowMs - 900_000, 900_000)
            )
            FixtureScenario.PARTIAL -> Loadable.Ready(
                build(forecastDays(5, nowMs)),
                SourceMeta(Freshness.AGING, nowMs - 600_000, 600_000, partial = true, missing = listOf("day 6", "day 7"))
            )
            FixtureScenario.STALE -> Loadable.Ready(
                build(forecastDays(7, nowMs)),
                SourceMeta(Freshness.STALE, nowMs - 900_000, 900_000, note = "Forecast is 15 min old")
            )
            FixtureScenario.LONG_CONTENT -> Loadable.Ready(
                build(forecastDays(30, nowMs)),
                SourceMeta(Freshness.LIVE, nowMs - 60_000, 60_000)
            )
            else -> Loadable.Ready(build(forecastDays(7, nowMs)), SourceMeta(Freshness.LIVE, nowMs - 60_000, 60_000))
        }
    }

    // ---------------------------------------------------------------- insights

    fun insights(nowMs: Long = System.currentTimeMillis()): List<Insight> = listOf(
        Insight(
            id = "fx-selfuse",
            category = InsightCategory.consumption,
            severity = InsightSeverity.positive,
            title = "Self-consumption",
            body = "82 percent of your solar is powering your home directly.",
            metric = "82%",
            delta = "+4% vs last week"
        ),
        Insight(
            id = "fx-efficiency",
            category = InsightCategory.production,
            severity = InsightSeverity.attention,
            title = "Production efficiency",
            body = "The array is running at 78 percent of rated efficiency. String C is 12 percent below its neighbours.",
            metric = "78%",
            delta = "-6%",
            actionLabel = "Inspect string C"
        ),
        Insight(
            id = "fx-battery",
            category = InsightCategory.battery,
            severity = InsightSeverity.neutral,
            title = "Battery health",
            body = "97.4 percent capacity after 312 cycles.",
            metric = "97.4%"
        ),
        Insight(
            id = "fx-savings",
            category = InsightCategory.savings,
            severity = InsightSeverity.positive,
            title = "Today's savings",
            body = "You saved about 6.42 dollars today at the current import rate of 0.32 per kWh.",
            metric = "$6.42",
            delta = "+1.10"
        ),
        Insight(
            id = "fx-streak",
            category = InsightCategory.forecast,
            severity = InsightSeverity.positive,
            title = "Self-sufficient streak",
            body = "The next three days look sunny. You could stay grid-independent.",
            metric = "94 kWh"
        ),
        Insight(
            id = "fx-maintenance",
            category = InsightCategory.maintenance,
            severity = InsightSeverity.critical,
            title = "Inverter fault",
            body = "The inverter reported an arc-fault warning at 13:42 and reset itself. Check the DC connectors.",
            metric = "1 fault",
            actionLabel = "Open diagnostics"
        )
    )

    /** Long content: 24 rows with long bodies, the shape that breaks a card list. */
    fun longInsights(nowMs: Long = System.currentTimeMillis()): List<Insight> =
        (1..24).map { index ->
            val base = insights(nowMs)[index % insights(nowMs).size]
            base.copy(
                id = "fx-long-$index",
                title = "${base.title} \u00B7 day $index",
                body = base.body + " This row is deliberately long so a two-line card, a three-line card and a truncated card can all be inspected at the same time, including the trailing action."
            )
        }

    fun insightsLoadable(scenario: FixtureScenario, nowMs: Long = System.currentTimeMillis()): Loadable<List<Insight>> =
        when (scenario) {
            FixtureScenario.LOADING -> Loadable.Loading
            FixtureScenario.EMPTY -> Loadable.Empty(
                meta = SourceMeta(Freshness.LIVE, nowMs, 0),
                message = "No advisories while the system is idle",
                actionLabel = null
            )
            FixtureScenario.STALE, FixtureScenario.OFFLINE -> Loadable.Empty(
                meta = SourceMeta(Freshness.STALE, nowMs - 96_000, 96_000, note = "Advisories paused"),
                message = "Advisories are paused while the reading is stale",
                actionLabel = "Retry now"
            )
            FixtureScenario.ERROR -> Loadable.Failed(
                ServiceFailure.of(FailureKind.UNKNOWN, detail = "Insight generation failed for the current reading"),
                SourceMeta(Freshness.AGING, nowMs - 7_000, 7_000)
            )
            FixtureScenario.PARTIAL -> Loadable.Ready(
                insights(nowMs).take(2),
                SourceMeta(Freshness.AGING, nowMs - 7_000, 7_000, partial = true, missing = listOf("battery", "savings"))
            )
            FixtureScenario.LONG_CONTENT -> Loadable.Ready(
                longInsights(nowMs),
                SourceMeta(Freshness.LIVE, nowMs - 2_000, 2_000, note = "24 advisories")
            )
            else -> Loadable.Ready(insights(nowMs), SourceMeta(Freshness.LIVE, nowMs - 2_000, 2_000))
        }

    // ---------------------------------------------------------------- connection

    fun connectionSnapshot(
        scenario: FixtureScenario,
        nowMs: Long = System.currentTimeMillis()
    ): Loadable<ConnectionSnapshot> = when (scenario) {
        FixtureScenario.LOADING -> Loadable.Loading
        FixtureScenario.OFFLINE -> Loadable.Failed(
            ServiceFailure.of(FailureKind.MULTIPLE_MASTERS, detail = "192.168.1.42:502 unit 1", attempts = 4),
            SourceMeta(Freshness.OFFLINE, nowMs - 96_000, 96_000)
        )
        FixtureScenario.ERROR -> Loadable.Failed(
            ServiceFailure.of(FailureKind.WRONG_UNIT_ID, detail = "Unit id 1 answered with Modbus exception 2", attempts = 1),
            SourceMeta(Freshness.OFFLINE, nowMs - 30_000, 30_000)
        )
        FixtureScenario.STALE -> Loadable.Ready(
            ConnectionSnapshot(connectionConfig, LinkState.CONNECTED, nowMs - 42_000, nowMs - 42_000, 0, "HX-9.6", "4.12.1"),
            SourceMeta(Freshness.STALE, nowMs - 42_000, 42_000)
        )
        FixtureScenario.LONG_CONTENT -> Loadable.Ready(
            ConnectionSnapshot(longConnectionConfig, LinkState.CONNECTED, nowMs - 1_000, nowMs - 1_000, 0, "HX-9.6 Hybrid Inverter with three-phase backup", "4.12.1-rc7+build.20260919"),
            SourceMeta(Freshness.LIVE, nowMs - 1_000, 1_000)
        )
        FixtureScenario.PARTIAL -> Loadable.Ready(
            ConnectionSnapshot(connectionConfig, LinkState.CONNECTED, nowMs - 7_000, nowMs - 7_000, 1, "HX-9.6", "4.12.1"),
            SourceMeta(Freshness.AGING, nowMs - 7_000, 7_000, partial = true, missing = listOf("firmware build date"))
        )
        else -> Loadable.Ready(
            ConnectionSnapshot(connectionConfig, LinkState.CONNECTED, nowMs - 1_400, nowMs - 1_400, 0, "HX-9.6", "4.12.1"),
            SourceMeta(Freshness.LIVE, nowMs - 1_400, 1_400)
        )
    }

    /** One snapshot per failure class, for the Connection sheet and the gallery. */
    fun connectionFailure(kind: FailureKind, nowMs: Long = System.currentTimeMillis()): Loadable<ConnectionSnapshot> =
        Loadable.Failed(
            ServiceFailure.of(kind, detail = "${connectionConfig.host}:${connectionConfig.port} unit ${connectionConfig.unitId}", attempts = 3),
            SourceMeta(Freshness.OFFLINE, nowMs - 96_000, 96_000)
        )

    // ---------------------------------------------------------------- location, theme, share

    fun location(scenario: FixtureScenario, nowMs: Long = System.currentTimeMillis()): Loadable<Location> =
        when (scenario) {
            FixtureScenario.LOADING -> Loadable.Loading
            FixtureScenario.DENIED_PERMISSION -> Loadable.Failed(
                ServiceFailure.of(FailureKind.LOCATION_DENIED, detail = "ACCESS_FINE_LOCATION denied"),
                SourceMeta(Freshness.LIVE, nowMs, 0)
            )
            FixtureScenario.EMPTY -> Loadable.Empty(
                meta = SourceMeta(Freshness.LIVE, nowMs, 0),
                message = "No location set",
                actionLabel = "Choose a place by name"
            )
            FixtureScenario.LONG_CONTENT -> Loadable.Ready(longLocation, SourceMeta(Freshness.LIVE, nowMs, 0))
            FixtureScenario.OFFLINE -> Loadable.Ready(defaultLocation, SourceMeta(Freshness.STALE, nowMs - 600_000, 600_000))
            else -> Loadable.Ready(defaultLocation, SourceMeta(Freshness.LIVE, nowMs, 0))
        }

    fun themeMode(scenario: FixtureScenario): Loadable<HeliosThemeMode> =
        if (scenario == FixtureScenario.LOADING) Loadable.Loading
        else Loadable.Ready(HeliosThemeMode.DARK, SourceMeta(Freshness.LIVE, System.currentTimeMillis(), 0))

    fun brand(scenario: FixtureScenario): Loadable<Brand> =
        if (scenario == FixtureScenario.LOADING) Loadable.Loading
        else Loadable.Ready(heliosBrand, SourceMeta(Freshness.LIVE, System.currentTimeMillis(), 0))

    fun snapshotPayload(nowMs: Long = System.currentTimeMillis()): SnapshotPayload = SnapshotPayload(
        v = 1,
        ts = nowMs,
        loc = defaultLocation.label,
        ac = 4.23,
        todayKwh = 18.4,
        lifeKwh = 18421,
        soc = 62,
        selfUse = 82,
        fc = forecastDays(7, nowMs).map { Math.round(it.expectedKwh).toInt() },
        br = null
    )

    /** The share targets the sheet offers. Availability is a device fact, so fixtures mark all three. */
    fun shareTargets(): List<com.helios.core.data.service.ShareTarget> = listOf(
        com.helios.core.data.service.ShareTarget("clipboard", "Copy link", true),
        com.helios.core.data.service.ShareTarget("messages", "Messages", true),
        com.helios.core.data.service.ShareTarget("email", "Email", true)
    )

    /** Deterministic stand-in for the encoded snapshot, used when a fixture needs a link. */
    fun encodedSnapshot(payload: SnapshotPayload): String = "v1-" + abs(payload.ts % 1_000_000).toString() + "-fixture"
}
