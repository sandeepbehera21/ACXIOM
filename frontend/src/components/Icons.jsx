/**
 * Minimal stroke icons for the sidebar. Inline SVG only — never emoji.
 */
const base = {
  width: 16,
  height: 16,
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 1.8,
  strokeLinecap: 'round',
  strokeLinejoin: 'round',
  'aria-hidden': true,
}

export function DashboardIcon() {
  return (
    <svg {...base}>
      <rect x="3" y="3" width="7" height="8" rx="1.5" />
      <rect x="14" y="3" width="7" height="5" rx="1.5" />
      <rect x="14" y="11" width="7" height="10" rx="1.5" />
      <rect x="3" y="14" width="7" height="7" rx="1.5" />
    </svg>
  )
}

export function CustomersIcon() {
  return (
    <svg {...base}>
      <circle cx="9" cy="8" r="3.2" />
      <path d="M3.5 20a5.5 5.5 0 0 1 11 0" />
      <path d="M16.5 5.6a3 3 0 0 1 0 5.6" />
      <path d="M18 20a5.4 5.4 0 0 0-1.6-3.8" />
    </svg>
  )
}

export function LeadsIcon() {
  return (
    <svg {...base}>
      <path d="M3 5h18l-7 8v6l-4 2v-8z" />
    </svg>
  )
}

export function OpportunitiesIcon() {
  return (
    <svg {...base}>
      <path d="M3 20V10" />
      <path d="M9 20V4" />
      <path d="M15 20v-7" />
      <path d="M21 20V8" />
    </svg>
  )
}

export function AuditIcon() {
  return (
    <svg {...base}>
      <path d="M6 3h9l4 4v14a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z" />
      <path d="M14 3v5h5" />
      <path d="M9 13h6" />
      <path d="M9 17h4" />
    </svg>
  )
}

export function SearchIcon() {
  return (
    <svg {...base} width="14" height="14">
      <circle cx="11" cy="11" r="7" />
      <path d="m20 20-3.5-3.5" />
    </svg>
  )
}

export function MenuIcon() {
  return (
    <svg {...base} width="18" height="18">
      <path d="M4 6h16" />
      <path d="M4 12h16" />
      <path d="M4 18h16" />
    </svg>
  )
}

export function FollowUpsIcon() {
  return (
    <svg {...base}>
      <circle cx="12" cy="12" r="9" />
      <polyline points="12 6 12 12 16 14" />
    </svg>
  )
}

export function ActivitiesIcon() {
  return (
    <svg {...base}>
      <path d="M9 11l3 3L22 4" />
      <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
    </svg>
  )
}

export function UsersIcon() {
  return (
    <svg {...base}>
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </svg>
  )
}

