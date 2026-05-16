package com.helios.core.domain.model

enum class WeatherCondition {
    clear,
    mostlyClear,   // "mostly-clear" in PWA
    partlyCloudy,  // "partly-cloudy" in PWA
    overcast,
    fog,
    drizzle,
    rain,
    heavyRain,     // "heavy-rain" in PWA
    snow,
    thunderstorm
}
