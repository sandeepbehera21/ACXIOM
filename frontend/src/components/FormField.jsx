/**
 * Label + control + inline error, used by every form in the app.
 *
 * Renders an `<input>`, `<select>` or `<textarea>` depending on `as`.
 * The error message is rendered underneath the control in red.
 */
export default function FormField({
  label,
  name,
  error,
  help,
  required = false,
  as = 'input',
  options = [],
  rows = 3,
  wrapperClassName = '',
  controlClassName = '',
  children,
  ...controlProps
}) {
  const invalid = Boolean(error)
  const describedBy = [help ? `${name}-help` : null, invalid ? `${name}-error` : null]
    .filter(Boolean)
    .join(' ')

  const classes = [`form-control`, invalid ? 'is-invalid' : '', controlClassName]
    .filter(Boolean)
    .join(' ')

  const selectClasses = [`form-select`, invalid ? 'is-invalid' : '', controlClassName]
    .filter(Boolean)
    .join(' ')

  const common = {
    id: name,
    name,
    'aria-describedby': describedBy || undefined,
    'aria-invalid': invalid || undefined,
    ...controlProps,
  }

  return (
    <div className={wrapperClassName}>
      <label className="form-label" htmlFor={name}>
        {label}
        {required && <span className="axc-required">*</span>}
      </label>

      {as === 'select' ? (
        <select className={selectClasses} {...common}>
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      ) : as === 'textarea' ? (
        <textarea className={classes} rows={rows} {...common} />
      ) : children ? (
        children
      ) : (
        <input className={classes} {...common} />
      )}

      {help && (
        <div className="axc-help" id={`${name}-help`}>
          {help}
        </div>
      )}
      {invalid && (
        <div className="axc-field-error" id={`${name}-error`}>
          {error}
        </div>
      )}
    </div>
  )
}
