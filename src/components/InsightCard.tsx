import {
  Sparkles,
  TrendingUp,
  Wrench,
  CloudSun,
  CircleAlert,
  Zap,
  BatteryCharging,
  ArrowRight
} from 'lucide-react';
import { motion } from 'framer-motion';
import type { Insight } from '../types';

const CATEGORY_ICON = {
  production: Zap,
  consumption: TrendingUp,
  battery: BatteryCharging,
  savings: Sparkles,
  maintenance: Wrench,
  forecast: CloudSun
} as const;

const SEVERITY_STYLE = {
  positive: { ring: 'ring-signal-flow/30', dot: 'bg-signal-flow', text: 'text-signal-flow' },
  neutral: { ring: 'ring-bone-700/40', dot: 'bg-bone-400', text: 'text-bone-300' },
  attention: { ring: 'ring-signal-solar/40', dot: 'bg-signal-solar', text: 'text-signal-solar' },
  critical: { ring: 'ring-signal-alert/50', dot: 'bg-signal-alert', text: 'text-signal-alert' }
} as const;

interface InsightCardProps {
  insight: Insight;
  index?: number;
  compact?: boolean;
}

export function InsightCard({ insight, index = 0, compact }: InsightCardProps) {
  const Icon = CATEGORY_ICON[insight.category] ?? Sparkles;
  const sev = SEVERITY_STYLE[insight.severity];

  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: index * 0.05, ease: [0.16, 1, 0.3, 1] }}
      className={`surface p-4 ${compact ? 'min-h-[120px]' : ''}`}
    >
      <div className="flex items-start gap-3">
        <div className={`size-9 shrink-0 rounded-full bg-carbon-800 border border-hairline flex items-center justify-center text-bone-300`}>
          <Icon size={16} strokeWidth={1.4} />
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1.5">
            <span className={`size-1.5 rounded-full ${sev.dot} animate-pulse-soft`} />
            <span className="label-cap">{insight.category}</span>
            {insight.metric && (
              <span className={`ml-auto text-[10px] font-mono ${sev.text}`}>
                {insight.metric}
              </span>
            )}
          </div>
          <h3 className="text-bone-100 text-[14px] font-medium leading-snug mb-1">{insight.title}</h3>
          <p className="text-bone-400 text-[12px] leading-relaxed">{insight.body}</p>
          {insight.actionLabel && (
            <button className="mt-3 inline-flex items-center gap-1.5 text-[11px] font-mono uppercase tracking-widest text-bone-200 hover:text-bone-100 transition-colors">
              {insight.actionLabel}
              <ArrowRight size={12} strokeWidth={1.6} />
            </button>
          )}
        </div>
      </div>
    </motion.div>
  );
}

export function InsightHighlight({ insight }: { insight: Insight }) {
  const Icon = CATEGORY_ICON[insight.category] ?? Sparkles;
  const sev = SEVERITY_STYLE[insight.severity];
  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.98 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
      className={`surface-raised p-5 ring-1 ${sev.ring}`}
    >
      <div className="flex items-center gap-2 mb-3">
        <CircleAlert size={12} strokeWidth={1.6} className="text-bone-400" />
        <span className="label-cap">helios° insight</span>
        <span className={`ml-auto label-cap ${sev.text}`}>{insight.severity}</span>
      </div>
      <div className="flex gap-3">
        <div className={`size-10 rounded-full border border-hairline bg-carbon-800 flex items-center justify-center text-bone-200`}>
          <Icon size={18} strokeWidth={1.4} />
        </div>
        <div className="flex-1">
          <h3 className="text-bone-100 text-[16px] font-medium leading-snug mb-1">{insight.title}</h3>
          <p className="text-bone-300 text-[13px] leading-relaxed">{insight.body}</p>
        </div>
      </div>
    </motion.div>
  );
}
