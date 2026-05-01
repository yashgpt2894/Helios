import { Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';

interface WeekChartProps {
  data: { day: string; produced: number; consumed: number }[];
}

export function WeekChart({ data }: WeekChartProps) {
  return (
    <div className="w-full h-[180px]">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} margin={{ top: 12, right: 4, left: 0, bottom: 0 }} barCategoryGap={14}>
          <XAxis
            dataKey="day"
            stroke="var(--hairline-strong)"
            tick={{ fill: 'var(--text-500)', fontSize: 10, fontFamily: 'JetBrains Mono' }}
            tickLine={false}
            axisLine={false}
          />
          <YAxis
            stroke="var(--hairline-strong)"
            tick={{ fill: 'var(--text-500)', fontSize: 10, fontFamily: 'JetBrains Mono' }}
            tickLine={false}
            axisLine={false}
            width={28}
            tickFormatter={(v) => `${v}`}
          />
          <Tooltip
            cursor={{ fill: 'var(--hairline)' }}
            contentStyle={{
              background: 'var(--surface-raised-from)',
              border: '1px solid var(--hairline-strong)',
              borderRadius: 8,
              fontSize: 11,
              fontFamily: 'JetBrains Mono',
              color: 'var(--text-100)'
            }}
            formatter={(v: number) => `${v.toFixed(1)} kWh`}
            labelStyle={{ color: 'var(--text-500)', fontSize: 10, textTransform: 'uppercase', letterSpacing: '0.2em' }}
          />
          <Bar dataKey="produced" fill="var(--signal-solar)" radius={[3, 3, 0, 0]} name="Produced" />
          <Bar dataKey="consumed" fill="var(--text-500)" fillOpacity={0.32} radius={[3, 3, 0, 0]} name="Consumed" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
