import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import ErrorAlert from '../components/ErrorAlert'
import FormField from '../components/FormField'
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
    return <Navigate to="/" replace />
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
    <div className="axc-auth-page">
      <div className="axc-auth-card">
        <div className="axc-auth-card__header">
          <span className="axc-brand-mark" aria-hidden="true" />
          <h1>AcxiomCRM</h1>
        </div>

        <div className="axc-auth-card__body">
          <h2 className="axc-page-title mb-1">Create account</h2>
          <p className="axc-page-subtitle mb-3">
            New accounts are created with the Sales Executive role. An
            administrator can change your role afterwards.
          </p>

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
              value={values.name}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.name}
              wrapperClassName="mb-3"
            />

            <FormField
              label="Email"
              name="email"
              type="email"
              required
              autoComplete="email"
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
              value={values.password}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.password}
              help="At least 8 characters, with an uppercase letter, a lowercase letter, a digit and a special character."
              wrapperClassName="mb-3"
            />

            <FormField
              label="Confirm password"
              name="confirmPassword"
              type="password"
              required
              autoComplete="new-password"
              value={values.confirmPassword}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.confirmPassword}
              wrapperClassName="mb-3"
            />

            <button
              type="submit"
              className="btn btn-axc-primary w-100"
              disabled={submitting}
            >
              {submitting ? 'Creating account…' : 'Create account'}
            </button>
          </form>

          <div className="axc-auth-footer">
            Already registered? <Link to="/login">Sign in</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
