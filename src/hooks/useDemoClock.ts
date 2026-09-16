import { useCallback, useEffect, useMemo, useState } from 'react';
import { useReducedMotion } from 'framer-motion';
import { DEMO_POLL_MS, DEMO_START_HOUR, DEMO_STEP_HOURS, demoTelemetryAt } from '../lib/demoDay';
import type { SolarTelemetry } from '../types';

export interface DemoClock {
  t: SolarTelemetry;
  hour: number;
  tick: number;
  paused: boolean;
  running: boolean;
  reduced: boolean;
  toggle: () => void;
  step: () => void;
}

export function useDemoClock(): DemoClock {
  const reduced = useReducedMotion() === true;
  const [paused, setPaused] = useState(false);
  const [visible, setVisible] = useState(
    () => typeof document === 'undefined' || document.visibilityState === 'visible'
  );
  const [tick, setTick] = useState(0);
  const running = !paused && visible && !reduced;

  const step = useCallback(() => setTick((n) => n + 1), []);
  const toggle = useCallback(() => setPaused((p) => !p), []);

  useEffect(() => {
    const onVisibility = () => setVisible(document.visibilityState === 'visible');
    document.addEventListener('visibilitychange', onVisibility);
    return () => document.removeEventListener('visibilitychange', onVisibility);
  }, []);

  useEffect(() => {
    if (!running) return;
    const id = setInterval(step, DEMO_POLL_MS);
    return () => clearInterval(id);
  }, [running, step]);

  const hour = Math.round(((DEMO_START_HOUR + tick * DEMO_STEP_HOURS) % 24) * 1000) / 1000;
  const t = useMemo(() => demoTelemetryAt(hour, tick), [hour, tick]);

  return { t, hour, tick, paused, running, reduced, toggle, step };
}
