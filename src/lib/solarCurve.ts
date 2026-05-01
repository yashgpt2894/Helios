import { clamp } from './format';

const SUNRISE_HOUR = 6.2;
const SUNSET_HOUR = 19.8;
const PEAK_HOUR = 13.0;

export function solarFractionAt(hourFloat: number): number {
  if (hourFloat <= SUNRISE_HOUR || hourFloat >= SUNSET_HOUR) return 0;
  const x = (hourFloat - SUNRISE_HOUR) / (SUNSET_HOUR - SUNRISE_HOUR);
  const bell = Math.sin(Math.PI * x);
  const skew = 1 - Math.abs(hourFloat - PEAK_HOUR) / 8;
  return clamp(bell * skew, 0, 1);
}

export function irradianceAt(hourFloat: number, cloudCover: number): number {
  const clear = solarFractionAt(hourFloat) * 1000;
  const transmittance = 1 - cloudCover * 0.7;
  return Math.max(0, clear * transmittance);
}

export function consumptionFractionAt(hourFloat: number): number {
  const morning = Math.exp(-Math.pow(hourFloat - 7.5, 2) / 2.5) * 0.7;
  const evening = Math.exp(-Math.pow(hourFloat - 19, 2) / 4) * 1.0;
  const baseline = 0.18;
  const midday = Math.exp(-Math.pow(hourFloat - 13, 2) / 18) * 0.25;
  return clamp(baseline + morning + evening + midday, 0.12, 1.2);
}

export function nowAsHourFloat(now = new Date()): number {
  return now.getHours() + now.getMinutes() / 60 + now.getSeconds() / 3600;
}
