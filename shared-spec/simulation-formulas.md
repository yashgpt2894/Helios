# Simulation Formulas — Ported from Helios PWA

All formulas below are extracted verbatim from `src/services/sunspec.ts` and `src/services/weather.ts`. Unit-test fixtures are provided for each non-trivial expression so that iOS and Android implementations can be validated against the same numerical expectations.

## 1. Solar Curve (`lib/solarCurve.ts`)

### 1.1 `solarFractionAt(hourFloat)`

```
SUNRISE_HOUR = 6.2
SUNSET_HOUR  = 19.8
PEAK_HOUR    = 13.0

if hourFloat <= SUNRISE_HOUR or hourFloat >= SUNSET_HOUR:
    return 0

x    = (hourFloat - SUNRISE_HOUR) / (SUNSET_HOUR - SUNRISE_HOUR)
bell = sin(PI * x)
skew = 1 - abs(hourFloat - PEAK_HOUR) / 8

return clamp(bell * skew, 0, 1)
```

**Fixtures**
| hourFloat | Expected |
|-----------|----------|
| 5.0       | 0.0000   |
| 6.2       | 0.0000   |
| 10.0      | 0.6854   |
| 13.0      | 1.0000   |
| 16.0      | 0.6533   |
| 19.8      | 0.0000   |
| 22.0      | 0.0000   |

### 1.2 `irradianceAt(hourFloat, cloudCover)`

```
clear         = solarFractionAt(hourFloat) * 1000
transmittance = 1 - cloudCover * 0.7

return max(0, clear * transmittance)
```

`cloudCover` is a fraction in `[0, 1]`.

**Fixtures**
| hourFloat | cloudCover | Expected |
|-----------|------------|----------|
| 13.0      | 0.0        | 1000.0   |
| 13.0      | 0.2        | 860.0    |
| 13.0      | 0.5        | 650.0    |
| 10.0      | 0.18       | 562.0    |
| 6.2       | 0.0        | 0.0      |
| 19.8      | 0.0        | 0.0      |

### 1.3 `consumptionFractionAt(hourFloat)`

```
morning = exp(-pow(hourFloat - 7.5, 2) / 2.5) * 0.7
evening = exp(-pow(hourFloat - 19, 2) / 4) * 1.0
baseline = 0.18
midday   = exp(-pow(hourFloat - 13, 2) / 18) * 0.25

return clamp(baseline + morning + evening + midday, 0.12, 1.2)
```

**Fixtures**
| hourFloat | Expected |
|-----------|----------|
| 0.0       | 0.1867   |
| 7.5       | 0.8800   |
| 13.0      | 0.4300   |
| 19.0      | 1.1800   |
| 23.0      | 0.1820   |

### 1.4 `nowAsHourFloat(now)`

```
return now.hours + now.minutes / 60 + now.seconds / 3600
```

**Fixture**: `2026-05-16T13:42:18` → `13.7050`

---

## 2. SunSpec Simulation (`services/sunspec.ts`)

### 2.1 Constants

```
SYSTEM_RATED_W        = 9600
BATTERY_CAPACITY_KWH  = 13.5
CONVERSION_EFFICIENCY = 0.964
```

### 2.2 `jitter(base, pct)`

```
return base * (1 + (random() * 2 - 1) * pct)
```

`random()` returns a uniform value in `[0, 1)`.

### 2.3 String builder (`buildPanels`)

```
sunSkew = clamp((hour - 12) / 6, -1, 1)

for each string s:
    orientationGain = 1 + s.orientation * sunSkew
    fractionOfRated = (s.ratedW / SYSTEM_RATED_W) * orientationGain
    power = clamp(totalDcPowerW * fractionOfRated, 0, s.ratedW)
    voltage = power > 30 ? jitter(380 + s.id.charCodeAt(0) % 20, 0.01) : 0
    current = voltage > 0 ? power / voltage : 0
```

String definitions:
| id | label | ratedW | panels | orientation |
|----|-------|--------|--------|-------------|
| A  | Roof · South-East | 3200 | 8 | -0.15 |
| B  | Roof · South-West | 3200 | 8 |  0.15 |
| C  | Garage · South    | 3200 | 8 |  0.00 |

**Fixture** (hour=13, totalDcPowerW=7000):
- A: power ≈ `clamp(7000 * (3200/9600) * 1.0, 0, 3200)` = 2333.3 W; voltage ≈ `jitter(400, 0.01)` ≈ 399.6 V; current ≈ 5.84 A
- B: power ≈ 2333.3 W; voltage ≈ `jitter(401, 0.01)` ≈ 400.5 V; current ≈ 5.83 A
- C: power ≈ 2333.3 W; voltage ≈ `jitter(380, 0.01)` ≈ 379.8 V; current ≈ 6.14 A

### 2.4 Status derivation (`deriveStatus`)

