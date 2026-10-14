import { useState } from 'react'
import ErrorAlert from './ErrorAlert'
import FormField from './FormField'
import Modal from './Modal'
import { useForm } from './useForm'
import { convertLead } from '../services/leadService'
import { extractApiError } from '../services/api'
import { OPPORTUNITY_STAGES } from '../utils/constants'
import {
  amount as validateAmount,
  expectedCloseDate as validateExpectedCloseDate,
  maxLength,
  MAX_LENGTH,
  probability as validateProbability,
  required,
} from '../validation/validators'
import { formatCurrency, formatEnumLabel, todayIso } from '../utils/format'

const stageOptions = OPPORTUNITY_STAGES.filter(
  (stage) => stage !== 'WON' && stage !== 'LOST',
).map((stage) => ({ value: stage, label: formatEnumLabel(stage) }))

const validators = {
  opportunityName: (value, values) =>
    values.createOpportunity
      ? required(value, 'Opportunity name') ||
        maxLength(value, MAX_LENGTH.name, 'Opportunity name')
      : null,
  amount: (value, values) =>
    values.createOpportunity ? validateAmount(value) : null,
  probability: (value, values) =>
    values.createOpportunity ? validateProbability(value) : null,
  expectedCloseDate: (value, values) =>
    values.createOpportunity
      ? validateExpectedCloseDate(value, values.stage)
      : null,
  stage: (value, values) =>
    values.createOpportunity ? required(value, 'Stage') : null,
}

const OPPORTUNITY_FIELDS = [
  'opportunityName',
  'amount',
  'probability',
  'expectedCloseDate',
  'stage',
  'source',
]

/**
 * "Convert lead" dialog. Converts the lead into a customer and, when the
 * checkbox is ticked, also creates an opportunity from the supplied details.
 */
export default function ConvertLeadModal({ lead, onClose, onConverted }) {
  const [submitting, setSubmitting] = useState(false)

  const {
    values,
    errors,
    formError,
    setFormError,
    setValue,
    handleChange,
    handleBlur,
    validateForm,
    applyServerErrors,
    clearFieldError,
  } = useForm(
    {
      createOpportunity: true,
      opportunityName: '',
      amount: '',
      probability: '',
      expectedCloseDate: '',
      stage: 'QUALIFICATION',
      source: lead?.source || '',
    },
    validators,
  )

  function handleToggle(event) {
    const checked = event.target.checked
    setValue('createOpportunity', checked)
    OPPORTUNITY_FIELDS.forEach(clearFieldError)
  }

  const amountNumber = Number(values.amount)
  const probabilityNumber = Number(values.probability)
  const weighted =
    Number.isFinite(amountNumber) &&
    Number.isFinite(probabilityNumber) &&
    probabilityNumber >= 0 &&
    probabilityNumber <= 100
      ? (amountNumber * probabilityNumber) / 100
      : 0

  async function handleSubmit(event) {
    event.preventDefault()
    setFormError('')
    if (!validateForm()) return

    setSubmitting(true)
    try {
      const payload = values.createOpportunity
        ? {
            createOpportunity: true,
            opportunityName: values.opportunityName.trim(),
            amount: Number(values.amount),
            probability: Number(values.probability),
            expectedCloseDate: values.expectedCloseDate,
            stage: values.stage,
            source: values.source.trim() || null,
          }
        : {
            createOpportunity: false,
            opportunityName: null,
            amount: null,
            probability: null,
            expectedCloseDate: null,
            stage: null,
            source: null,
          }

      const result = await convertLead(lead.id, payload)
      onConverted(result, lead)
    } catch (error) {
      const { message, fieldErrors } = extractApiError(error)
      applyServerErrors(fieldErrors, message || 'Unable to convert the lead.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal
      title={`Convert lead — ${lead?.name}`}
      size="lg"
      onClose={submitting ? () => {} : onClose}
      footer={
        <>
          <button
            type="button"
            className="btn btn-outline-axc"
            onClick={onClose}
            disabled={submitting}
          >
            Cancel
          </button>
          <button
            type="submit"
            form="convert-lead-form"
            className="btn btn-axc-primary"
            disabled={submitting}
          >
            {submitting ? 'Converting…' : 'Convert lead'}
          </button>
        </>
      }
    >
      {formError && (
        <div className="mb-3">
          <ErrorAlert message={formError} onDismiss={() => setFormError('')} />
        </div>
      )}

      <form id="convert-lead-form" onSubmit={handleSubmit} noValidate>
        <p className="axc-modal-note">
          Converting creates a customer record from this lead and marks the lead
          as converted.
        </p>

        <div className="form-check mb-3">
          <input
            className="form-check-input"
            type="checkbox"
            id="createOpportunity"
            name="createOpportunity"
            checked={values.createOpportunity}
            onChange={handleToggle}
          />
          <label className="form-check-label" htmlFor="createOpportunity">
            Also create an opportunity for this customer
          </label>
        </div>

        {values.createOpportunity && (
          <>
            <div className="row g-3">
              <FormField
                label="Opportunity name"
                name="opportunityName"
                required
                value={values.opportunityName}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.opportunityName}
                wrapperClassName="col-12"
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
                wrapperClassName="col-12 col-sm-6"
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
                wrapperClassName="col-12 col-sm-6"
              />

              <FormField
                label="Expected close date"
                name="expectedCloseDate"
                type="date"
                required
                min={todayIso()}
                value={values.expectedCloseDate}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.expectedCloseDate}
                wrapperClassName="col-12 col-sm-6"
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
                wrapperClassName="col-12 col-sm-6"
              />

              <FormField
                label="Source"
                name="source"
                maxLength={MAX_LENGTH.source}
                value={values.source}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.source}
                wrapperClassName="col-12 col-sm-6"
              />
            </div>

            <div className="axc-weighted mt-3">
              <span className="axc-weighted__label">Weighted amount</span>
              <span className="axc-weighted__value">
                {formatCurrency(weighted)}
              </span>
            </div>
            <p className="axc-help mb-0">
              Weighted amount = amount × probability ÷ 100.
            </p>
          </>
        )}
      </form>
    </Modal>
  )
}
