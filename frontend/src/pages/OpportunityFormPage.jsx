import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import ErrorAlert from '../components/ErrorAlert'
import FormField from '../components/FormField'
import FormPageLayout from '../components/FormPageLayout'
import LoadingState from '../components/LoadingState'
import { useForm } from '../components/useForm'
import { listCustomers } from '../services/customerService'
import {
  createOpportunity,
  getOpportunity,
  updateOpportunity,
} from '../services/opportunityService'
import { extractApiError } from '../services/api'
import { OPPORTUNITY_STAGES } from '../utils/constants'
import { formatCurrency, formatEnumLabel, orNull, todayIso } from '../utils/format'
import {
  amount as validateAmount,
  expectedCloseDate as validateExpectedCloseDate,
  maxLength,
  MAX_LENGTH,
  probability as validateProbability,
  required,
} from '../validation/validators'

const stageOptions = OPPORTUNITY_STAGES.map((value) => ({
  value,
  label: formatEnumLabel(value),
}))

const validators = {
  name: (value) =>
    required(value, 'Name') || maxLength(value, MAX_LENGTH.name, 'Name'),
  customerId: (value) => required(value, 'Customer'),
  amount: (value) => validateAmount(value),
  probability: (value) => validateProbability(value),
  stage: (value) => required(value, 'Stage'),
  expectedCloseDate: (value, values) =>
    validateExpectedCloseDate(value, values.stage),
  source: (value) => maxLength(value, MAX_LENGTH.source, 'Source'),
  notes: (value) => maxLength(value, MAX_LENGTH.notes, 'Notes'),
}

const emptyValues = {
  name: '',
  customerId: '',
  amount: '',
  probability: '',
  stage: 'QUALIFICATION',
  expectedCloseDate: '',
  source: '',
  notes: '',
}

function toFormValues(record) {
  return {
    name: record.name || '',
    customerId:
      record.customerId === null || record.customerId === undefined
        ? ''
        : String(record.customerId),
    amount:
      record.amount === null || record.amount === undefined
        ? ''
        : String(record.amount),
    probability:
      record.probability === null || record.probability === undefined
        ? ''
        : String(record.probability),
    stage: record.stage || 'QUALIFICATION',
    expectedCloseDate: record.expectedCloseDate || '',
    source: record.source || '',
    notes: record.notes || '',
  }
}

