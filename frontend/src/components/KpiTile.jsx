import { EMPTY_PLACEHOLDER } from '../utils/format'

/**
 * Dashboard metric tile. Renders "—" until a real value arrives, so a loading
 * dashboard never shows invented numbers.
 */
export default function KpiTile({ label, value, hint }) {
  const display =
    value === null || value === undefined || value === ''
      ? EMPTY_PLACEHOLDER
      : value

  return (
    <div className="axc-kpi">
      <div className="axc-kpi__label">{label}</div>
      <div className="axc-kpi__value">{display}</div>
      {hint && <div className="axc-kpi__hint">{hint}</div>}
    </div>
  )
}
