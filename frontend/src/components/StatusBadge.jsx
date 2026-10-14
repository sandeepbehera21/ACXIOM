import { formatEnumLabel } from '../utils/format'

/**
 * Colour tone per status / stage / priority / audit-action value.
 * Anything not listed falls back to a neutral grey badge.
 */
const TONES = {
  // Customer status
  PROSPECT: 'blue',
  ACTIVE: 'green',
  INACTIVE: 'grey',

  // Lead status
  NEW: 'blue',
  CONTACTED: 'indigo',
  QUALIFIED: 'teal',
  UNQUALIFIED: 'grey',
  CONVERTED: 'green',
  LOST: 'red',

  // Lead priority
  LOW: 'grey',
  MEDIUM: 'amber',
  HIGH: 'red',

  // Opportunity stage / status
  QUALIFICATION: 'blue',
  PROPOSAL: 'indigo',
  NEGOTIATION: 'amber',
  WON: 'green',
  OPEN: 'blue',

  // Audit result + action
  SUCCESS: 'green',
  FAILURE: 'red',
  LOGIN_SUCCESS: 'green',
  LOGIN_FAILURE: 'red',
  LOGOUT: 'grey',
  LOCKOUT: 'amber',
  UNLOCK: 'teal',
  CREATE: 'teal',
  UPDATE: 'blue',
  DELETE: 'red',
  CONVERT: 'indigo',
  ROLE_CHANGE: 'amber',
  SECURITY: 'amber',
}

/** Small coloured pill for a status-like enum value. */
export default function StatusBadge({ value, label }) {
  if (!value) return <span className="axc-cell-sub">—</span>
  const tone = TONES[value] || 'grey'
  return (
    <span className={`axc-badge axc-badge--${tone}`}>
      {label || formatEnumLabel(value)}
    </span>
  )
}
