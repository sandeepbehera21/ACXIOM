import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div className="axc-notfound">
      <div className="axc-notfound__code">404</div>
      <h1 className="axc-page-title">Page not found</h1>
      <p className="axc-page-subtitle mb-3">
        The page you asked for does not exist or has been moved.
      </p>
      <Link className="btn btn-axc-primary btn-sm" to="/">
        Back to dashboard
      </Link>
    </div>
  )
}
