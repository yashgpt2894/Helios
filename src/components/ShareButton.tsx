import { Share2, Check, Copy } from 'lucide-react';
import { useState } from 'react';
import { useStore } from '../store/useStore';
import { buildShareUrl, buildSnapshot, shareOrCopy } from '../services/share';

export function ShareButton() {
  const t = useStore((s) => s.telemetry);
  const location = useStore((s) => s.location);
  const brand = useStore((s) => s.brand);
  const forecast = useStore((s) => s.forecast);
  const [state, setState] = useState<'idle' | 'copied' | 'shared'>('idle');

  async function handleShare() {
    const fc = forecast?.days.slice(0, 5).map((d) => d.expectedKwh);
    const snapshot = buildSnapshot(t, location.label, brand.id, fc);
    const url = buildShareUrl(snapshot);
    const result = await shareOrCopy(url, brand);
    if (result === 'shared') setState('shared');
    else if (result === 'copied') setState('copied');
    setTimeout(() => setState('idle'), 2400);
  }

  const Icon = state === 'copied' || state === 'shared' ? Check : Share2;
  const label = state === 'copied' ? 'Copied' : state === 'shared' ? 'Shared' : 'Share snapshot';

  return (
    <button
      onClick={handleShare}
      aria-label={label}
      title={label}
      className="size-7 rounded-full border border-hairline bg-carbon-800/60 flex items-center justify-center text-bone-400 hover:text-bone-100 transition-colors shrink-0"
    >
      <Icon size={12} strokeWidth={1.7} />
    </button>
  );
}

export function ShareButtonPlain({ url }: { url: string }) {
  const [copied, setCopied] = useState(false);
  async function copy() {
    try {
      await navigator.clipboard.writeText(url);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      /* no-op */
    }
  }
  return (
    <button
      onClick={copy}
      className="inline-flex items-center gap-2 px-3 py-2 rounded-lg border border-hairline bg-carbon-800/60 text-bone-300 hover:text-bone-100 transition-colors"
    >
      {copied ? <Check size={13} strokeWidth={1.7} /> : <Copy size={13} strokeWidth={1.7} />}
      <span className="text-[11px] font-mono">{copied ? 'Copied' : 'Copy link'}</span>
    </button>
  );
}
