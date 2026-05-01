import type { Brand } from '../types';
import { resolveTheme, type Theme } from '../lib/theme';

export const HELIOS: Brand = {
  id: 'helios',
  name: 'helios°',
  legalName: 'helios° energy',
  accent: '#f0c674',
  accentLight: '#b8862e',
  mark: 'helios',
  tagline: 'Precision energy intelligence for your solar array.'
};

const BRANDS: Record<string, Brand> = {
  helios: HELIOS,
  voltcraft: {
    id: 'voltcraft',
    name: 'Voltcraft',
    legalName: 'Voltcraft Solar, Inc.',
    accent: '#a78bfa',
    accentLight: '#6d28d9',
    mark: 'text',
    textMark: 'V',
    supportEmail: 'support@voltcraft.example',
    tagline: 'Your solar, refined.'
  },
  sunworks: {
    id: 'sunworks',
    name: 'SunWorks',
    legalName: 'SunWorks Energy Co.',
    accent: '#38bdf8',
    accentLight: '#0369a1',
    mark: 'text',
    textMark: 'S',
    supportEmail: 'help@sunworks.example',
    tagline: 'Powering your home, smarter.'
  },
  meridian: {
    id: 'meridian',
    name: 'Meridian',
    legalName: 'Meridian Renewables',
    accent: '#fb923c',
    accentLight: '#c2410c',
    mark: 'text',
    textMark: 'M',
    supportEmail: 'care@meridian.example',
    tagline: 'Solar, perfectly tuned.'
  }
};

export function resolveBrand(id: string | null | undefined): Brand {
  if (!id) return HELIOS;
  return BRANDS[id.toLowerCase()] ?? HELIOS;
}

export function brandFromSearch(search: string): Brand {
  const params = new URLSearchParams(search);
  const id = params.get('brand');
  return resolveBrand(id);
}

export function applyBrandAccent(brand: Brand, theme: Theme): void {
  const root = document.documentElement;
  if (brand.id === 'helios') {
    root.style.removeProperty('--signal-solar');
    return;
  }
  const resolved = resolveTheme(theme);
  root.style.setProperty('--signal-solar', resolved === 'light' ? brand.accentLight : brand.accent);
}

export function listBrands(): Brand[] {
  return Object.values(BRANDS);
}