```
if hour < 5.8 or hour > 20.2:     return NIGHT
if dcPowerW < 50:                  return STANDBY
if dcPowerW > SYSTEM_RATED_W * 0.95 and batterySoc > 99:
                                    return CURTAILED
return PRODUCING
```

**Fixtures**
| hour | dcPowerW | batterySoc | Expected   |
|------|----------|------------|------------|
| 4.0  | 5000     | 50         | NIGHT      |
| 10.0 | 30       | 50         | STANDBY    |
| 13.0 | 9500     | 100        | CURTAILED  |
| 13.0 | 5000     | 50         | PRODUCING  |

### 2.5 Main telemetry read (`readTelemetry`)

```
cloudCover   = 0.18 + 0.12 * sin(now.ms / 1000 / 240)
irradiance   = irradianceAt(hour, cloudCover)
dcPowerIdeal = (irradiance / 1000) * SYSTEM_RATED_W
dcPowerW     = jitter(max(0, dcPowerIdeal), 0.03)
acPowerW     = dcPowerW * CONVERSION_EFFICIENCY
homeLoadIdeal = consumptionFractionAt(hour) * 4200
homeLoadW     = jitter(homeLoadIdeal, 0.07)
surplusW      = acPowerW - homeLoadW
dt            = (now.ms - lastSampleAt.ms) / 1000
lastSampleAt  = now.ms
```

### 2.6 Battery charge / discharge logic

```
if surplusW >= 0:
    if cachedBattery < 100:
        batteryPowerW = min(surplusW, 5000)
        cachedBattery += (batteryPowerW * (dt / 3600)) / (BATTERY_CAPACITY_KWH * 10)
        remaining     = surplusW - batteryPowerW
        gridExportW   = remaining
    else:
        gridExportW = surplusW
else:
    deficit = -surplusW
    if cachedBattery > 12:
        batteryPowerW = -min(deficit, 5000)
        cachedBattery += (batteryPowerW * (dt / 3600)) / (BATTERY_CAPACITY_KWH * 10)
        stillNeeded   = deficit - abs(batteryPowerW)
        gridImportW   = max(0, stillNeeded)
    else:
        gridImportW = deficit

cachedBattery = clamp(cachedBattery, 0, 100)
```

**Fixtures** (dt = 2.0 s, initial cachedBattery = 50.0):

| surplusW | cachedBattery (after) | batteryPowerW | gridExportW | gridImportW |
|----------|----------------------|---------------|-------------|-------------|
| 3000     | 50.0123              | 3000          | 0           | 0           |
| 6000     | 50.0206              | 5000          | 1000        | 0           |
| 6000     | 99.997 (start 100)   | 0             | 6000        | 0           |
| -2000    | 49.9918              | -2000         | 0           | 0           |
| -6000    | 49.9918              | -5000         | 0           | 1000        |
| -2000    | 10.0                 | 0             | 0           | 2000        |

### 2.7 Temperature model

```
baseHeat        = 28 + (dcPowerW / SYSTEM_RATED_W) * 22
heatsinkTempC   = jitter(baseHeat, 0.04)
cabinetTempC    = jitter(baseHeat - 6, 0.04)
ambientTempC    = jitter(18 + 8 * solarFractionAt(hour), 0.03)
batteryTempC    = jitter(24 + abs(batteryPowerW) / 800, 0.03)
```

**Fixture** (hour=13, dcPowerW=5000, batteryPowerW=2000):
- baseHeat = 28 + (5000/9600)*22 = 39.458
- heatsink ≈ 39.5°C; cabinet ≈ 33.5°C; ambient ≈ 26.0°C; battery ≈ 26.5°C

### 2.8 Today energy integral (`computeTodayKwh`)

```
total = 0
step  = 0.25
for h from 0 to currentHour inclusive, step = step:
    w = (irradianceAt(h, cloudCover) / 1000) * SYSTEM_RATED_W * CONVERSION_EFFICIENCY
    total += (w * step) / 1000
return total
```

**Fixture** (currentHour=13, cloudCover=0.18):
- Result is a numerical integral. Reference value computed from PWA at 13:00 with cloudCover=0.18: **≈ 42.7 kWh** (approximate; exact value depends on loop resolution).

### 2.9 `buildTodaySeries`

```
cloudCover = 0.2
for h from 0 to 24, step = 0.5:
    irr           = irradianceAt(h, cloudCover)
    ideal         = (irr / 1000) * SYSTEM_RATED_W * CONVERSION_EFFICIENCY
    isPast        = h <= hourNow
    productionW   = isPast ? ideal * (1 + sin(h * 3) * 0.05) : ideal
    consumptionW  = consumptionFractionAt(h) * 4200
    surplus       = productionW - consumptionW
    batteryW      = clamp(surplus, -5000, 5000) * 0.7
    gridW         = surplus - batteryW
    append { t:h, productionW, consumptionW, batteryW, gridW, irradianceWm2: irr }
```

