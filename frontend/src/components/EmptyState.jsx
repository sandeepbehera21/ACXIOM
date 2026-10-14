/** Empty-table placeholder. No invented rows — just the reason for the gap. */
export default function EmptyState({ title = 'Nothing to show', message }) {
  return (
    <div className="axc-empty">
      <div className="axc-empty__title">{title}</div>
      {message && <div>{message}</div>}
    </div>
  )
}
