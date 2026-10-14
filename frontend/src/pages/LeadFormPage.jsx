import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import ErrorAlert from '../components/ErrorAlert'
import FormField from '../components/FormField'
import FormPageLayout from '../components/FormPageLayout'
import LoadingState from '../components/LoadingState'
import { useForm } from '../components/useForm'
import { createLead, getLead, updateLead } from '../services/leadService'
import { extractApiError } from '../services/api'
import { LEAD_PRIORITIES, LEAD_STATUSES } from '../utils/constants'
import { formatEnumLabel, orNull } from '../utils/format'
import {
  email as validateEmail,
  maxLength,
  MAX_LENGTH,
  phone as validatePhone,
  required,
} from '../validation/validators'

const statusOptions = LEAD_STATUSES.map((value) => ({
  value,
  label: formatEnumLabel(value),
}))

const priorityOptions = LEAD_PRIORITIES.map((value) => ({
  value,
  label: formatEnumLabel(value),
}))

function optionalExpectedValue(value) {
  if (value === '' || value === null || value === undefined) return null
  const parsed = Number(value)
  if (Number.isNaN(parsed)) return 'Expected value must be a number.'
  if (parsed < 0) return 'Expected value cannot be negative.'
  return null
}

const validators = {
  name: (value) =>
    required(value, 'Name') || maxLength(value, MAX_LENGTH.name, 'Name'),
  email: (value) => validateEmail(value),
  phone: (value) => validatePhone(value),
  companyName: (value) =>
    maxLength(value, MAX_LENGTH.company, 'Company name'),
  source: (value) => maxLength(value, MAX_LENGTH.source, 'Source'),
  status: (value) => required(value, 'Status'),
  priority: (value) => required(value, 'Priority'),
  expectedValue: (value) => optionalExpectedValue(value),
  notes: (value) => maxLength(value, MAX_LENGTH.notes, 'Notes'),
}

const emptyValues = {
  name: '',
  email: '',
  phone: '',
  companyName: '',
  source: '',
  status: 'NEW',
  priority: 'MEDIUM',
  expectedValue: '',
  notes: '',
}

function toFormValues(record) {
  return {
    name: record.name || '',
    email: record.email || '',
    phone: record.phone || '',
    companyName: record.companyName || '',
    source: record.source || '',
    status: record.status || 'NEW',
    priority: record.priority || 'MEDIUM',
    expectedValue:
      record.expectedValue === null || record.expectedValue === undefined
        ? ''
        : String(record.expectedValue),
    notes: record.notes || '',
  }
}

export default function LeadFormPage() {
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
        const record = await getLead(id)
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
        source: orNull(values.source),
        status: values.status,
        priority: values.priority,
        expectedValue:
          values.expectedValue === '' ? null : Number(values.expectedValue),
        notes: orNull(values.notes),
      }

      if (isEdit) {
        await updateLead(id, payload)
      } else {
        await createLead(payload)
      }

      navigate('/leads', {
        state: { notice: isEdit ? 'Lead updated.' : 'Lead created.' },
      })
    } catch (error) {
      const { message, fieldErrors } = extractApiError(error)
      applyServerErrors(fieldErrors, message || 'Unable to save the lead.')
    } finally {
      setSubmitting(false)
    }
  }

  const title = isEdit ? 'Edit lead' : 'New lead'

  return (
    <FormPageLayout
      title={title}
      subtitle={
        isEdit
          ? 'Update the lead details and save your changes.'
          : 'Capture a new enquiry so it can be qualified and converted.'
      }
      backTo="/leads"
      loadError={loadError}
      onDismissLoadError={() => setLoadError('')}
    >
      {loadingRecord ? (
        <LoadingState label="Loading lead…" />
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
              label="Source"
              name="source"
              maxLength={MAX_LENGTH.source}
              placeholder="Website, referral, campaign…"
              value={values.source}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.source}
              wrapperClassName="col-12 col-md-6"
            />

            <FormField
              label="Expected value"
              name="expectedValue"
              type="number"
              min="0"
              step="0.01"
              value={values.expectedValue}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.expectedValue}
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
              label="Priority"
              name="priority"
              as="select"
              required
              options={priorityOptions}
              value={values.priority}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.priority}
              wrapperClassName="col-12 col-md-6"
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
              {submitting ? 'Saving…' : isEdit ? 'Save changes' : 'Create lead'}
            </button>
            <button
              type="button"
              className="btn btn-outline-axc"
              onClick={() => navigate('/leads')}
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
