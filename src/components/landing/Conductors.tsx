import { useBallistics } from '../../hooks/useBallistics';
import { FlowArrow } from './Pictograms';
import { SITE, type FlowFrame } from '../../lib/placardModel';

interface Props {
  frame: FlowFrame;
}

/**
 * The one-line diagram, printed the way a label-set prints it: nodes stacked on a single conductor
 * rail, every path named with a number, a unit and a direction word so nothing depends on colour.
 */
export function Conductors({ frame }: Props) {
  const dc = useBallistics(frame.dcPowerW / 1000);
  const ac = useBallistics(frame.acPowerW / 1000);
  const home = useBallistics(frame.homeLoadW / 1000);
  const battery = useBallistics(Math.abs(frame.batteryPowerW) / 1000);
  const grid = useBallistics((frame.gridExportW + frame.gridImportW) / 1000);

  const batteryWord = frame.batteryPowerW > 30 ? 'CHARGING' : frame.batteryPowerW < -30 ? 'DISCHARGING' : 'IDLE';
  const gridWord = frame.gridExportW > 30 ? 'EXPORTING' : frame.gridImportW > 30 ? 'IMPORTING' : 'IDLE';

  return (
    <div>
      <ol className="pc-line__rows">
        <li className="pc-node pc-node--source">
          <span className="pc-node__id">PV array</span>
          <span className="pc-node__spec">24 modules · 3 strings · {SITE.ratedDcW / 1000} kW DC</span>
          <span className="pc-values" style={{ marginLeft: 'auto' }}>
            <span className="pc-values__item">
              <span className="pc-val">{dc.toFixed(2)}</span>
              <span className="pc-val--unit">kW</span>
            </span>
            <span className="pc-values__item">
              <span className="pc-val pc-val--s">{Math.round(frame.irradianceWm2)}</span>
              <span className="pc-val--unit">W/m²</span>
            </span>
          </span>
        </li>

        <li className="pc-linkrow">
          <span className="pc-linkrow__rail" aria-hidden="true">
            <i />
            <FlowArrow direction="down" />
            <i />
          </span>
          <span className="pc-linkrow__meta">
            <span className="pc-chip pc-chip--solar">Solar</span>
            <span className="pc-val pc-val--s">DC {dc.toFixed(2)} kW</span>
            <span className="pc-val--unit">
              {frame.strings.filter((s) => s.powerW > 30).length} strings carrying
            </span>
          </span>
        </li>

        <li className="pc-node pc-node--device">
          <span className="pc-node__id">HX-9.6 hybrid</span>
          <span className="pc-node__spec">
            {SITE.conversion} DC to AC · fw {SITE.firmware} · {SITE.model}
          </span>
          <span className="pc-values" style={{ marginLeft: 'auto' }}>
            <span className="pc-values__item">
              <span className="pc-val">{ac.toFixed(2)}</span>
              <span className="pc-val--unit">kW AC</span>
            </span>
            <span className="pc-values__item">
              <span className="pc-val pc-val--s">{frame.heatsinkTempC.toFixed(1)}</span>
              <span className="pc-val--unit">°C heatsink</span>
            </span>
          </span>
        </li>

        <li className="pc-linkrow">
          <span className="pc-linkrow__rail" aria-hidden="true">
            <i />
            <FlowArrow direction="down" />
            <i />
          </span>
          <span className="pc-linkrow__meta">
            <span className="pc-chip pc-chip--flow">House</span>
            <span className="pc-val pc-val--s">AC {ac.toFixed(2)} kW</span>
            <span className="pc-val--unit">240 V · 60 Hz</span>
          </span>
        </li>

        <li className="pc-node">
          <span className="pc-node__id">House board</span>
          <span className="pc-node__spec">Whole-home load</span>
          <span className="pc-values" style={{ marginLeft: 'auto' }}>
            <span className="pc-values__item">
              <span className="pc-val">{home.toFixed(2)}</span>
              <span className="pc-val--unit">kW drawing</span>
            </span>
          </span>
        </li>

        <li className="pc-linkrow pc-linkrow--branch">
          <span className="pc-linkrow__rail pc-linkrow__rail--elbow" aria-hidden="true">
            <span className="pc-linkrow__elbow" />
          </span>
          <span className="pc-linkrow__meta">
            <span className="pc-chip pc-chip--battery">Battery</span>
            <span className="pc-val">{battery.toFixed(2)}</span>
            <span className="pc-val--unit">kW</span>
            <span className="pc-val pc-val--s">{batteryWord}</span>
            <span className="pc-val--unit">
              {Math.round(frame.batterySocPct)}% of {SITE.batteryKwh} kWh
            </span>
          </span>
        </li>

        <li className="pc-linkrow pc-linkrow--branch">
          <span className="pc-linkrow__rail pc-linkrow__rail--elbow" aria-hidden="true">
            <span className="pc-linkrow__elbow pc-linkrow__elbow--cap" />
          </span>
          <span className="pc-linkrow__meta">
            <span className="pc-chip pc-chip--grid">Utility</span>
            <span className="pc-val">{grid.toFixed(2)}</span>
            <span className="pc-val--unit">kW</span>
            <span className="pc-val pc-val--s">{gridWord}</span>
          </span>
        </li>
      </ol>

      <h3 className="pc-tag pc-tag--strong" style={{ margin: '26px 0 0' }}>
        Per-string output · simulated, read at the selected hour
      </h3>
      <div className="pc-strings">
        {frame.strings.map((string) => (
          <div className="pc-string" key={string.id}>
            <span className="pc-string__id">{string.id}</span>
            <span className="pc-string__name">{string.label}</span>
            <span className="pc-string__num">{(string.powerW / 1000).toFixed(2)} kW</span>
            <span className="pc-string__num">{string.voltageV.toFixed(0)} V</span>
            <span className="pc-string__num">{string.currentA.toFixed(2)} A</span>
            <span className="pc-string__bar" aria-hidden="true">
              <i style={{ width: `${Math.round(string.shareOfPeers * 100)}%` }} />
            </span>
          </div>
        ))}
      </div>
      <p className="pc-note" style={{ marginTop: '12px' }}>
        Imbalance above 15% raises an inspection notice. The garage string leads at dawn and trails at
        dusk, because it faces a different way — the shape of a real roof.
      </p>
    </div>
  );
}
