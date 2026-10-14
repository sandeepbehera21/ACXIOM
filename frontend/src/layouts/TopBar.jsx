import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { MenuIcon } from '../components/Icons'
import { useAuth } from '../services/AuthContext'
import { formatEnumLabel } from '../utils/format'

function initials(name) {
  if (!name) return '?'
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('')
}

export default function TopBar({ user, onToggleSidebar }) {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const [signingOut, setSigningOut] = useState(false)

  async function handleSignOut() {
    setSigningOut(true)
    try {
      await logout()
    } finally {
      setSigningOut(false)
      navigate('/login', { replace: true })
    }
  }

  return (
    <header className="axc-topbar">
      <button
        type="button"
        className="axc-topbar__hamburger"
        aria-label="Toggle navigation"
        onClick={onToggleSidebar}
      >
        <MenuIcon />
      </button>

      <span className="axc-user-chip d-none d-md-flex">
        <span className="axc-user-avatar" aria-hidden="true">
          {initials(user?.name)}
        </span>
        <span>
          <span className="axc-user-name d-block">{user?.name}</span>
          <span className="axc-user-role d-block">
            {user?.roleDisplay || formatEnumLabel(user?.role)}
          </span>
        </span>
      </span>

      <span className="axc-topbar__spacer" />

      <button
        type="button"
        className="btn btn-sm btn-outline-axc"
        onClick={handleSignOut}
        disabled={signingOut}
      >
        {signingOut ? 'Signing out…' : 'Sign out'}
      </button>
    </header>
  )
}
