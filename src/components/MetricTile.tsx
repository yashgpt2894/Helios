import type { ReactNode } from 'react';

interface MetricTileProps {
  label: string;
  value: ReactNode;
  unit?: string;
  delta?: string;
  deltaTone?: 'positive' | 'negative' | 'neutral';
  icon?: ReactNode;
  subtle?: ReactNode;
}

export function MetricTile({ label, value, unit, delta, deltaTone = 'neutral', icon, subtle }: MetricTileProps) {
  const tone =
    deltaTone === 'positive'
      ? 'text-signal-flow'
      : deltaTone === 'negative'
      ? 'text-signal-alert'
      : 'text-bone-500';

  return (
    <div className="surface p-4 flex flex-col gap-3 min-h-[112px]">
      <div className="flex items-center justify-between">
        <span className="label-cap">{label}</span>
        {icon && <span className="text-bone-500">{icon}</span>}
      </div>
      <div className="flex items-baseline gap-1.5 mt-auto">
        <span className="num-display text-3xl text-bone-100 leading-none">{value}</span>
        {unit && <span className="text-[11px] text-bone-500 font-mono uppercase tracking-wider">{unit}</span>}
      </div>
      {(delta || subtle) && (
        <div className="flex items-center justify-between text-[10px] font-mono uppercase tracking-widest">
          {delta && <span className={tone}>{delta}</span>}
          {subtle && <span className="text-bone-600 ml-auto">{subtle}</span>}
        </div>
      )}
    </div>
  );
}
