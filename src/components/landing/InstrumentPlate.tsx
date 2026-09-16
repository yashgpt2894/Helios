import { useBallistics } from '../../hooks/useBallistics';
import { SITE, formatHour, type FlowFrame } from '../../lib/placardModel';

interface Props {
  frame: FlowFrame;
  following: boolean;
}

function direction(word: string | null, kW: number) {
  if (!word) return { label: kW > 0.02 ? 'FLOW' : 'IDLE', value: Math.abs(kW) };
  return { label: word, value: Math.abs(kW) };
}

/**
 * The instrument window: what the reference system is doing at the selected hour. Figures are sprung
 * toward their new value (mass, damping, one small overshoot) the way a needle on a meter moves.
 */
export function InstrumentPlate({ frame, following }: Props) {
  const ac = useBallistics(frame.acPowerW / 1000);
  const dc = useBallistics(frame.dcPowerW / 1000);
  const home = useBallistics(frame.homeLoadW / 1000);
  const battery = useBallistics(frame.batteryPowerW / 1000);
  const grid = useBallistics((frame.gridExportW - frame.gridImportW) / 1000);

  const extent = Math.max(0, Math.min(1, ac / (SITE.ratedDcW / 1000)));
  const batteryState = direction(frame.batteryPowerW > 30 ? 'CHARGE' : frame.batteryPowerW < -30 ? 'DISCHARGE' : null, battery);
  const gridState = direction(frame.gridExportW > 30 ? 'EXPORT' : frame.gridImportW > 30 ? 'IMPORT' : null, grid);

  return (
    <div className="pc-plate">
      <div className="pc-plate__head">
        <span>NOW · {formatHour(frame.hour)}</span>
        <span>{following ? 'SIMULATED · 30 s' : 'SIMULATED · HELD'}</span>
      </div>

      <p className="pc-plate__state pc-plate__state">{frame.state}</p>
      <p className="pc-plate__sub">Inverter state · reference system</p>

      <div className="pc-readout">
        <span className="pc-readout__value">{ac.toFixed(2)}</span>
        <span className="pc-readout__unit">kW AC</span>
      </div>
      <p className="pc-plate__sub">Output right now</p>

      <div className="pc-scale">
        <div className="pc-scale__track" aria-hidden="true">
          <span className="pc-scale__ticks" />
          <span className="pc-scale__fill" style={{ width: `${extent * 100}%` }} />
          <span className="pc-scale__mark" style={{ left: `calc(${extent * 100}% - 1px)` }} />
        </div>
        <div className="pc-scale__legend" aria-hidden="true">
          <span>0</span>
          <span>array rating 9.6 kW</span>
          <span>9.6</span>
        </div>
      </div>

      <dl className="pc-grid4">
        <div>
          <dt>DC array</dt>
          <dd>
            {dc.toFixed(2)} <em>kW</em>
          </dd>
        </div>
        <div>
          <dt>House</dt>
          <dd>
            {home.toFixed(2)} <em>kW</em>
          </dd>
        </div>
        <div>
          <dt>Battery {Math.round(frame.batterySocPct)}%</dt>
          <dd>
            {batteryState.value.toFixed(2)} <em>{batteryState.label}</em>
          </dd>
        </div>
        <div>
          <dt>Utility</dt>
          <dd>
            {gridState.value.toFixed(2)} <em>{gridState.label}</em>
          </dd>
        </div>
      </dl>
    </div>
  );
}
