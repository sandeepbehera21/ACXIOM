import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../services/AuthContext'

/**
 * Gate for every authenticated route. While the session is being restored we
 * show a quiet loader; unauthenticated users go to /login.
 */
export default function ProtectedRoute() {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <div className="axc-fullpage-loading">
        <span className="axc-spinner" aria-hidden="true" />
        Loading AcxiomCRM…
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return <Outlet />
}
