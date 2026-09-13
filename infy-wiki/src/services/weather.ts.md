# Weather

```ts
import type { ForecastDay, Location, ProductionForecast, WeatherCondition } from '../types';

const SYSTEM_RATING_KW = 9.6;
const PERFORMANCE_RATIO = 0.82;
const TYPICAL_PSH = 4.8;

const DEFAULT_LOCATION: Location = {
  lat: 37.7749,
  lng: -122.4194,
  label: 'San Francisco, CA',
  source: 'default'
};

export function getDefaultLocation(): Location {
  return DEFAULT_LOCATION;
}

export async function requestBrowserLocation(): Promise<Location | null> {
  if (typeof navigator === 'undefined' || !navigator.geolocation) return null;
  return new Promise((resolve) => {
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const { latitude, longitude } = pos.coords;
        const label = await reverseGeocode(latitude, longitude);
        resolve({ lat: latitude, lng: longitude, label, source: 'browser' });
      },
      () => resolve(null),
      { timeout: 10000, maximumAge: 1000 * 60 * 60 }
    );
  });
}

async function reverseGeocode(lat: number, lng: number): Promise<string> {
  try {
    const url = `https://geocoding-api.open-meteo.com/v1/search?latitude=${lat}&longitude=${lng}&count=1&language=en&format=json`;
    const r = await fetch(url);
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    const data = await r.json();
    const result = data?.results?.[0];
    if (result) {
      const region = result.admin1 || result.country_code || '';
      return region ? `${result.name}, ${region}` : result.name;
    }
  } catch {
    /* fall through */
  }
  return `${lat.toFixed(2)}, ${lng.toFixed(2)}`;
}

interface OpenMeteoResponse {
  daily: {
    time: string[];
    weather_code: number[];
    temperature_2m_max: number[];
    temperature_2m_min: number[];
    precipitation_sum: number[];
    shortwave_radiation_sum: number[];
    cloud_cover_mean: number[];
  };
}

export async function fetchForecast(location: Location, days = 7): Promise<ProductionForecast> {
  const params = new URLSearchParams({
    latitude: location.lat.toString(),
    longitude: location.lng.toString(),
    daily: 'weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,shortwave_radiation_sum,cloud_cover_mean',
    forecast_days: days.toString(),
    timezone: 'auto'
  });
  const url = `https://api.open-meteo.com/v1/forecast?${params}`;

  const r = await fetch(url);
  if (!r.ok) throw new Error(`Forecast API error: HTTP ${r.status}`);
  const data = (await r.json()) as OpenMeteoResponse;

  const forecastDays: ForecastDay[] = data.daily.time.map((date, i) => {
    const radiationMJ = data.daily.shortwave_radiation_sum[i] ?? 0;
    const psh = (radiationMJ * 1000) / 3600;
    const expectedKwh = psh * SYSTEM_RATING_KW * PERFORMANCE_RATIO;
    const code = data.daily.weather_code[i] ?? 0;
    const condition = mapWeatherCode(code);
    const typicalKwh = TYPICAL_PSH * SYSTEM_RATING_KW * PERFORMANCE_RATIO;
    return {
      date,
      weatherCode: code,
      condition: condition.condition,
      conditionLabel: condition.label,
      tempHighC: data.daily.temperature_2m_max[i] ?? 0,
      tempLowC: data.daily.temperature_2m_min[i] ?? 0,
      precipitationMm: data.daily.precipitation_sum[i] ?? 0,
      shortwaveRadiationMJ: radiationMJ,
      cloudCoverPct: data.daily.cloud_cover_mean[i] ?? 0,
      expectedKwh,
      expectedKwhVsTypical: typicalKwh > 0 ? (expectedKwh - typicalKwh) / typicalKwh : 0
    };
  });

  const totalKwh = forecastDays.reduce((s, d) => s + d.expectedKwh, 0);
  const lastWeekTypicalKwh = forecastDays.length * TYPICAL_PSH * SYSTEM_RATING_KW * PERFORMANCE_RATIO;
  const vsLastWeekPct = lastWeekTypicalKwh > 0 ? ((totalKwh - lastWeekTypicalKwh) / lastWeekTypicalKwh) * 100 : 0;

  return {
    fetchedAt: Date.now(),
    location,
    days: forecastDays,
    totalKwh,
    vsLastWeekPct
  };
}

export function mapWeatherCode(code: number): { condition: WeatherCondition; label: string } {
  if (code === 0) return { condition: 'clear', label: 'Clear' };
  if (code === 1) return { condition: 'mostly-clear', label: 'Mostly clear' };
  if (code === 2) return { condition: 'partly-cloudy', label: 'Partly cloudy' };
  if (code === 3) return { condition: 'overcast', label: 'Overcast' };
  if (code === 45 || code === 48) return { condition: 'fog', label: 'Fog' };
  if (code >= 51 && code <= 57) return { condition: 'drizzle', label: 'Drizzle' };
  if (code >= 61 && code <= 67) {
    return code >= 65 ? { condition: 'heavy-rain', label: 'Heavy rain' } : { condition: 'rain', label: 'Rain' };
  }
  if (code >= 71 && code <= 77) return { condition: 'snow', label: 'Snow' };
  if (code >= 80 && code <= 82) {
    return code === 82 ? { condition: 'heavy-rain', label: 'Heavy showers' } : { condition: 'rain', label: 'Showers' };
  }
  if (code === 85 || code === 86) return { condition: 'snow', label: 'Snow showers' };
  if (code >= 95) return { condition: 'thunderstorm', label: 'Thunderstorm' };
  return { condition: 'partly-cloudy', label: 'Mixed' };
}

export function dayLabelFor(dateStr: string, todayIso: string): string {
  if (dateStr === todayIso) return 'Today';
  const d = new Date(dateStr + 'T00:00:00');
  const today = new Date(todayIso + 'T00:00:00');
  const diffDays = Math.round((d.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  if (diffDays === 1) return 'Tomorrow';
  return d.toLocaleDateString('en-US', { weekday: 'short' });
}

```