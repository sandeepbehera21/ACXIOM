import { useCallback, useRef, useState } from 'react'
import { validateAll, validateField } from '../validation/validators'

/**
 * Small form state helper shared by every form in the app.
 *
 * - errors surface on blur and on submit
 * - a field that is already in error re-validates as the user types
 * - `validateForm()` returns false while any error exists, which blocks submit
 * - server `fieldErrors` merge straight into the error map
 */
export function useForm(initialValues, validators = {}) {
  const initialRef = useRef(initialValues)
  const [values, setValues] = useState(initialValues)
  const [errors, setErrors] = useState({})
  const [touched, setTouched] = useState({})
  const [formError, setFormError] = useState('')

  const valuesRef = useRef(values)
  const touchedRef = useRef(touched)

  /** Bulk replacement that keeps the internal ref in step with state. */
  const setValuesSafe = useCallback((next) => {
    valuesRef.current = next
    setValues(next)
  }, [])

  const setValue = useCallback(
    (name, value) => {
      const next = { ...valuesRef.current, [name]: value }
      valuesRef.current = next
      setValues(next)
      if (touchedRef.current[name] && validators[name]) {
        setErrors((prev) => ({
          ...prev,
          [name]: validators[name](value, next),
        }))
      }
    },
    [validators],
  )

  const handleChange = useCallback(
    (event) => {
      setValue(event.target.name, event.target.value)
    },
    [setValue],
  )

  const handleBlur = useCallback(
    (event) => {
      const { name } = event.target
      if (!name) return
      if (!touchedRef.current[name]) {
        const nextTouched = { ...touchedRef.current, [name]: true }
        touchedRef.current = nextTouched
        setTouched(nextTouched)
      }
      setErrors((prev) => ({
        ...prev,
        [name]: validateField(
          name,
          valuesRef.current[name],
          valuesRef.current,
          validators,
        ),
      }))
    },
    [validators],
  )

  /** Validates everything, marks all fields touched, returns true when clean. */
  const validateForm = useCallback(() => {
    const all = validateAll(valuesRef.current, validators)
    setErrors(all)
    const allTouched = {}
    Object.keys(validators).forEach((key) => {
      allTouched[key] = true
    })
    touchedRef.current = allTouched
    setTouched(allTouched)
    return Object.keys(all).length === 0
  }, [validators])

  /** Replaces the whole value set (used when a record finishes loading). */
  const reset = useCallback((nextValues, options = {}) => {
    const resolved = nextValues || initialRef.current
    valuesRef.current = resolved
    touchedRef.current = {}
    setValues(resolved)
    setTouched({})
    setErrors(options.errors || {})
    setFormError(options.formError || '')
  }, [])

  /**
   * Maps the API's `fieldErrors` onto the matching inputs. Errors that do not
   * correspond to a rendered field are appended to the form-level message so
   * nothing the server reported is silently swallowed.
   */
  const applyServerErrors = useCallback(
    (fieldErrors = {}, message = '') => {
      const entries = Object.entries(fieldErrors || {})
      const matched = {}
      const unmatched = []

      entries.forEach(([field, fieldMessage]) => {
        if (validators[field]) matched[field] = fieldMessage
        else unmatched.push(`${field}: ${fieldMessage}`)
      })

      const keys = Object.keys(matched)
      if (keys.length) {
        setErrors((prev) => ({ ...prev, ...matched }))
        const nextTouched = { ...touchedRef.current }
        keys.forEach((key) => {
          nextTouched[key] = true
        })
        touchedRef.current = nextTouched
        setTouched(nextTouched)
      }

      const combined = [message, ...unmatched].filter(Boolean).join(' ')
      if (combined) setFormError(combined)
    },
    [validators],
  )

  const clearFieldError = useCallback((name) => {
    setErrors((prev) => {
      if (!prev[name]) return prev
      const next = { ...prev }
      delete next[name]
      return next
    })
  }, [])

  return {
    values,
    errors,
    touched,
    formError,
    setFormError,
    setValue,
    setValues: setValuesSafe,
    handleChange,
    handleBlur,
    validateForm,
    reset,
    applyServerErrors,
    clearFieldError,
  }
}

export default useForm
