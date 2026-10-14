/**
 * Display helpers. Everything here works on real API values only — missing
 * values render as an em dash so an empty response never looks like data.
 */

export const EMPTY_PLACEHOLDER = '—'

export function formatEnumLabel(value) {
  if (!value) return ''
  return String(value)
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

const currencyFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
})

export function formatCurrency(value) {
  if (value === null || value === undefined || value === '') {
    return EMPTY_PLACEHOLDER
  }
  const parsed = Number(value)
  if (Number.isNaN(parsed)) return EMPTY_PLACEHOLDER
  return currencyFormatter.format(parsed)
}

export function formatNumber(value) {
  if (value === null || value === undefined || value === '') {
    return EMPTY_PLACEHOLDER
  }
  const parsed = Number(value)
  if (Number.isNaN(parsed)) return EMPTY_PLACEHOLDER
  return new Intl.NumberFormat('en-IN').format(parsed)
}

export function formatDate(value) {
  if (!value) return EMPTY_PLACEHOLDER
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return EMPTY_PLACEHOLDER
  return date.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

export function formatDateTime(value) {
  if (!value) return EMPTY_PLACEHOLDER
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return EMPTY_PLACEHOLDER
  return date.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/** Trims a text value to a safe payload value ('' -> null). */
export function orNull(value) {
  if (value === null || value === undefined) return null
  const trimmed = String(value).trim()
  return trimmed === '' ? null : trimmed
}

/** Today as yyyy-MM-dd, used as the min for date inputs. */
export function todayIso() {
  const now = new Date()
  const offset = now.getTimezoneOffset()
  return new Date(now.getTime() - offset * 60000).toISOString().slice(0, 10)
}
