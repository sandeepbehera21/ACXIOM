import { useEffect, useState } from 'react'
import ConfirmDialog from '../components/ConfirmDialog'
import EmptyState from '../components/EmptyState'
import ErrorAlert from '../components/ErrorAlert'
import LoadingState from '../components/LoadingState'
import Modal from '../components/Modal'
import PageHeader from '../components/PageHeader'
import StatusBadge from '../components/StatusBadge'
import {
  createFollowUp,
  deleteFollowUp,
  listFollowUps,
  updateFollowUp,
} from '../services/followUpService'
import { extractApiError } from '../services/api'
import { FOLLOW_UP_STATUSES, FOLLOW_UP_TYPES } from '../utils/constants'
import { formatDate, formatEnumLabel } from '../utils/format'

export default function FollowUpListPage() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [notice, setNotice] = useState('')

  // Modal state
  const [showModal, setShowModal] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [modalError, setModalError] = useState('')
  const [editingItem, setEditingItem] = useState(null)
  const [form, setForm] = useState({
    subject: '',
    followUpDate: '',
    followUpType: 'CALL',
    status: 'PLANNED',
    remarks: '',
  })

  // Delete dialog
  const [pendingDelete, setPendingDelete] = useState(null)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    let active = true
    async function fetchList() {
      setLoading(true)
      setError('')
      try {
        const params = {}
        if (statusFilter) params.status = statusFilter
        const data = await listFollowUps(params)
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
  }, [statusFilter])

  async function loadData() {
    try {
      const params = {}
      if (statusFilter) params.status = statusFilter
      const data = await listFollowUps(params)
      setItems(data)
    } catch (err) {
      setError(extractApiError(err).message)
    }
  }

  function openCreate() {
    setEditingItem(null)
    setForm({
      subject: '',
      followUpDate: new Date().toISOString().split('T')[0],
      followUpType: 'CALL',
      status: 'PLANNED',
      remarks: '',
    })
    setModalError('')
    setShowModal(true)
  }

  function openEdit(item) {
    setEditingItem(item)
    setForm({
      subject: item.subject || '',
      followUpDate: item.followUpDate || '',
      followUpType: item.followUpType || 'CALL',
      status: item.status || 'PLANNED',
      remarks: item.remarks || '',
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
        await updateFollowUp(editingItem.id, form)
        setNotice('Follow-up updated successfully.')
      } else {
        await createFollowUp(form)
        setNotice('Follow-up scheduled successfully.')
      }
      setShowModal(false)
      loadData()
    } catch (err) {
      const apiErr = extractApiError(err)
      setModalError(apiErr.message || 'Failed to save follow-up.')
    } finally {
      setSubmitting(false)
    }
  }

  async function quickUpdateStatus(item, newStatus) {
    try {
      await updateFollowUp(item.id, {
        subject: item.subject,
        followUpDate: item.followUpDate,
        followUpType: item.followUpType,
        remarks: item.remarks,
        status: newStatus,
      })
      setNotice(`Follow-up marked as ${formatEnumLabel(newStatus)}.`)
      loadData()
    } catch (err) {
      setError(extractApiError(err).message)
    }
  }

  async function confirmDelete() {
    if (!pendingDelete) return
    setDeleting(true)
    try {
      await deleteFollowUp(pendingDelete.id)
      setNotice('Follow-up removed.')
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
        title="Follow-Ups"
        subtitle="Schedule and track customer and lead touchpoints."
        action={
          <button
            type="button"
            className="btn btn-sm btn-axc-primary"
            onClick={openCreate}
          >
            + Schedule Follow-Up
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
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="">All Statuses</option>
          {FOLLOW_UP_STATUSES.map((s) => (
            <option key={s} value={s}>
              {formatEnumLabel(s)}
            </option>
          ))}
        </select>
      </div>

      <div className="axc-card">
        {loading ? (
          <LoadingState label="Loading follow-ups…" />
        ) : items.length === 0 ? (
          <EmptyState
            title="No follow-ups found"
            message="No follow-up records match the criteria. Schedule one to get started."
          />
        ) : (
          <div className="axc-table-wrap">
            <table className="axc-table">
              <thead>
                <tr>
                  <th>Subject</th>
                  <th>Date</th>
                  <th>Type</th>
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
                      <button
                        type="button"
                        className="btn btn-link p-0 text-start fw-medium text-decoration-none"
                        onClick={() => openEdit(row)}
                      >
                        {row.subject}
                      </button>
                      {row.remarks && (
                        <div className="text-muted small">{row.remarks}</div>
                      )}
                    </td>
                    <td>{formatDate(row.followUpDate)}</td>
                    <td>
                      <span className="badge bg-light text-dark border">
                        {row.followUpType}
                      </span>
                    </td>
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
                        {row.status === 'PLANNED' && (
                          <button
                            type="button"
                            className="btn btn-outline-success btn-sm"
                            title="Mark Complete"
                            onClick={() => quickUpdateStatus(row, 'COMPLETED')}
                          >
                            ✓ Complete
                          </button>
                        )}
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
        title={editingItem ? 'Edit Follow-Up' : 'Schedule Follow-Up'}
      >
        <form onSubmit={handleSubmit}>
          {modalError && (
            <div className="mb-3">
              <ErrorAlert message={modalError} onDismiss={() => setModalError('')} />
            </div>
          )}

          <div className="mb-3">
            <label className="form-label required">Subject</label>
            <input
              type="text"
              className="form-control form-control-sm"
              required
              value={form.subject}
              onChange={(e) => setForm({ ...form, subject: e.target.value })}
              placeholder="e.g. Call regarding contract renewal"
            />
          </div>

          <div className="row g-2 mb-3">
            <div className="col-sm-6">
              <label className="form-label required">Follow-Up Date</label>
              <input
                type="date"
                className="form-control form-control-sm"
                required
                value={form.followUpDate}
                onChange={(e) =>
                  setForm({ ...form, followUpDate: e.target.value })
                }
              />
            </div>
            <div className="col-sm-6">
              <label className="form-label required">Type</label>
              <select
                className="form-select form-select-sm"
                value={form.followUpType}
                onChange={(e) =>
                  setForm({ ...form, followUpType: e.target.value })
                }
              >
                {FOLLOW_UP_TYPES.map((t) => (
                  <option key={t} value={t}>
                    {t}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label">Status</label>
            <select
              className="form-select form-select-sm"
              value={form.status}
              onChange={(e) => setForm({ ...form, status: e.target.value })}
            >
              {FOLLOW_UP_STATUSES.map((s) => (
                <option key={s} value={s}>
                  {formatEnumLabel(s)}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-3">
            <label className="form-label">Remarks / Notes</label>
            <textarea
              className="form-control form-control-sm"
              rows="3"
              value={form.remarks}
              onChange={(e) => setForm({ ...form, remarks: e.target.value })}
              placeholder="Discussion notes or reminders..."
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
              {submitting ? 'Saving…' : editingItem ? 'Save Changes' : 'Schedule'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmDialog
        open={Boolean(pendingDelete)}
        title="Delete Follow-Up"
        message={`Are you sure you want to delete follow-up "${pendingDelete?.subject}"?`}
        confirmLabel={deleting ? 'Deleting…' : 'Delete'}
        onConfirm={confirmDelete}
        onCancel={() => setPendingDelete(null)}
      />
    </div>
  )
}
