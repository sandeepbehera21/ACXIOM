import { useEffect, useState } from 'react'
import ConfirmDialog from '../components/ConfirmDialog'
import EmptyState from '../components/EmptyState'
import ErrorAlert from '../components/ErrorAlert'
import LoadingState from '../components/LoadingState'
import Modal from '../components/Modal'
import PageHeader from '../components/PageHeader'
import StatusBadge from '../components/StatusBadge'
import {
  createActivity,
  deleteActivity,
  listActivities,
  updateActivity,
} from '../services/activityService'
import { extractApiError } from '../services/api'
import { ACTIVITY_STATUSES, ACTIVITY_TYPES } from '../utils/constants'
import { formatDateTime, formatEnumLabel } from '../utils/format'

export default function ActivityListPage() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [typeFilter, setTypeFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [notice, setNotice] = useState('')

  // Modal
  const [showModal, setShowModal] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [modalError, setModalError] = useState('')
  const [editingItem, setEditingItem] = useState(null)
  const [form, setForm] = useState({
    activityType: 'CALL',
    subject: '',
    description: '',
    activityDate: '',
    status: 'PLANNED',
  })

  // Delete
  const [pendingDelete, setPendingDelete] = useState(null)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    let active = true
    async function fetchList() {
      setLoading(true)
      setError('')
      try {
        const params = {}
        if (typeFilter) params.activityType = typeFilter
        if (statusFilter) params.status = statusFilter
        const data = await listActivities(params)
        if (active) setItems(data)
      } catch (err) {
        if (active) setError(extractApiError(err).message)
      } finally {
        if (active) setLoading(false)
      }
    }
    fetchList()
    return () => {
      active = false
    }
  }, [typeFilter, statusFilter])

  async function loadData() {
    try {
      const params = {}
      if (typeFilter) params.activityType = typeFilter
      if (statusFilter) params.status = statusFilter
      const data = await listActivities(params)
      setItems(data)
    } catch (err) {
      setError(extractApiError(err).message)
    }
  }

  function openCreate() {
    setEditingItem(null)
    setForm({
      activityType: 'CALL',
      subject: '',
      description: '',
      activityDate: new Date().toISOString().slice(0, 16),
      status: 'PLANNED',
    })
    setModalError('')
    setShowModal(true)
  }

  function openEdit(item) {
    setEditingItem(item)
    setForm({
      activityType: item.activityType || 'CALL',
      subject: item.subject || '',
      description: item.description || '',
      activityDate: item.activityDate ? item.activityDate.slice(0, 16) : '',
      status: item.status || 'PLANNED',
    })
    setModalError('')
    setShowModal(true)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitting(true)
    setModalError('')
    try {
      if (editingItem) {
        await updateActivity(editingItem.id, form)
        setNotice('Activity updated successfully.')
      } else {
        await createActivity(form)
        setNotice('Activity recorded successfully.')
      }
      setShowModal(false)
      loadData()
    } catch (err) {
      const apiErr = extractApiError(err)
      setModalError(apiErr.message || 'Failed to save activity.')
    } finally {
      setSubmitting(false)
    }
  }

  async function confirmDelete() {
    if (!pendingDelete) return
    setDeleting(true)
    try {
      await deleteActivity(pendingDelete.id)
      setNotice('Activity deleted.')
      setPendingDelete(null)
      loadData()
    } catch (err) {
      setError(extractApiError(err).message)
      setPendingDelete(null)
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div>
      <PageHeader
        title="Activities"
        subtitle="Customer communication and task log (Calls, Meetings, Emails, Tasks)."
        action={
          <button
            type="button"
            className="btn btn-sm btn-axc-primary"
            onClick={openCreate}
          >
            + Record Activity
          </button>
        }
      />

      {notice && (
        <div className="alert alert-success alert-dismissible mb-3" role="alert">
          {notice}
          <button
            type="button"
            className="btn-close"
            onClick={() => setNotice('')}
            aria-label="Close"
          />
        </div>
      )}

      {error && (
        <div className="mb-3">
          <ErrorAlert message={error} onDismiss={() => setError('')} />
        </div>
      )}

      <div className="axc-filter-bar mb-3 d-flex flex-wrap gap-2 align-items-center">
        <select
          className="form-select form-select-sm w-auto"
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value)}
        >
          <option value="">All Types</option>
          {ACTIVITY_TYPES.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
        <select
          className="form-select form-select-sm w-auto"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="">All Statuses</option>
          {ACTIVITY_STATUSES.map((s) => (
            <option key={s} value={s}>
              {formatEnumLabel(s)}
            </option>
          ))}
        </select>
      </div>

      <div className="axc-card">
        {loading ? (
          <LoadingState label="Loading activities…" />
        ) : items.length === 0 ? (
          <EmptyState
            title="No activities recorded"
            message="No activities match your filters. Record a call, meeting, email or task."
          />
        ) : (
          <div className="axc-table-wrap">
            <table className="axc-table">
              <thead>
                <tr>
                  <th>Type</th>
                  <th>Subject</th>
                  <th>Date & Time</th>
                  <th>Status</th>
                  <th>Related To</th>
                  <th>Assigned To</th>
                  <th className="text-end">Actions</th>
                </tr>
              </thead>
              <tbody>
                {items.map((row) => (
                  <tr key={row.id}>
                    <td>
                      <span className="badge bg-secondary">{row.activityType}</span>
                    </td>
                    <td>
                      <div className="fw-medium">{row.subject}</div>
                      {row.description && (
                        <div className="text-muted small">{row.description}</div>
                      )}
                    </td>
                    <td>{formatDateTime(row.activityDate)}</td>
                    <td>
                      <StatusBadge value={row.status} />
                    </td>
                    <td>
                      {row.customerName && <span>Customer: {row.customerName}</span>}
                      {row.leadName && <span>Lead: {row.leadName}</span>}
                      {!row.customerName && !row.leadName && (
                        <span className="text-muted">—</span>
                      )}
                    </td>
                    <td>{row.assignedToName || '—'}</td>
                    <td className="text-end">
                      <div className="btn-group btn-group-sm">
                        <button
                          type="button"
                          className="btn btn-outline-secondary btn-sm"
                          onClick={() => openEdit(row)}
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          className="btn btn-outline-danger btn-sm"
                          onClick={() => setPendingDelete(row)}
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal for Create/Edit */}
      <Modal
        open={showModal}
        onClose={() => setShowModal(false)}
        title={editingItem ? 'Edit Activity' : 'Record Activity'}
      >
        <form onSubmit={handleSubmit}>
          {modalError && (
            <div className="mb-3">
              <ErrorAlert message={modalError} onDismiss={() => setModalError('')} />
            </div>
          )}

          <div className="row g-2 mb-3">
            <div className="col-sm-6">
              <label className="form-label required">Activity Type</label>
              <select
                className="form-select form-select-sm"
                value={form.activityType}
                onChange={(e) =>
                  setForm({ ...form, activityType: e.target.value })
                }
              >
                {ACTIVITY_TYPES.map((t) => (
                  <option key={t} value={t}>
                    {t}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-sm-6">
              <label className="form-label required">Status</label>
              <select
                className="form-select form-select-sm"
                value={form.status}
                onChange={(e) => setForm({ ...form, status: e.target.value })}
              >
                {ACTIVITY_STATUSES.map((s) => (
                  <option key={s} value={s}>
                    {formatEnumLabel(s)}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label required">Subject</label>
            <input
              type="text"
              className="form-control form-control-sm"
              required
              value={form.subject}
              onChange={(e) => setForm({ ...form, subject: e.target.value })}
              placeholder="e.g. Product demo call"
            />
          </div>

          <div className="mb-3">
            <label className="form-label required">Activity Date & Time</label>
            <input
              type="datetime-local"
              className="form-control form-control-sm"
              required
              value={form.activityDate}
              onChange={(e) =>
                setForm({ ...form, activityDate: e.target.value })
              }
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Description / Minutes</label>
            <textarea
              className="form-control form-control-sm"
              rows="3"
              value={form.description}
              onChange={(e) =>
                setForm({ ...form, description: e.target.value })
              }
              placeholder="Details of discussion, action items..."
            />
          </div>

          <div className="d-flex justify-content-end gap-2">
            <button
              type="button"
              className="btn btn-sm btn-outline-secondary"
              onClick={() => setShowModal(false)}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-sm btn-axc-primary"
              disabled={submitting}
            >
              {submitting ? 'Saving…' : editingItem ? 'Save Changes' : 'Record'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmDialog
        open={Boolean(pendingDelete)}
        title="Delete Activity"
        message={`Are you sure you want to delete activity "${pendingDelete?.subject}"?`}
        confirmLabel={deleting ? 'Deleting…' : 'Delete'}
        onConfirm={confirmDelete}
        onCancel={() => setPendingDelete(null)}
      />
    </div>
  )
}
