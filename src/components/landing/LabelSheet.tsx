import { listBrands } from '../../services/brand';

const TELEMETRY_SHAPE = [
  { field: 'acPowerW', type: 'number', unit: 'W' },
  { field: 'batterySoc', type: 'number', unit: '%' },
  { field: 'panels[3]', type: 'powerW · voltageV · currentA', unit: '' }
];

const BRAND_SWATCHES = [
  { id: 'voltcraft', monogram: 'V' },
  { id: 'sunworks', monogram: 'S' },
  { id: 'meridian', monogram: 'M' }
];

/**
 * The sibling labels of a roof installation: what a placard does not say. Every claim here is
 * checkable in the repository, and where a mechanism is narrower than the marketing usually is, the
 * label says so.
 */
export function LabelSheet({ light }: { light: boolean }) {
  const brands = listBrands();

  return (
    <div className="pc-labels">
      <article className="pc-label pc-label--l01">
        <div className="pc-label__top">
          <span className="pc-label__id">L-01</span>
          <span className="pc-label__role">SunSpec Modbus TCP</span>
        </div>
        <h3 className="pc-label__title">It is built to read the inverter, not a cloud</h3>
        <p className="pc-label__body">
          An inverter on a home network speaks <strong>SunSpec Modbus TCP</strong>, the interface SMA, Fronius,
          SolarEdge, Enphase and Schneider already ship. A browser cannot open a raw TCP socket,
          so a live system needs one small thing this page will not pretend away: a gateway on the same
          network (a Raspberry Pi running a small relay is enough) that hands the app its registers.
          Until you have one, the app reads the simulator, and every figure on this sheet says so.
        </p>
        <div className="pc-label__artifact">
          <div className="pc-shape">
            {TELEMETRY_SHAPE.map((row) => (
              <span key={row.field}>
                <b>{row.field}</b> · {row.type} {row.unit ? `· ${row.unit}` : ''}
              </span>
            ))}
          </div>
        </div>
      </article>

      <article className="pc-label pc-label--l02">
        <div className="pc-label__top">
          <span className="pc-label__id">L-02</span>
          <span className="pc-label__role">Open-Meteo · no key</span>
        </div>
        <h3 className="pc-label__title">Tomorrow, from radiation</h3>
        <p className="pc-label__body">
          Shortwave radiation from Open-Meteo, refetched while the app is open. No key and no sign-up. It
          starts at one fixed default location, San Francisco{' '}
          <span className="pc-mono">37.77, -122.42</span>, and uses yours once you tap{' '}
          <span className="pc-mono">use my location</span>.
        </p>
        <div className="pc-label__artifact">
          <span className="pc-formula">EXPECTED kWh = PEAK SUN HOURS × 9.6 kW × 0.82</span>
        </div>
      </article>

      <article className="pc-label pc-label--l03">
        <div className="pc-label__top">
          <span className="pc-label__id">L-03</span>
          <span className="pc-label__role">No account · no database</span>
        </div>
        <h3 className="pc-label__title">Nothing to sync</h3>
        <p className="pc-label__body">
          Readings live in the browser. There is no account to create and no place they are sent.
        </p>
        <div className="pc-label__artifact">
          <ul className="pc-absence">
            <li>
              <i aria-hidden="true" />
              No account
            </li>
            <li>
              <i aria-hidden="true" />
              No database
            </li>
            <li>
              <i aria-hidden="true" />
              No analytics
            </li>
            <li>
              <i aria-hidden="true" />
              No server to breach
            </li>
          </ul>
        </div>
      </article>

      <article className="pc-label pc-label--l04">
        <div className="pc-label__top">
          <span className="pc-label__id">L-04</span>
          <span className="pc-label__role">base64url · client-side</span>
        </div>
        <h3 className="pc-label__title">A reading fits in a link</h3>
        <p className="pc-label__body">
          Today&apos;s snapshot is encoded into the URL itself and decodes entirely on the device that
          opens it. Send it to whoever installed the array.
        </p>
        <div className="pc-label__artifact">
          <span className="pc-formula">/share/eyJhY1Bvd2VyVyI6NT…</span>
        </div>
      </article>

      <article className="pc-label pc-label--l05">
        <div className="pc-label__top">
          <span className="pc-label__id">L-05</span>
          <span className="pc-label__role">?brand= · four brands ship</span>
        </div>
        <h3 className="pc-label__title">Your name travels with it</h3>
        <p className="pc-label__body">
          <span className="pc-mono">?brand=</span> swaps the accent and the monogram, and the brand
          survives into shared snapshots, so the customer sees the name of the firm that put the panels
          up. Three example brands ship in the repository.
        </p>
        <div className="pc-label__artifact">
          <div className="pc-brands">
            {BRAND_SWATCHES.map((swatch) => {
              const brand = brands.find((item) => item.id === swatch.id);
              return (
                <span className="pc-brand" key={swatch.id}>
                  <span
                    className="pc-brand__mark"
                    style={{
                      background: (light ? brand?.accentLight : brand?.accent) ?? 'transparent',
                      color: light ? '#ffffff' : 'var(--pc-mark-ink)'
                    }}
                    aria-hidden="true"
                  >
                    {swatch.monogram}
                  </span>
                  {brand?.name ?? swatch.id}
                </span>
              );
            })}
          </div>
        </div>
      </article>

      <article className="pc-label pc-label--l06">
        <div className="pc-label__top">
          <span className="pc-label__id">L-06</span>
          <span className="pc-label__role">PWA · service worker</span>
        </div>
        <h3 className="pc-label__title">Installs like an app, needs nothing like a server</h3>
        <p className="pc-label__body">
          One install prompt on the phone, one icon on the home screen. The shell and the last reading
          survive without a network; the forecast waits until you are back on one.
        </p>
        <div className="pc-label__artifact">
          <div className="pc-shape">
            <span>
              <b>manifest</b> · standalone · maskable icons
            </span>
            <span>
              <b>service worker</b> · precached shell, offline after first load
            </span>
            <span>
              <b>theme</b> · carbon, paper, or follow the system
            </span>
            <span>
              <b>app type</b> · Google Fonts, runtime-cached by the service worker
            </span>
          </div>
        </div>
      </article>
    </div>
  );
}
