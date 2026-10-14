/**
 * Dismissible alert used for form-level API messages.
 * `variant` maps to the theme's alert colours.
 */
export default function ErrorAlert({
  message,
  variant = 'danger',
  onDismiss,
  children,
}) {
  if (!message && !children) return null

  return (
    <div className={`axc-alert axc-alert--${variant}`} role="alert">
      <div className="axc-alert__body">
        {message && <div>{message}</div>}
        {children}
      </div>
      {onDismiss && (
        <button
          type="button"
          className="axc-alert__close"
          aria-label="Dismiss"
          onClick={onDismiss}
        >
          &times;
        </button>
      )}
    </div>
  )
}