export default function OpportunityFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)

  const [loadingRecord, setLoadingRecord] = useState(isEdit)
  const [loadError, setLoadError] = useState('')
  const [customers, setCustomers] = useState([])
  const [loadingCustomers, setLoadingCustomers] = useState(true)
  const [customersError, setCustomersError] = useState('')
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

  // Customers for the picker.
  useEffect(() => {
    let active = true

    async function loadCustomers() {
      setLoadingCustomers(true)
      setCustomersError('')
      try {
        const data = await listCustomers({ page: 0, size: 200 })
        if (active) setCustomers(data?.content || [])
      } catch (err) {
        if (active) setCustomersError(extractApiError(err).message)
      } finally {
        if (active) setLoadingCustomers(false)
      }
    }

    loadCustomers()

    return () => {
      active = false
    }
  }, [])

  // Existing opportunity when editing.
  useEffect(() => {
    if (!isEdit) return undefined
    let active = true

    async function load() {
      setLoadingRecord(true)
      setLoadError('')
      try {
        const record = await getOpportunity(id)
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

  const customerOptions = useMemo(() => {
    const options = [{ value: '', label: 'Select a customer…' }]
    const known = new Set(customers.map((customer) => String(customer.id)))

    customers.forEach((customer) => {
      options.push({
        value: String(customer.id),
        label: customer.name,
      })
    })

    // Keep the selected customer visible even if it is off the loaded page.
    if (values.customerId && !known.has(String(values.customerId))) {
      options.push({
        value: String(values.customerId),
        label: `Customer #${values.customerId}`,
      })
    }

    return options
  }, [customers, values.customerId])

  const amountNumber = Number(values.amount)
  const probabilityNumber = Number(values.probability)
  const hasAmount = values.amount !== '' && Number.isFinite(amountNumber)
  const hasProbability =
    values.probability !== '' &&
    Number.isFinite(probabilityNumber) &&
    probabilityNumber >= 0 &&
    probabilityNumber <= 100
  const weighted = hasAmount && hasProbability
    ? (amountNumber * probabilityNumber) / 100
    : 0

  const isClosedStage = values.stage === 'WON' || values.stage === 'LOST'

  async function handleSubmit(event) {
    event.preventDefault()
    setFormError('')
    if (!validateForm()) return

    setSubmitting(true)
    try {
      const payload = {
        name: values.name.trim(),
        customerId: Number(values.customerId),
        amount: Number(values.amount),
        probability: Number(values.probability),
        stage: values.stage,
        expectedCloseDate: values.expectedCloseDate,
        source: orNull(values.source),
        notes: orNull(values.notes),
      }

      if (isEdit) {
        await updateOpportunity(id, payload)
      } else {
        await createOpportunity(payload)
      }

      navigate('/opportunities', {
        state: {
          notice: isEdit ? 'Opportunity updated.' : 'Opportunity created.',
        },
      })
    } catch (error) {
      const { message, fieldErrors } = extractApiError(error)
      applyServerErrors(
        fieldErrors,
        message || 'Unable to save the opportunity.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  const title = isEdit ? 'Edit opportunity' : 'New opportunity'

  return (
    <FormPageLayout
      title={title}
      subtitle={
        isEdit
          ? 'Update the deal and save your changes.'
          : 'Track a new deal against an existing customer.'
      }
      backTo="/opportunities"
      loadError={loadError}
      onDismissLoadError={() => setLoadError('')}
    >
      {loadingRecord ? (
        <LoadingState label="Loading opportunity…" />
      ) : (
        <form onSubmit={handleSubmit} noValidate>
          {formError && (
            <div className="mb-3">
              <ErrorAlert message={formError} onDismiss={() => setFormError('')} />
            </div>
          )}

          {customersError && (
            <div className="mb-3">
              <ErrorAlert
                message={`Could not load customers: ${customersError}`}
                onDismiss={() => setCustomersError('')}
              />
            </div>
          )}

          <div className="row g-3">
            <FormField
              label="Opportunity name"
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
              label="Customer"
              name="customerId"
              as="select"
              required
              options={customerOptions}
              disabled={loadingCustomers}
              value={values.customerId}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.customerId}
              help={loadingCustomers ? 'Loading customers…' : undefined}
              wrapperClassName="col-12 col-md-6"
            />

            <FormField
              label="Amount"
              name="amount"
              type="number"
              min="0"
              step="0.01"
              required
              value={values.amount}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.amount}
              wrapperClassName="col-12 col-md-4"
            />

            <FormField
              label="Probability (%)"
              name="probability"
              type="number"
              min="0"
              max="100"
              step="1"
              required
              value={values.probability}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.probability}
              wrapperClassName="col-12 col-md-4"
            />

            <FormField
              label="Stage"
              name="stage"
              as="select"
              required
              options={stageOptions}
              value={values.stage}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.stage}
              wrapperClassName="col-12 col-md-4"
            />

            <FormField
              label="Expected close date"
              name="expectedCloseDate"
              type="date"
              required
              min={isClosedStage ? undefined : todayIso()}
              value={values.expectedCloseDate}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.expectedCloseDate}
              help={
                isClosedStage
                  ? 'Closed deals may carry a past date.'
                  : 'Cannot be in the past while the deal is open.'
              }
              wrapperClassName="col-12 col-md-6"
            />

            <FormField
              label="Source"
              name="source"
              maxLength={MAX_LENGTH.source}
              value={values.source}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.source}
              wrapperClassName="col-12 col-md-6"
            />

            <div className="col-12 col-md-6">
              <div className="axc-weighted">
                <span className="axc-weighted__label">Weighted amount</span>
                <span className="axc-weighted__value">
                  {formatCurrency(weighted)}
                </span>
              </div>
              <p className="axc-help mb-0">
                Weighted amount = amount × probability ÷ 100.
              </p>
            </div>

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
                  : 'Create opportunity'}
            </button>
            <button
              type="button"
              className="btn btn-outline-axc"
              onClick={() => navigate('/opportunities')}
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
