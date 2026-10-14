/** Inline loading state for tables and cards. */
export default function LoadingState({ label = 'Loading…' }) {
  return (
    <div className="axc-loading">
      <span className="axc-spinner" aria-hidden="true" />
      {label}
    </div>
  )
}
