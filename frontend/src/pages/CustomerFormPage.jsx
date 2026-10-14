import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import ErrorAlert from '../components/ErrorAlert'
import FormField from '../components/FormField'
import FormPageLayout from '../components/FormPageLayout'
import LoadingState from '../components/LoadingState'
import { useForm } from '../components/useForm'
import {
  createCustomer,
  getCustomer,
  updateCustomer,
} from '../services/customerService'
import { extractApiError } from '../services/api'
import { CUSTOMER_STATUSES } from '../utils/constants'
import { formatEnumLabel, orNull } from '../utils/format'
import {
  email as validateEmail,
  maxLength,
  MAX_LENGTH,
  phone as validatePhone,
  required,
} from '../validation/validators'

const statusOptions = CUSTOMER_STATUSES.map((value) => ({
  value,
  label: formatEnumLabel(value),
}))

const validators = {
  name: (value) =>
    required(value, 'Name') || maxLength(value, MAX_LENGTH.name, 'Name'),
  email: (value) => validateEmail(value),
  phone: (value) => validatePhone(value),
  companyName: (value) =>
    maxLength(value, MAX_LENGTH.company, 'Company name'),
  address: (value) => maxLength(value, MAX_LENGTH.address, 'Address'),
  city: (value) => maxLength(value, MAX_LENGTH.city, 'City'),
  state: (value) => maxLength(value, MAX_LENGTH.state, 'State'),
  status: (value) => required(value, 'Status'),
  notes: (value) => maxLength(value, MAX_LENGTH.notes, 'Notes'),
}

const emptyValues = {
  name: '',
  email: '',
  phone: '',
  companyName: '',
  address: '',
  city: '',
  state: '',
  status: 'PROSPECT',
  notes: '',
}

function toFormValues(record) {
  return {
    name: record.name || '',
    email: record.email || '',
    phone: record.phone || '',
    companyName: record.companyName || '',
    address: record.address || '',
    city: record.city || '',
    state: record.state || '',
    status: record.status || 'PROSPECT',
    notes: record.notes || '',
  }
}

export default function CustomerFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)

  const [loadingRecord, setLoadingRecord] = useState(isEdit)
  const [loadError, setLoadError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const {
    values,
    errors,
    formError,
    setFormError,
    handleChange,
    handleBlur,
    validateForm,
    reset,
    applyServerErrors,
  } = useForm(emptyValues, validators)

  useEffect(() => {
    if (!isEdit) return undefined
    let active = true

    async function load() {
      setLoadingRecord(true)
      setLoadError('')
      try {
        const record = await getCustomer(id)
        if (active) reset(toFormValues(record))
      } catch (err) {
        if (active) setLoadError(extractApiError(err).message)
      } finally {
        if (active) setLoadingRecord(false)
      }
    }

    load()

    return () => {
      active = false
    }
  }, [id, isEdit, reset])

  async function handleSubmit(event) {
    event.preventDefault()
    setFormError('')
    if (!validateForm()) return

    setSubmitting(true)
    try {
      const payload = {
        name: values.name.trim(),
        email: values.email.trim(),
        phone: values.phone.trim(),
        companyName: orNull(values.companyName),
        address: orNull(values.address),
        city: orNull(values.city),
        state: orNull(values.state),
        status: values.status,
        notes: orNull(values.notes),
      }

      if (isEdit) {
        await updateCustomer(id, payload)
      } else {
        await createCustomer(payload)
      }

      navigate('/customers', {
        state: {
          notice: isEdit ? 'Customer updated.' : 'Customer created.',
        },
      })
    } catch (error) {
      const { message, fieldErrors } = extractApiError(error)
      applyServerErrors(fieldErrors, message || 'Unable to save the customer.')
    } finally {
      setSubmitting(false)
    }
  }

  const title = isEdit ? 'Edit customer' : 'New customer'

  return (
    <FormPageLayout
      title={title}
      subtitle={
        isEdit
          ? 'Update the account details and save your changes.'
          : 'Create a customer record for an account you are working with.'
      }
      backTo="/customers"
      loadError={loadError}
      onDismissLoadError={() => setLoadError('')}
    >
      {loadingRecord ? (
        <LoadingState label="Loading customer…" />
      ) : (
        <form onSubmit={handleSubmit} noValidate>
          {formError && (
            <div className="mb-3">
              <ErrorAlert message={formError} onDismiss={() => setFormError('')} />
            </div>
          )}

          <div className="row g-3">
            <FormField
              label="Name"
              name="name"
              required
              maxLength={MAX_LENGTH.name}
              value={values.name}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.name}
              wrapperClassName="col-12 col-md-6"
            />

            <FormField
              label="Email"
              name="email"
              type="email"
              required
              maxLength={MAX_LENGTH.email}
              value={values.email}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.email}
              wrapperClassName="col-12 col-md-6"
            />

            <FormField
              label="Phone"
              name="phone"
              required
              inputMode="numeric"
              placeholder="9876543210"
              value={values.phone}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.phone}
              help="10-digit Indian mobile number."
              wrapperClassName="col-12 col-md-6"
            />

            <FormField
              label="Company"
              name="companyName"
              maxLength={MAX_LENGTH.company}
              value={values.companyName}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.companyName}
              wrapperClassName="col-12 col-md-6"
            />

            <FormField
              label="Status"
              name="status"
              as="select"
              required
              options={statusOptions}
              value={values.status}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.status}
              wrapperClassName="col-12 col-md-6"
            />

            <FormField
              label="City"
              name="city"
              maxLength={MAX_LENGTH.city}
              value={values.city}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.city}
              wrapperClassName="col-12 col-md-3"
            />

            <FormField
              label="State"
              name="state"
              maxLength={MAX_LENGTH.state}
              value={values.state}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.state}
              wrapperClassName="col-12 col-md-3"
            />

            <FormField
              label="Address"
              name="address"
              as="textarea"
              rows={2}
              maxLength={MAX_LENGTH.address}
              value={values.address}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.address}
              wrapperClassName="col-12"
            />

            <FormField
              label="Notes"
              name="notes"
              as="textarea"
              rows={3}
              maxLength={MAX_LENGTH.notes}
              value={values.notes}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.notes}
              wrapperClassName="col-12"
            />
          </div>

          <div className="axc-form-actions mt-4">
            <button
              type="submit"
              className="btn btn-axc-primary"
              disabled={submitting}
            >
              {submitting
                ? 'Saving…'
                : isEdit
                  ? 'Save changes'
                  : 'Create customer'}
            </button>
            <button
              type="button"
              className="btn btn-outline-axc"
              onClick={() => navigate('/customers')}
              disabled={submitting}
            >
              Cancel
            </button>
          </div>
        </form>
      )}
    </FormPageLayout>
  )
}
