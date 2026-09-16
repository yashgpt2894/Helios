import { useEffect, useRef, useState } from 'react';

interface BallisticsOptions {
  /** Spring constant. Higher settles faster. VU-style movement uses a low value. */
  stiffness?: number;
  /** Damping coefficient. Below critical damping the value overshoots once, as a needle does. */
  damping?: number;
}

function prefersStillness(): boolean {
  return (
    typeof window !== 'undefined' &&
    typeof window.matchMedia === 'function' &&
    window.matchMedia('(prefers-reduced-motion: reduce)').matches
  );
}

/**
 * Moving a printed reading is a physical event, not a CSS tween: the number has mass, so it leans
 * toward its new value and settles. Under `prefers-reduced-motion` the reading is set instantly.
 */
export function useBallistics(target: number, options: BallisticsOptions = {}): number {
  const { stiffness = 210, damping = 26 } = options;
  const [value, setValue] = useState(target);
  const state = useRef({ x: target, v: 0, frame: 0 });

  useEffect(() => {
    if (prefersStillness()) {
      state.current = { x: target, v: 0, frame: 0 };
      setValue(target);
      return;
    }

    const s = state.current;
    let last = performance.now();

    const step = (now: number) => {
      const dt = Math.min((now - last) / 1000, 0.05);
      last = now;
      const a = stiffness * (target - s.x) - damping * s.v;
      s.v += a * dt;
      s.x += s.v * dt;

      if (Math.abs(target - s.x) < 0.0006 && Math.abs(s.v) < 0.002) {
        s.x = target;
        s.v = 0;
        setValue(target);
        s.frame = 0;
        return;
      }
      setValue(s.x);
      s.frame = requestAnimationFrame(step);
    };

    s.frame = requestAnimationFrame(step);
    return () => {
      if (s.frame) cancelAnimationFrame(s.frame);
      s.frame = 0;
    };
  }, [target, stiffness, damping]);

  return value;
}