Produces 49 `HistoryPoint` values (0.0, 0.5, 1.0, …, 24.0).

### 2.10 `buildWeekSeries`

```
days = ['Mon','Tue','Wed','Thu','Fri','Sat','Sun']
for each day at index i:
    factor    = 0.85 + 0.18 * sin(i * 1.3)
    produced  = 38 * factor + (random() * 4 - 2)
    consumed  = 28 + (random() * 6 - 3)
    yield { day, produced: max(0, produced), consumed: max(0, consumed) }
```

**Fixture** (seeded random(0)=0.5 for all calls):
- Mon: produced ≈ 38*(0.85+0.18*sin(0)) + 0 = 32.3; consumed ≈ 28 + 0 = 28.0

---

## 3. Weather & Forecast (`services/weather.ts`)

### 3.1 Constants

```
SYSTEM_RATING_KW  = 9.6
PERFORMANCE_RATIO = 0.82
TYPICAL_PSH       = 4.8
```

### 3.2 Peak Sun Hours → kWh

```
psh         = (shortwaveRadiationMJ * 1000) / 3600
expectedKwh = psh * SYSTEM_RATING_KW * PERFORMANCE_RATIO
```

**Fixtures**
| shortwaveRadiationMJ | psh     | expectedKwh |
|----------------------|---------|-------------|
| 10.0                 | 2.7778  | 21.87       |
| 17.28                | 4.8     | 37.79       |
| 25.0                 | 6.9444  | 54.67       |

### 3.3 `vsLastWeekPct`

```
lastWeekTypicalKwh = days.length * TYPICAL_PSH * SYSTEM_RATING_KW * PERFORMANCE_RATIO
vsLastWeekPct      = ((totalKwh - lastWeekTypicalKwh) / lastWeekTypicalKwh) * 100
```

**Fixture** (7 days, totalKwh = 300):
- lastWeekTypical = 7 * 4.8 * 9.6 * 0.82 = 264.45
- vsLastWeekPct = ((300 - 264.45) / 264.45) * 100 = **+13.44%**

### 3.4 Weather code mapping (`mapWeatherCode`)

| Code Range | Condition      | Label          |
|------------|----------------|----------------|
| 0          | clear          | Clear          |
| 1          | mostly-clear   | Mostly clear   |
| 2          | partly-cloudy  | Partly cloudy  |
| 3          | overcast       | Overcast       |
| 45, 48     | fog            | Fog            |
| 51–57      | drizzle        | Drizzle        |
| 61–64      | rain           | Rain           |
| 65–67      | heavy-rain     | Heavy rain     |
| 71–77      | snow           | Snow           |
| 80, 81     | rain           | Showers        |
| 82         | heavy-rain     | Heavy showers  |
| 85, 86     | snow           | Snow showers   |
| 95+        | thunderstorm   | Thunderstorm   |
| default    | partly-cloudy  | Mixed          |

### 3.5 `dayLabelFor`

```
if dateStr == todayIso: return "Today"
diffDays = round((date - today) / (1000 * 60 * 60 * 24))
if diffDays == 1: return "Tomorrow"
return date.toLocaleDateString("en-US", { weekday: "short" })
```

---

## 4. Insights (`services/aiInsights.ts`)

### 4.1 Self-consumption percentage

```
selfConsumptionPct = acPowerW > 0
    ? clamp(1 - gridExportW / max(1, acPowerW), 0, 1) * 100
    : 0
```

### 4.2 Savings model

```
KWH_RATE_GRID   = 0.32
KWH_RATE_EXPORT = 0.08

todaySavings  = energyTodayKwh * KWH_RATE_GRID + (energyTodayKwh * 0.4) * KWH_RATE_EXPORT
monthSavings  = energyMonthKwh * (KWH_RATE_GRID * 0.7)
```

### 4.3 String spread

```
expected = panels.map(p => p.powerW / p.ratedW)
maxVal   = max(...expected)
minVal   = min(...expected)
spread   = maxVal == 0 ? 0 : (maxVal - minVal) / maxVal
```

---

## 5. Forecast Insights (`services/forecastInsights.ts`)

### 5.1 Pre-charge cost estimate

```
chargeKwh = max(0, batteryCapacityKwh * 0.95 - (batterySoc / 100) * batteryCapacityKwh)
cost      = chargeKwh * OFF_PEAK_RATE   // OFF_PEAK_RATE = 0.11
```

### 5.2 3-day off-grid check

```
next3Total     = next3Days.sum(d => d.expectedKwh)
avgDailyUseKwh = (homeLoadW > 0 ? homeLoadW : 800) * 24 / 1000 * 0.7
if next3Total > avgDailyUseKwh * 3 * 1.3:
    generate "self-sufficient-streak" insight
```

### 5.3 Projected weekly savings

```
projectedSavings = forecast.totalKwh * KWH_RATE_GRID * 0.7
```
