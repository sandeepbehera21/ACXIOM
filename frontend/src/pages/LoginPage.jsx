import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import ErrorAlert from '../components/ErrorAlert'
import FormField from '../components/FormField'
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
    return <Navigate to={location.state?.from || '/'} replace />
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFormError('')
    if (!validateForm()) return

    setSubmitting(true)
    try {
      await login(values.email.trim(), values.password)
      const target = location.state?.from || '/'
      navigate(target, { replace: true })
    } catch (error) {
      const { message, fieldErrors } = extractApiError(error)
      applyServerErrors(fieldErrors, message || 'Unable to sign in.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="axc-auth-page">
      <div className="axc-auth-card">
        <div className="axc-auth-card__header">
          <span className="axc-brand-mark" aria-hidden="true" />
          <h1>AcxiomCRM</h1>
        </div>

        <div className="axc-auth-card__body">
          <h2 className="axc-page-title mb-1">Sign in</h2>
          <p className="axc-page-subtitle mb-3">
            Use your AcxiomCRM account to continue.
          </p>

          {noticeOpen && (
            <div className="mb-3">
              <ErrorAlert
                variant="success"
                message="Account created. Sign in with your new credentials."
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
              label="Email"
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
              value={values.password}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.password}
              wrapperClassName="mb-3"
            />

            <button
              type="submit"
              className="btn btn-axc-primary w-100"
              disabled={submitting}
            >
              {submitting ? 'Signing in…' : 'Sign in'}
            </button>
          </form>

          <div className="axc-auth-footer">
            Don&apos;t have an account? <Link to="/register">Create one</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
