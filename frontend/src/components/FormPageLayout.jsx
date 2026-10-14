import { useNavigate } from 'react-router-dom'
import ErrorAlert from './ErrorAlert'
import PageHeader from './PageHeader'

/**
 * Shared wrapper for the create/edit form pages: header with a back action,
 * a load-error alert and the card that holds the form.
 */
export default function FormPageLayout({
  title,
  subtitle,
  backTo,
  loadError,
  onDismissLoadError,
  children,
}) {
  const navigate = useNavigate()

  return (
    <div>
      <PageHeader
        title={title}
        subtitle={subtitle}
        action={
          <button
            type="button"
            className="btn btn-outline-axc btn-sm"
            onClick={() => navigate(backTo)}
          >
            Back to list
          </button>
        }
      />

      {loadError && (
        <div className="mb-3">
          <ErrorAlert message={loadError} onDismiss={onDismissLoadError} />
        </div>
      )}

      <div className="axc-card">
        <div className="axc-card__body">{children}</div>
      </div>
    </div>
  )
}
