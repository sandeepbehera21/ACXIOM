/**
 * Reusable client-side validators.
 *
 * Every function returns an error string when the value is invalid, or `null`
 * when it is acceptable. The rules deliberately mirror the server-side
 * constraints so the user sees the same message before and after a round trip.
 *
 * Cross-field validators receive the whole values object as a second argument.
 */

export const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/
export const PHONE_PATTERN = /^[6-9]\d{9}$/

export const MAX_LENGTH = {
  name: 150,
  email: 150,
  company: 150,
  address: 255,
  city: 100,
  state: 100,
  notes: 1000,
  source: 50,
  password: 128,
}

const isEmpty = (value) =>
  value === undefined || value === null || String(value).trim() === ''

/** Generic required check. */
export function required(value, label = 'This field') {
  if (isEmpty(value)) return `${label} is required.`
  return null
}

/** Max-length check; blank values pass (combine with `required` if needed). */
export function maxLength(value, limit, label = 'This field') {
  if (isEmpty(value)) return null
  if (String(value).length > limit) {
    return `${label} must not exceed ${limit} characters.`
  }
  return null
}

/** Email: required plus format. */
export function email(value, label = 'Email') {
  const missing = required(value, label)
  if (missing) return missing
  const trimmed = String(value).trim()
  if (trimmed.length > MAX_LENGTH.email) {
    return `${label} must not exceed ${MAX_LENGTH.email} characters.`
  }
  if (!EMAIL_PATTERN.test(trimmed)) return 'Enter a valid email address.'
  return null
}

/** Indian mobile number: 10 digits starting 6-9. */
export function phone(value, label = 'Phone') {
  const missing = required(value, label)
  if (missing) return missing
  const trimmed = String(value).trim()
  if (!PHONE_PATTERN.test(trimmed)) {
    return 'Enter a valid 10-digit mobile number.'
  }
  return null
}

/** Optional phone: blank is fine, but a supplied value must be valid. */
export function optionalPhone(value) {
  if (isEmpty(value)) return null
  const trimmed = String(value).trim()
  if (!PHONE_PATTERN.test(trimmed)) {
    return 'Enter a valid 10-digit mobile number.'
  }
  return null
}

/** Opportunity amount: numeric and strictly greater than zero. */
export function amount(value, label = 'Amount') {
  const missing = required(value, label)
  if (missing) return `${label} is required.`
  const parsed = Number(value)
  if (Number.isNaN(parsed)) return 'Amount must be a number.'
  if (parsed <= 0) return 'Amount must be greater than 0.'
  return null
}

/** Opportunity probability: whole number between 0 and 100. */
export function probability(value, label = 'Probability') {
  const missing = required(value, label)
  if (missing) return `${label} is required.`
  const parsed = Number(value)
  if (Number.isNaN(parsed) || !Number.isInteger(parsed)) {
    return 'Probability must be a whole number.'
  }
  if (parsed < 0 || parsed > 100) {
    return 'Probability must be between 0 and 100.'
  }
  return null
}

/**
 * Expected close date. Past dates are rejected while the opportunity is still
 * open (a stage that is not WON or LOST).
 */
export function expectedCloseDate(value, stage) {
  const missing = required(value, 'Expected close date')
  if (missing) return 'Expected close date is required.'
  const isClosedStage = stage === 'WON' || stage === 'LOST'
  if (isClosedStage) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const picked = new Date(`${value}T00:00:00`)
  if (Number.isNaN(picked.getTime())) return 'Enter a valid date.'
  if (picked < today) return 'Expected close date cannot be in the past.'
  return null
}

/** Password policy: 8+ chars with upper, lower, digit and special character. */
export function password(value, label = 'Password') {
  const missing = required(value, label)
  if (missing) return `${label} is required.`
  const raw = String(value)
  if (raw.length < 8) return 'Password must be at least 8 characters long.'
  if (raw.length > MAX_LENGTH.password) {
    return `Password must not exceed ${MAX_LENGTH.password} characters.`
  }
  if (!/[A-Z]/.test(raw)) {
    return 'Password must contain at least one uppercase letter.'
  }
  if (!/[a-z]/.test(raw)) {
    return 'Password must contain at least one lowercase letter.'
  }
  if (!/\d/.test(raw)) return 'Password must contain at least one digit.'
  if (!/[^A-Za-z0-9]/.test(raw)) {
    return 'Password must contain at least one special character.'
  }
  return null
}

/** Confirm-password match. */
export function confirmPassword(value, values = {}) {
  const missing = required(value, 'Confirm password')
  if (missing) return 'Please confirm your password.'
  if (value !== values.password) return 'Passwords do not match.'
  return null
}

/** Sign-in password: only presence is checked (the server owns the policy). */
export function signInPassword(value) {
  const missing = required(value, 'Password')
  if (missing) return 'Password is required.'
  return null
}

/**
 * Validates a single field using a map of `{ field: (value, values) => msg }`.
 * Returns the first error message or null.
 */
export function validateField(name, value, values, validators) {
  const validator = validators[name]
  if (!validator) return null
  return validator(value, values)
}

/**
 * Validates every field in the map. Returns an object of
 * `{ field: message }` for the fields that failed.
 */
export function validateAll(values, validators) {
  const errors = {}
  Object.keys(validators).forEach((name) => {
    const message = validateField(name, values[name], values, validators)
    if (message) errors[name] = message
  })
  return errors
}
