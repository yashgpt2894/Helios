# Share

```ts
import type { Brand, SnapshotPayload, SolarTelemetry } from '../types';

export function encodeSnapshot(payload: SnapshotPayload): string {
  const json = JSON.stringify(payload);
  const b64 = btoa(unescape(encodeURIComponent(json)));
  return b64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

export function decodeSnapshot(encoded: string): SnapshotPayload | null {
  try {
    const padded = encoded.replace(/-/g, '+').replace(/_/g, '/') + '==='.slice((encoded.length + 3) % 4);
    const json = decodeURIComponent(escape(atob(padded)));
    const parsed = JSON.parse(json);
    if (parsed && parsed.v === 1) return parsed as SnapshotPayload;
    return null;
  } catch {
    return null;
  }
}

export function buildSnapshot(t: SolarTelemetry, locLabel: string, brandId: string, fc?: number[]): SnapshotPayload {
  const selfUse =
    t.acPowerW > 0 ? Math.max(0, Math.min(100, (1 - t.gridExportW / Math.max(1, t.acPowerW)) * 100)) : 0;
  return {
    v: 1,
    ts: t.timestamp,
    loc: locLabel,
    ac: Number((t.acPowerW / 1000).toFixed(2)),
    todayKwh: Number(t.energyTodayKwh.toFixed(1)),
    lifeKwh: Math.round(t.energyLifetimeKwh),
    soc: Math.round(t.batterySoc),
    selfUse: Math.round(selfUse),
    fc: fc?.map((k) => Math.round(k)),
    br: brandId === 'helios' ? undefined : brandId
  };
}

export function buildShareUrl(snapshot: SnapshotPayload, baseUrl?: string): string {
  const origin = baseUrl ?? (typeof window !== 'undefined' ? window.location.origin : '');
  return `${origin}/share/${encodeSnapshot(snapshot)}`;
}

export async function shareOrCopy(url: string, brand: Brand): Promise<'shared' | 'copied' | 'failed'> {
  if (typeof navigator !== 'undefined' && 'share' in navigator) {
    try {
      await (navigator as Navigator & { share: (data: ShareData) => Promise<void> }).share({
        title: `${brand.name} · my solar`,
        text: 'Live snapshot from my solar array',
        url
      });
      return 'shared';
    } catch {
      /* fall through to clipboard */
    }
  }
  try {
    await navigator.clipboard.writeText(url);
    return 'copied';
  } catch {
    return 'failed';
  }
}

```