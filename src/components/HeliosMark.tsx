import type { Brand } from '../types';

interface HeliosMarkProps {
  size?: number;
  withBackground?: boolean;
  className?: string;
  brand?: Brand;
}

export function HeliosMark({ size = 36, withBackground = false, className, brand }: HeliosMarkProps) {
  const id = `mark-${size}`;

  if (brand && brand.mark === 'text') {
    return <TextMark size={size} brand={brand} withBackground={withBackground} className={className} />;
  }

  return (
    <svg
      viewBox="0 0 1024 1024"
      width={size}
      height={size}
      className={className}
      role="img"
      aria-label="helios mark"
    >
      <defs>
        <radialGradient id={`bg-${id}`} cx="50%" cy="38%" r="75%">
          <stop offset="0%" stopColor="#1a1a1c" />
          <stop offset="60%" stopColor="#0f0f10" />
          <stop offset="100%" stopColor="#070708" />
        </radialGradient>
        <linearGradient id={`blade-${id}`} x1="0" y1="-92" x2="0" y2="0" gradientUnits="userSpaceOnUse">
          <stop offset="0%" stopColor="#f4f1ea" />
          <stop offset="55%" stopColor="#dcd6c8" />
          <stop offset="100%" stopColor="#a59f90" />
        </linearGradient>
        <radialGradient id={`iris-${id}`} cx="50%" cy="32%" r="70%">
          <stop offset="0%" stopColor="#1d1d1f" />
          <stop offset="100%" stopColor="#0a0a0b" />
        </radialGradient>
        <clipPath id={`clip-${id}`}>
          <circle cx="0" cy="0" r="92" />
        </clipPath>
      </defs>
      {withBackground && <rect width="1024" height="1024" rx="180" fill={`url(#bg-${id})`} />}
      <g transform="translate(512 512) scale(3.6)">
        <circle cx="0" cy="0" r="98" fill="none" stroke="#efece5" strokeWidth="0.6" opacity="0.35" />
        <circle cx="0" cy="0" r="92" fill="none" stroke="#efece5" strokeWidth="1.6" />
        <g clipPath={`url(#clip-${id})`}>
          <g fill={`url(#blade-${id})`} stroke="#efece5" strokeWidth="0.4" strokeLinejoin="miter">
            {[0, 45, 90, 135, 180, 225, 270, 315].map((r) => (
              <g key={r} transform={`rotate(${r})`}>
                <polygon points="0,-92 78,-46 0,0" />
              </g>
            ))}
          </g>
          <g stroke="#0b0b0c" strokeWidth="0.7" opacity="0.45">
            {[0, 45, 90, 135, 180, 225, 270, 315].map((r) => (
              <g key={r} transform={`rotate(${r})`}>
                <line x1="0" y1="0" x2="0" y2="-92" />
              </g>
            ))}
          </g>
        </g>
        <circle cx="0" cy="0" r="26" fill={`url(#iris-${id})`} />
        <circle cx="0" cy="0" r="26" fill="none" stroke="#efece5" strokeWidth="1.2" />
        <circle cx="0" cy="0" r="22" fill="none" stroke="#efece5" strokeWidth="0.4" opacity="0.5" />
        <circle cx="0" cy="0" r="5" fill="#efece5" />
      </g>
    </svg>
  );
}

function TextMark({
  size,
  brand,
  withBackground,
  className
}: {
  size: number;
  brand: Brand;
  withBackground?: boolean;
  className?: string;
}) {
  const letter = brand.textMark ?? brand.name.charAt(0).toUpperCase();
  const radius = size / 2 - 1;
  return (
    <svg
      viewBox={`0 0 ${size} ${size}`}
      width={size}
      height={size}
      className={className}
      role="img"
      aria-label={`${brand.name} mark`}
    >
      <defs>
        <linearGradient id={`brand-${brand.id}`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor={brand.accent} stopOpacity="1" />
          <stop offset="100%" stopColor={brand.accent} stopOpacity="0.7" />
        </linearGradient>
      </defs>
      {withBackground && <rect width={size} height={size} rx={size * 0.18} fill="#0b0b0c" />}
      <circle cx={size / 2} cy={size / 2} r={radius} fill={withBackground ? '#141416' : `url(#brand-${brand.id})`} stroke={brand.accent} strokeWidth="1.5" />
      <text
        x={size / 2}
        y={size / 2}
        textAnchor="middle"
        dominantBaseline="central"
        fill={withBackground ? brand.accent : '#0b0b0c'}
        style={{
          fontFamily: 'Instrument Serif, Georgia, serif',
          fontSize: size * 0.55,
          fontWeight: 500,
          letterSpacing: '-0.04em'
        }}
      >
        {letter}
      </text>
    </svg>
  );
}
