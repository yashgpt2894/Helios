/**
 * Authored pictograms, drawn on the sheet's own rule weights. They replace the emoji/unicode-glyph
 * habit: every mark here is a path, in one stroke weight, in the sheet's current ink.
 *
 * The dual-supply pictogram is the standard arrangement for a residential PV label: a house, two
 * supplies entering it, one of them from an array on the roof.
 */

interface PictProps {
  className?: string;
  title?: string;
}

/** House with two incoming supplies and an array on the roof. Icon scale, keyline construction. */
export function DualSupplyPictogram({ className, title }: PictProps) {
  return (
    <svg
      className={className}
      viewBox="0 0 120 120"
      role="img"
      aria-label={title ?? 'House with two supplies, one from a rooftop array'}
      fill="none"
    >
      <g stroke="currentColor" strokeWidth="4" strokeLinejoin="miter" strokeLinecap="butt">
        {/* rooftop array: three tilted modules on a rail */}
        <g transform="translate(30 12) rotate(-18)">
          <rect x="0" y="0" width="60" height="8" />
          <rect x="0" y="12" width="60" height="8" />
        </g>
        {/* roof and house */}
        <path d="M60 34 L104 62 L104 104 L16 104 L16 62 Z" />
        {/* grid supply, entering from the left wall */}
        <path d="M4 82 L16 82" />
        <path d="M8 74 L8 90" />
        {/* array supply, entering from the roof line */}
        <path d="M44 44 L60 44" />
        <path d="M44 36 L44 52" />
      </g>
      <g fill="currentColor">
        <rect x="62" y="74" width="22" height="30" />
      </g>
    </svg>
  );
}

/** The DC hazard mark: a bolt inside a triangle. Used once, where the hazard is real. */
export function HazardBoltPictogram({ className, title }: PictProps) {
  return (
    <svg
      className={className}
      viewBox="0 0 120 120"
      role="img"
      aria-label={title ?? 'Electrical hazard'}
      fill="none"
    >
      <path
        d="M60 8 L112 106 L8 106 Z"
        stroke="currentColor"
        strokeWidth="6"
        strokeLinejoin="miter"
        fill="currentColor"
        fillOpacity="0.08"
      />
      <path d="M66 34 L44 70 L60 70 L54 96 L78 58 L62 58 Z" fill="currentColor" stroke="currentColor" strokeWidth="4" strokeLinejoin="miter" />
    </svg>
  );
}

/** Chevron used on conductor links. Direction is stated in words beside it as well. */
export function FlowArrow({ direction = 'down', className }: { direction?: 'down' | 'up' | 'right'; className?: string }) {
  const rotate = direction === 'down' ? 0 : direction === 'up' ? 180 : -90;
  return (
    <svg className={className} viewBox="0 0 24 20" width="22" height="18" aria-hidden="true" focusable="false">
      <g transform={`rotate(${rotate} 12 10)`} fill="currentColor">
        <path d="M12 18 L2 2 L22 2 Z" />
      </g>
    </svg>
  );
}

/** Printed legend swatch: solid area, one hatch direction, the other hatch direction. */
export function LegendMark({ kind, className }: { kind: 'solid' | 'surplus' | 'deficit' | 'line'; className?: string }) {
  const id = `legend-${kind}`;
  return (
    <svg className={className} viewBox="0 0 28 14" width="28" height="14" aria-hidden="true" focusable="false">
      <defs>
        <pattern id={`${id}-hatch`} width="6" height="6" patternUnits="userSpaceOnUse" patternTransform="rotate(45)">
          <rect width="6" height="6" fill="none" />
          <line x1="0" y1="0" x2="0" y2="6" stroke="currentColor" strokeWidth="3" />
        </pattern>
        <pattern id={`${id}-hatch-back`} width="6" height="6" patternUnits="userSpaceOnUse" patternTransform="rotate(-45)">
          <rect width="6" height="6" fill="none" />
          <line x1="0" y1="0" x2="0" y2="6" stroke="currentColor" strokeWidth="2" />
        </pattern>
      </defs>
      {kind === 'solid' && <rect x="0" y="1" width="28" height="12" fill="currentColor" />}
      {kind === 'surplus' && <rect x="0" y="1" width="28" height="12" fill={`url(#${id}-hatch)`} />}
      {kind === 'deficit' && <rect x="0" y="1" width="28" height="12" fill={`url(#${id}-hatch-back)`} />}
      {kind === 'line' && <line x1="0" y1="7" x2="28" y2="7" stroke="currentColor" strokeWidth="2.5" />}
    </svg>
  );
}
