import type { ReactNode } from 'react';

interface SectionHeaderProps {
  eyebrow?: string;
  title: string;
  trailing?: ReactNode;
  description?: string;
}

export function SectionHeader({ eyebrow, title, trailing, description }: SectionHeaderProps) {
  return (
    <div className="flex items-end justify-between mb-3 px-1">
      <div>
        {eyebrow && <div className="label-cap mb-1.5">{eyebrow}</div>}
        <h2 className="text-bone-100 text-[17px] font-medium tracking-tight">{title}</h2>
        {description && <p className="text-bone-500 text-[12px] mt-0.5">{description}</p>}
      </div>
      {trailing && <div className="text-bone-500 text-[11px] font-mono">{trailing}</div>}
    </div>
  );
}
