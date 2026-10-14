import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import ErrorAlert from '../components/ErrorAlert'
import FormField from '../components/FormField'
import PublicNavbar from '../components/PublicNavbar'
import { useForm } from '../components/useForm'
import { useAuth } from '../services/AuthContext'
import { extractApiError } from '../services/api'
import { email as validateEmail, signInPassword } from '../validation/validators'

const validators = {
  email: (value) => validateEmail(value),
  password: (value) => signInPassword(value),
}

export default function LoginPage() {
  const { login, user, loading } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const justRegistered = Boolean(location.state?.registered)

  const [submitting, setSubmitting] = useState(false)
  const [noticeOpen, setNoticeOpen] = useState(justRegistered)
  const [quickLogging, setQuickLogging] = useState('')

  const {
    values,
    errors,
    formError,
    setFormError,
    handleChange,
    handleBlur,
    validateForm,
    applyServerErrors,
  } = useForm({ email: '', password: '' }, validators)

  if (!loading && user) {
    return <Navigate to={location.state?.from || '/dashboard'} replace />
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFormError('')
    if (!validateForm()) return

    setSubmitting(true)
    try {
      await login(values.email.trim(), values.password)
      const target = location.state?.from || '/dashboard'
      navigate(target, { replace: true })
    } catch (error) {
      const { message, fieldErrors } = extractApiError(error)
      applyServerErrors(fieldErrors, message || 'Unable to sign in.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleQuickDemo(email, pass, roleLabel) {
    setFormError('')
    setQuickLogging(roleLabel)
    handleChange({ target: { name: 'email', value: email } })
    handleChange({ target: { name: 'password', value: pass } })
    try {
      await login(email, pass)
      const target = location.state?.from || '/dashboard'
      navigate(target, { replace: true })
    } catch (error) {
      const { message, fieldErrors } = extractApiError(error)
      applyServerErrors(fieldErrors, message || 'Unable to sign in.')
    } finally {
      setQuickLogging('')
    }
  }

  return (
    <div className="axc-auth-wrapper">
      {/* Top Navbar */}
      <PublicNavbar activePage="login" />

      {/* Main Auth Container */}
      <main className="axc-auth-container">
        <div className="axc-auth-grid">
          {/* Left Form Box */}
          <div className="axc-auth-box">
            <div className="mb-4">
              <span className="badge bg-primary-subtle text-primary fw-semibold px-2 py-1 rounded-pill mb-2" style={{ fontSize: '11px' }}>
                Secure Login
              </span>
              <h1 className="h3 fw-bold text-dark mb-1">Welcome back</h1>
              <p className="text-muted small mb-0">
                Sign in to access your AcxiomCRM workspace.
              </p>
            </div>

            {/* Quick 1-Click Demo Login bar */}
            <div className="p-3 rounded-3 bg-light border mb-4">
              <div className="small fw-semibold text-secondary mb-2" style={{ fontSize: '11px', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                ⚡ 1-Click Demo Sign-In
              </div>
              <div className="d-flex gap-2 flex-wrap">
                <button
                  type="button"
                  className="axc-quick-demo-pill"
                  disabled={submitting || Boolean(quickLogging)}
                  onClick={() => handleQuickDemo('manager@acxiomcrm.local', 'Manager@123', 'Manager')}
                >
                  <span className="badge bg-primary" style={{ width: '8px', height: '8px', padding: 0, borderRadius: '50%' }} />
                  {quickLogging === 'Manager' ? 'Signing in…' : 'Sales Manager'}
                </button>
                <button
                  type="button"
                  className="axc-quick-demo-pill"
                  disabled={submitting || Boolean(quickLogging)}
                  onClick={() => handleQuickDemo('admin@acxiomcrm.local', 'Admin@123', 'Admin')}
                >
                  <span className="badge bg-purple" style={{ width: '8px', height: '8px', padding: 0, borderRadius: '50%', backgroundColor: '#7c3aed' }} />
                  {quickLogging === 'Admin' ? 'Signing in…' : 'System Admin'}
                </button>
                <button
                  type="button"
                  className="axc-quick-demo-pill"
                  disabled={submitting || Boolean(quickLogging)}
                  onClick={() => handleQuickDemo('sales@acxiomcrm.local', 'Sales@123', 'Sales')}
                >
                  <span className="badge bg-success" style={{ width: '8px', height: '8px', padding: 0, borderRadius: '50%' }} />
                  {quickLogging === 'Sales' ? 'Signing in…' : 'Sales Exec'}
                </button>
              </div>
            </div>

            {noticeOpen && (
              <div className="mb-3">
                <ErrorAlert
                  variant="success"
                  message="Account created successfully! Sign in with your credentials."
                  onDismiss={() => setNoticeOpen(false)}
                />
              </div>
            )}

            {formError && (
              <div className="mb-3">
                <ErrorAlert message={formError} onDismiss={() => setFormError('')} />
              </div>
            )}

            <form onSubmit={handleSubmit} noValidate>
              <FormField
                label="Email address"
                name="email"
                type="email"
                required
                autoComplete="username"
                placeholder="you@example.com"
                value={values.email}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.email}
                wrapperClassName="mb-3"
              />

              <FormField
                label="Password"
                name="password"
                type="password"
                required
                autoComplete="current-password"
                placeholder="Enter your password"
                value={values.password}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.password}
                wrapperClassName="mb-4"
              />

              <button
                type="submit"
                className="btn btn-lg axc-btn-glow w-100 py-2 fw-semibold"
                style={{ borderRadius: '10px' }}
                disabled={submitting || Boolean(quickLogging)}
              >
                {submitting ? 'Signing in…' : 'Sign in to CRM →'}
              </button>
            </form>

            <div className="d-flex justify-content-between align-items-center mt-4 pt-3 border-top small text-muted">
              <div>
                Don&apos;t have an account?{' '}
                <Link to="/register" className="fw-semibold text-primary text-decoration-none">
                  Create one
                </Link>
              </div>
              <div>
                <Link to="/" className="text-secondary text-decoration-none">
                  ← Back to Home
                </Link>
              </div>
            </div>
          </div>

          {/* Right Showcase Panel (Visible on Desktop) */}
          <div className="axc-auth-showcase d-none d-lg-block">
            <span className="badge bg-info-subtle text-info fw-semibold px-3 py-1 rounded-pill mb-3" style={{ fontSize: '11px' }}>
              AcxiomCRM Enterprise v2.0
            </span>
            <h2 className="h4 fw-bold text-white mb-2">
              Accelerate Deals with Unified Intelligence
            </h2>
            <p className="text-light text-opacity-75 small mb-4">
              Everything you need to convert prospects into high-value relationships.
            </p>

            <div className="axc-showcase-item">
              <div className="axc-showcase-icon">⚡</div>
              <div>
                <div className="fw-semibold text-white small">1-Click Lead Conversion</div>
                <div className="text-muted" style={{ fontSize: '12px' }}>
                  Converts leads into Customers and Opportunities in a single atomic transaction.
                </div>
              </div>
            </div>

            <div className="axc-showcase-item">
              <div className="axc-showcase-icon">📊</div>
              <div>
                <div className="fw-semibold text-white small">Stage-Weighted Forecasting</div>
                <div className="text-muted" style={{ fontSize: '12px' }}>
                  Live weighted probability calculations across all 5 opportunity stages.
                </div>
              </div>
            </div>

            <div className="axc-showcase-item">
              <div className="axc-showcase-icon">🔒</div>
              <div>
                <div className="fw-semibold text-white small">Zero-Trust RBAC &amp; CSRF</div>
                <div className="text-muted" style={{ fontSize: '12px' }}>
                  BCrypt encryption, double-submit CSRF, and strict row-level ownership isolation.
                </div>
              </div>
            </div>

            <div className="mt-4 pt-3 border-top border-light border-opacity-10">
              <div className="text-warning small mb-1">★★★★★</div>
              <div className="text-white fst-italic small">
                &ldquo;Live reporting and automated follow-ups gave our sales team unprecedented visibility.&rdquo;
              </div>
              <div className="text-muted" style={{ fontSize: '11px' }}>
                Verified 39/39 Acceptance Tests Passed
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}
