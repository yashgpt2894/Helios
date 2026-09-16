import { HazardBoltPictogram } from './Pictograms';
import type { FlowFrame, Notice } from '../../lib/placardModel';
import { formatHour } from '../../lib/placardModel';

interface Props {
  frame: FlowFrame;
  notices: Notice[];
}

const WORD_CLASS: Record<Notice['word'], string> = {
  DANGER: 'pc-notice__word--danger',
  WARNING: 'pc-notice__word--warning',
  CAUTION: 'pc-notice__word--caution',
  NOTICE: 'pc-notice__word--notice',
  GUIDE: 'pc-notice__word--guide'
};

/**
 * The product's output, on the sheet's own terms: a signal word, a message, a metric and one action.
 * The one DANGER field is the one hazard that is always true of a photovoltaic system.
 */
export function NoticeSet({ frame, notices }: Props) {
  return (
    <div>
      <div className="pc-danger">
        <p className="pc-danger__word">Danger</p>
        <div>
          <p className="pc-danger__body">
            <strong>Array DC conductors stay live whenever the modules see light</strong> — including
            when the AC disconnect is off, and including when this house is dark. That is why a
            photovoltaic system is marked at all.
          </p>
          <p className="pc-danger__note" style={{ marginTop: '12px' }}>
            {formatHour(frame.hour)} · marked supply: photovoltaic · second supply: utility
          </p>
        </div>
        <HazardBoltPictogram className="pc-danger__pict" title="Electrical hazard mark" />
      </div>

      <ul className="pc-notices" style={{ listStyle: 'none', margin: 0, padding: 0 }}>
        {notices.map((notice) => (
          <li className="pc-notice" key={notice.id}>
            <span className={`pc-notice__word ${WORD_CLASS[notice.word]}`}>{notice.word}</span>
            <span>
              <h3 className="pc-notice__title">{notice.title}</h3>
              <p className="pc-notice__body">{notice.body}</p>
            </span>
            <span className="pc-notice__metric">
              <span className="pc-notice__value">
                {notice.metric}
                <span>{notice.unit}</span>
              </span>
              <p className="pc-notice__delta">{notice.delta}</p>
              <a className="pc-notice__action" href="/app">
                {notice.action}
              </a>
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}
