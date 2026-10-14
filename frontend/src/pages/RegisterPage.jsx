import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import ErrorAlert from '../components/ErrorAlert'
import FormField from '../components/FormField'
import PublicNavbar from '../components/PublicNavbar'
import { useForm } from '../components/useForm'
import { useAuth } from '../services/AuthContext'
import { register } from '../services/authService'
import { extractApiError } from '../services/api'
import {
  confirmPassword,
  email as validateEmail,
  maxLength,
  MAX_LENGTH,
  password as validatePassword,
  required,
} from '../validation/validators'

const validators = {
  name: (value) =>
    required(value, 'Name') || maxLength(value, MAX_LENGTH.name, 'Name'),
  email: (value) => validateEmail(value),
  password: (value) => validatePassword(value),
  confirmPassword: (value, values) => confirmPassword(value, values),
}

export default function RegisterPage() {
  const { user, loading } = useAuth()
  const navigate = useNavigate()
  const [submitting, setSubmitting] = useState(false)

  const {
    values,
    errors,
    formError,
    setFormError,
    handleChange,
    handleBlur,
    validateForm,
    applyServerErrors,
  } = useForm(
    { name: '', email: '', password: '', confirmPassword: '' },
    validators,
  )

  if (!loading && user) {
    return <Navigate to="/dashboard" replace />
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFormError('')
    if (!validateForm()) return

    setSubmitting(true)
    try {
      await register({
        name: values.name.trim(),
        email: values.email.trim(),
        password: values.password,
        confirmPassword: values.confirmPassword,
      })
      navigate('/login', { replace: true, state: { registered: true } })
    } catch (error) {
      const { message, fieldErrors } = extractApiError(error)
      applyServerErrors(fieldErrors, message || 'Unable to create the account.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="axc-auth-wrapper">
      {/* Top Navbar */}
      <PublicNavbar activePage="register" />

      {/* Main Container */}
      <main className="axc-auth-container">
        <div className="axc-auth-grid">
          {/* Form Card */}
          <div className="axc-auth-box">
            <div className="mb-4">
              <span className="badge bg-success-subtle text-success fw-semibold px-2 py-1 rounded-pill mb-2" style={{ fontSize: '11px' }}>
                Join AcxiomCRM
              </span>
              <h1 className="h3 fw-bold text-dark mb-1">Create an account</h1>
              <p className="text-muted small mb-0">
                New accounts are initialized with Sales Executive permissions.
              </p>
            </div>

            {formError && (
              <div className="mb-3">
                <ErrorAlert message={formError} onDismiss={() => setFormError('')} />
              </div>
            )}

            <form onSubmit={handleSubmit} noValidate>
              <FormField
                label="Full name"
                name="name"
                required
                autoComplete="name"
                placeholder="Jane Doe"
                value={values.name}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.name}
                wrapperClassName="mb-3"
              />

              <FormField
                label="Email address"
                name="email"
                type="email"
                required
                autoComplete="email"
                placeholder="jane@example.com"
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
                autoComplete="new-password"
                placeholder="Minimum 8 characters"
                value={values.password}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.password}
                help="Requires uppercase, lowercase, digit, and special character."
                wrapperClassName="mb-3"
              />

              <FormField
                label="Confirm password"
                name="confirmPassword"
                type="password"
                required
                autoComplete="new-password"
                placeholder="Re-enter your password"
                value={values.confirmPassword}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.confirmPassword}
                wrapperClassName="mb-4"
              />

              <button
                type="submit"
                className="btn btn-lg axc-btn-glow w-100 py-2 fw-semibold"
                style={{ borderRadius: '10px' }}
                disabled={submitting}
              >
                {submitting ? 'Creating account…' : 'Create Free Account &rarr;'}
              </button>
            </form>

            <div className="d-flex justify-content-between align-items-center mt-4 pt-3 border-top small text-muted">
              <div>
                Already have an account?{' '}
                <Link to="/login" className="fw-semibold text-primary text-decoration-none">
                  Sign in
                </Link>
              </div>
              <div>
                <Link to="/" className="text-secondary text-decoration-none">
                  &larr; Back to Home
                </Link>
              </div>
            </div>
          </div>

          {/* Right Showcase Panel (Visible on Desktop) */}
          <div className="axc-auth-showcase d-none d-lg-block">
            <span className="badge bg-primary-subtle text-primary fw-semibold px-3 py-1 rounded-pill mb-3" style={{ fontSize: '11px' }}>
              Why AcxiomCRM?
            </span>
            <h2 className="h4 fw-bold text-white mb-2">
              Empower Your Full Sales Team
            </h2>
            <p className="text-light text-opacity-75 small mb-4">
              Everything built with rigorous enterprise standards and bank-grade data security.
            </p>

            <div className="axc-showcase-item">
              <div className="axc-showcase-icon">👥</div>
              <div>
                <div className="fw-semibold text-white small">Customer 360&deg; Profiles</div>
                <div className="text-muted" style={{ fontSize: '12px' }}>
                  Manage complete customer relationships with full activity and opportunity history.
                </div>
              </div>
            </div>

            <div className="axc-showcase-item">
              <div className="axc-showcase-icon">📅</div>
              <div>
                <div className="fw-semibold text-white small">Smart Follow-Up Scheduling</div>
                <div className="text-muted" style={{ fontSize: '12px' }}>
                  Guaranteed date compliance: never miss a critical client touchpoint.
                </div>
              </div>
            </div>

            <div className="axc-showcase-item">
              <div className="axc-showcase-icon">🛡️</div>
              <div>
                <div className="fw-semibold text-white small">Enterprise Audit Logging</div>
                <div className="text-muted" style={{ fontSize: '12px' }}>
                  Every login and record transition is permanently logged for full governance.
                </div>
              </div>
            </div>

            <div className="mt-4 pt-3 border-top border-light border-opacity-10">
              <div className="text-warning small mb-1">★★★★★</div>
              <div className="text-white fst-italic small">
                &ldquo;Setting up our sales pipeline took minutes. The conversion workflow is seamless.&rdquo;
              </div>
              <div className="text-muted" style={{ fontSize: '11px' }}>
                AcxiomCRM Enterprise Edition
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}
