import type { InverterStatus } from '../types';

const COPY: Record<InverterStatus, { label: string; color: string; dot: string }> = {
  PRODUCING: { label: 'Producing', color: 'text-signal-flow', dot: 'bg-signal-flow' },
  STANDBY: { label: 'Standby', color: 'text-bone-400', dot: 'bg-bone-500' },
  CURTAILED: { label: 'Curtailed', color: 'text-signal-solar', dot: 'bg-signal-solar' },
  NIGHT: { label: 'Night', color: 'text-bone-500', dot: 'bg-bone-600' },
  FAULT: { label: 'Fault', color: 'text-signal-alert', dot: 'bg-signal-alert' }
};

export function StatusPill({ status }: { status: InverterStatus }) {
  const c = COPY[status];
  return (
    <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-hairline bg-carbon-800/60 whitespace-nowrap shrink-0">
      <span className={`size-1.5 rounded-full ${c.dot} animate-pulse-soft`} />
      <span className={`text-[11px] tracking-[0.18em] uppercase font-mono ${c.color}`}>{c.label}</span>
    </div>
  );
}
