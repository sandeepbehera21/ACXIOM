import { Link } from 'react-router-dom'
import { useAuth } from '../services/AuthContext'

export default function PublicNavbar({ activePage }) {
  const { user } = useAuth()

  return (
    <header className="axc-landing-nav">
      <div className="container d-flex justify-content-between align-items-center">
        <Link
          to="/"
          className="d-flex align-items-center gap-2 text-decoration-none"
        >
          <span className="axc-brand-mark" aria-hidden="true" />
          <span className="axc-brand-name">AcxiomCRM</span>
        </Link>

        <nav className="axc-landing-nav-links d-none d-md-flex">
          <Link to="/">Home</Link>
          <a href="/#features">Features</a>
          <a href="/#modules">Modules</a>
          <a href="/#demo">Demo Accounts</a>
          <a href="/#security">Security</a>
        </nav>

        <div className="d-flex align-items-center gap-3">
          {user ? (
            <Link to="/dashboard" className="btn btn-sm axc-btn-glow px-3 py-2">
              Open CRM Dashboard &rarr;
            </Link>
          ) : (
            <>
              {activePage !== 'login' && (
                <Link
                  to="/login"
                  className="btn btn-sm btn-outline-light px-3 py-2"
                  style={{ borderRadius: '8px', borderColor: 'rgba(255,255,255,0.2)' }}
                >
                  Sign in
                </Link>
              )}
              {activePage !== 'register' && (
                <Link
                  to="/register"
                  className="btn btn-sm axc-btn-glow px-3 py-2"
                  style={{ borderRadius: '8px' }}
                >
                  Create account
                </Link>
              )}
            </>
          )}
        </div>
      </div>
    </header>
  )
}
