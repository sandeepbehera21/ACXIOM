import { Navigate } from 'react-router-dom'
import { useAuth } from '../services/AuthContext'

/**
 * Role gate for a single route. This is UI convenience only — the backend
 * enforces the same rules.
 */
export default function RequireRole({ role, children }) {
  const { user } = useAuth()

  if (!user) return <Navigate to="/login" replace />
  const allowed = Array.isArray(role) ? role.includes(user.role) : user.role === role
  if (!allowed) return <Navigate to="/" replace />

  return children
}
