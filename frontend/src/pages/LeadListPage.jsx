import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import ConfirmDialog from '../components/ConfirmDialog'
import ConvertLeadModal from '../components/ConvertLeadModal'
import EmptyState from '../components/EmptyState'
import ErrorAlert from '../components/ErrorAlert'
import LoadingState from '../components/LoadingState'
import PageHeader from '../components/PageHeader'
import Pagination from '../components/Pagination'
import SearchInput from '../components/SearchInput'
import StatusBadge from '../components/StatusBadge'
import { usePagedList } from '../components/usePagedList'
import { deleteLead, listLeads } from '../services/leadService'
import { extractApiError } from '../services/api'
import { LEAD_STATUSES, PAGE_SIZE } from '../utils/constants'
import { formatCurrency, formatDate, formatEnumLabel } from '../utils/format'

const statusOptions = [
  { value: '', label: 'All statuses' },
  ...LEAD_STATUSES.map((value) => ({
    value,
    label: formatEnumLabel(value),
  })),
]

export default function LeadListPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const list = usePagedList({ fetcher: listLeads, size: PAGE_SIZE })

  const [converting, setConverting] = useState(null)
  const [pendingDelete, setPendingDelete] = useState(null)
  const [deleting, setDeleting] = useState(false)
  const [notice, setNotice] = useState(location.state?.notice || '')
  const [actionError, setActionError] = useState('')

  function handleConverted(result, lead) {
    setConverting(null)
    setNotice(
      result?.opportunity
        ? `"${lead.name}" was converted. Customer and opportunity created.`
        : `"${lead.name}" was converted to a customer.`,
    )
    list.reload()
  }

  async function confirmDelete() {
    if (!pendingDelete) return
    setDeleting(true)
    setActionError('')
    try {
      await deleteLead(pendingDelete.id)
      setNotice(`Lead "${pendingDelete.name}" was removed.`)
      setPendingDelete(null)
      list.reload()
    } catch (err) {
      setActionError(extractApiError(err).message)
      setPendingDelete(null)
    } finally {
      setDeleting(false)
    }
  }

  const { items, loading, error } = list

  return (
    <div>
      <PageHeader
        title="Leads"
        subtitle="Incoming enquiries waiting to be qualified."
        action={
          <Link className="btn btn-sm btn-axc-primary" to="/leads/new">
            New lead
          </Link>
        }
      />

      {notice && (
        <div className="mb-3">
          <ErrorAlert
            variant="success"
            message={notice}
            onDismiss={() => setNotice('')}
          />
        </div>
      )}

      {(error || actionError) && (
        <div className="mb-3">
          <ErrorAlert
            message={actionError || error}
            onDismiss={() => {
              setActionError('')
              list.setError('')
            }}
          />
        </div>
      )}

      <div className="axc-card">
        <div className="axc-card__header">
          <div className="axc-toolbar">
            <SearchInput
              value={list.searchInput}
              onChange={list.setSearchInput}
              placeholder="Search by name, email, phone or company"
            />
            <select
              className="form-select axc-toolbar__filter"
              aria-label="Filter by status"
              value={list.status}
              onChange={(event) => list.setStatusFilter(event.target.value)}
            >
              {statusOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <span className="axc-cell-sub">
            {list.totalElements} record{list.totalElements === 1 ? '' : 's'}
          </span>
        </div>

        {loading ? (
          <LoadingState label="Loading leads…" />
        ) : items.length === 0 ? (
          <EmptyState
            title="No leads found"
            message={
              list.searchInput || list.status
                ? 'Try a different search term or clear the status filter.'
                : 'New leads will appear here as they are captured.'
            }
          />
        ) : (
          <div className="axc-table-wrap">
            <table className="axc-table">
              <caption className="visually-hidden">Lead records</caption>
              <thead>
                <tr>
                  <th scope="col">Code</th>
                  <th scope="col">Lead</th>
                  <th scope="col">Company</th>
                  <th scope="col">Source</th>
                  <th scope="col">Status</th>
                  <th scope="col">Priority</th>
                  <th scope="col" className="axc-num">
                    Expected value
                  </th>
                  <th scope="col">Assigned to</th>
                  <th scope="col">Created</th>
                  <th scope="col" className="axc-actions">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody>
                {items.map((lead) => (
                  <tr key={lead.id}>
                    <td className="axc-mono">{lead.leadCode}</td>
                    <td>
                      <Link
                        className="axc-cell-strong d-block"
                        to={`/leads/${lead.id}/edit`}
                      >
                        {lead.name}
                      </Link>
                      <span className="axc-cell-sub">
                        {lead.email}
                        {lead.phone ? ` · ${lead.phone}` : ''}
                      </span>
                    </td>
                    <td>{lead.companyName || '—'}</td>
                    <td>{lead.source || '—'}</td>
                    <td>
                      <StatusBadge value={lead.status} />
                    </td>
                    <td>
                      <StatusBadge value={lead.priority} />
                    </td>
                    <td className="axc-num">{formatCurrency(lead.expectedValue)}</td>
                    <td>{lead.assignedToName || '—'}</td>
                    <td>{formatDate(lead.createdDate)}</td>
                    <td className="axc-actions">
                      <button
                        type="button"
                        className="btn btn-sm btn-link-axc"
                        onClick={() => navigate(`/leads/${lead.id}/edit`)}
                      >
                        Edit
                      </button>
                      {lead.status !== 'CONVERTED' && (
                        <button
                          type="button"
                          className="btn btn-sm btn-link-axc"
                          onClick={() => setConverting(lead)}
                        >
                          Convert
                        </button>
                      )}
                      <button
                        type="button"
                        className="btn btn-sm btn-link-danger"
                        onClick={() => setPendingDelete(lead)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination
          page={list.page}
          totalPages={list.totalPages}
          totalElements={list.totalElements}
          size={list.size}
          onPageChange={list.setPage}
        />
      </div>

      {converting && (
        <ConvertLeadModal
          lead={converting}
          onClose={() => setConverting(null)}
          onConverted={handleConverted}
        />
      )}

      {pendingDelete && (
        <ConfirmDialog
          title="Delete lead"
          message={`Delete the lead "${pendingDelete.name}"? Converted leads keep their audit history but will no longer appear in the working list.`}
          confirmLabel="Delete"
          busy={deleting}
          onConfirm={confirmDelete}
          onCancel={() => setPendingDelete(null)}
        />
      )}
    </div>
  )
}
