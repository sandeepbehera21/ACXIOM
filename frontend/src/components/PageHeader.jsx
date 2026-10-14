/** Page title row with an optional action on the right. */
export default function PageHeader({ title, subtitle, action }) {
  return (
    <div className="d-flex align-items-start justify-content-between gap-3 mb-3 flex-wrap">
      <div>
        <h1 className="axc-page-title">{title}</h1>
        {subtitle && <p className="axc-page-subtitle">{subtitle}</p>}
      </div>
      {action && <div className="d-flex gap-2">{action}</div>}
    </div>
  )
}
