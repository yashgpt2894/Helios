import { Area, AreaChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import type { HistoryPoint } from '../types';
import { nowAsHourFloat } from '../lib/solarCurve';

interface ProductionChartProps {
  data: HistoryPoint[];
  height?: number;
}

export function ProductionChart({ data, height = 200 }: ProductionChartProps) {
  const hourNow = nowAsHourFloat();
  return (
    <div style={{ height }} className="w-full">
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart data={data} margin={{ top: 10, right: 0, left: 0, bottom: 0 }}>
          <defs>
            <linearGradient id="prodGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="var(--signal-solar)" stopOpacity={0.45} />
              <stop offset="100%" stopColor="var(--signal-solar)" stopOpacity={0.0} />
            </linearGradient>
            <linearGradient id="consGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="var(--text-500)" stopOpacity={0.22} />
              <stop offset="100%" stopColor="var(--text-500)" stopOpacity={0.0} />
            </linearGradient>
          </defs>
          <XAxis
            dataKey="t"
            type="number"
            domain={[0, 24]}
            ticks={[0, 6, 12, 18, 24]}
            stroke="var(--hairline-strong)"
            tickFormatter={(v) => (v === 0 ? '12a' : v === 12 ? '12p' : v === 24 ? '12a' : `${v % 12}${v < 12 ? 'a' : 'p'}`)}
            tick={{ fill: 'var(--text-500)', fontSize: 10, fontFamily: 'JetBrains Mono' }}
            tickLine={false}
            axisLine={false}
          />
          <YAxis
            stroke="var(--hairline-strong)"
            tickFormatter={(v) => `${(v / 1000).toFixed(0)}`}
            tick={{ fill: 'var(--text-500)', fontSize: 10, fontFamily: 'JetBrains Mono' }}
            tickLine={false}
            axisLine={false}
            width={28}
          />
          <Tooltip
            contentStyle={{
              background: 'var(--surface-raised-from)',
              border: '1px solid var(--hairline-strong)',
              borderRadius: 8,
              fontSize: 11,
              fontFamily: 'JetBrains Mono',
              color: 'var(--text-100)'
            }}
            labelStyle={{ color: 'var(--text-500)', fontSize: 10, textTransform: 'uppercase', letterSpacing: '0.2em' }}
            itemStyle={{ color: 'var(--text-100)' }}
            formatter={(v: number) => `${(v / 1000).toFixed(2)} kW`}
            labelFormatter={(v: number) => `${Math.floor(v)}:${String(Math.round((v % 1) * 60)).padStart(2, '0')}`}
          />
          <Area
            type="monotone"
            dataKey="consumptionW"
            stroke="var(--text-400)"
            strokeWidth={1}
            strokeDasharray="3 3"
            fill="url(#consGrad)"
            name="Home"
          />
          <Area
            type="monotone"
            dataKey="productionW"
            stroke="var(--signal-solar)"
            strokeWidth={1.6}
            fill="url(#prodGrad)"
            name="Solar"
          />
          <line
            x1={`${(hourNow / 24) * 100}%`}
            x2={`${(hourNow / 24) * 100}%`}
            y1="0"
            y2="100%"
            stroke="var(--signal-solar)"
            strokeOpacity="0.5"
            strokeWidth={1}
            strokeDasharray="2 2"
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
