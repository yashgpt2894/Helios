import { motion, useMotionValue, useTransform, animate } from 'framer-motion';
import { useEffect } from 'react';

interface LiveNumberProps {
  value: number;
  digits?: number;
  className?: string;
  prefix?: string;
  suffix?: string;
}

export function LiveNumber({ value, digits = 2, className, prefix, suffix }: LiveNumberProps) {
  const mv = useMotionValue(value);
  const display = useTransform(mv, (v) => {
    const formatted = v.toFixed(digits);
    return `${prefix ?? ''}${formatted}${suffix ?? ''}`;
  });

  useEffect(() => {
    const controls = animate(mv, value, { duration: 0.8, ease: [0.16, 1, 0.3, 1] });
    return () => controls.stop();
  }, [value, mv]);

  return <motion.span className={className}>{display}</motion.span>;
}
