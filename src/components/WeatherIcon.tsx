import {
  Sun,
  CloudSun,
  Cloud,
  Cloudy,
  CloudFog,
  CloudDrizzle,
  CloudRain,
  CloudRainWind,
  CloudSnow,
  CloudLightning
} from 'lucide-react';
import type { WeatherCondition } from '../types';

interface WeatherIconProps {
  condition: WeatherCondition;
  size?: number;
  className?: string;
  strokeWidth?: number;
}

const MAP = {
  clear: Sun,
  'mostly-clear': Sun,
  'partly-cloudy': CloudSun,
  overcast: Cloudy,
  fog: CloudFog,
  drizzle: CloudDrizzle,
  rain: CloudRain,
  'heavy-rain': CloudRainWind,
  snow: CloudSnow,
  thunderstorm: CloudLightning
} as const;

const TONE = {
  clear: 'text-signal-solar',
  'mostly-clear': 'text-signal-solar',
  'partly-cloudy': 'text-bone-300',
  overcast: 'text-bone-400',
  fog: 'text-bone-500',
  drizzle: 'text-signal-grid',
  rain: 'text-signal-grid',
  'heavy-rain': 'text-signal-grid',
  snow: 'text-bone-200',
  thunderstorm: 'text-signal-alert'
} as const;

export function WeatherIcon({ condition, size = 18, className, strokeWidth = 1.5 }: WeatherIconProps) {
  const Icon = MAP[condition] ?? Cloud;
  return <Icon size={size} strokeWidth={strokeWidth} className={`${TONE[condition]} ${className ?? ''}`} />;
}
