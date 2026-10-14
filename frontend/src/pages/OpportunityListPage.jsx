import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import ConfirmDialog from '../components/ConfirmDialog'
import EmptyState from '../components/EmptyState'
import ErrorAlert from '../components/ErrorAlert'
import LoadingState from '../components/LoadingState'
import PageHeader from '../components/PageHeader'
import Pagination from '../components/Pagination'
import SearchInput from '../components/SearchInput'
import StatusBadge from '../components/StatusBadge'
import { usePagedList } from '../components/usePagedList'
import { deleteOpportunity, listOpportunities } from '../services/opportunityService'
import { extractApiError } from '../services/api'
import { OPPORTUNITY_STAGES, PAGE_SIZE } from '../utils/constants'
import { formatCurrency, formatDate, formatEnumLabel } from '../utils/format'

const stageOptions = [
  { value: '', label: 'All stages' },
  ...OPPORTUNITY_STAGES.map((value) => ({
    value,
    label: formatEnumLabel(value),
  })),
]

export default function OpportunityListPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const list = usePagedList({ fetcher: listOpportunities, size: PAGE_SIZE })

  const [pendingDelete, setPendingDelete] = useState(null)
  const [deleting, setDeleting] = useState(false)
  const [notice, setNotice] = useState(location.state?.notice || '')
  const [actionError, setActionError] = useState('')

  async function confirmDelete() {
    if (!pendingDelete) return
    setDeleting(true)
    setActionError('')
    try {
      await deleteOpportunity(pendingDelete.id)
      setNotice(`Opportunity "${pendingDelete.name}" was removed.`)
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
        title="Opportunities"
        subtitle="Open and closed deals across the pipeline."
        action={
          <Link className="btn btn-sm btn-axc-primary" to="/opportunities/new">
            New opportunity
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
              placeholder="Search by opportunity or customer"
            />
            <select
              className="form-select axc-toolbar__filter"
              aria-label="Filter by stage"
              value={list.stage}
              onChange={(event) => list.setStageFilter(event.target.value)}
            >
              {stageOptions.map((option) => (
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
          <LoadingState label="Loading opportunities…" />
        ) : items.length === 0 ? (
          <EmptyState
            title="No opportunities found"
            message={
              list.searchInput || list.stage
                ? 'Try a different search term or clear the stage filter.'
                : 'Deals appear here once they are created for a customer.'
            }
          />
        ) : (
          <div className="axc-table-wrap">
            <table className="axc-table">
              <caption className="visually-hidden">Opportunity records</caption>
              <thead>
                <tr>
                  <th scope="col">Opportunity</th>
                  <th scope="col">Customer</th>
                  <th scope="col" className="axc-num">
                    Amount
                  </th>
                  <th scope="col">Stage</th>
                  <th scope="col" className="axc-num">
                    Probability
                  </th>
                  <th scope="col" className="axc-num">
                    Weighted
                  </th>
                  <th scope="col">Expected close</th>
                  <th scope="col">Owner</th>
                  <th scope="col" className="axc-actions">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody>
                {items.map((opportunity) => (
                  <tr key={opportunity.id}>
                    <td>
                      <Link
                        className="axc-cell-strong d-block"
                        to={`/opportunities/${opportunity.id}/edit`}
                      >
                        {opportunity.name}
                      </Link>
                      {opportunity.source && (
                        <span className="axc-cell-sub">
                          {opportunity.source}
                        </span>
                      )}
                    </td>
                    <td>{opportunity.customerName || '—'}</td>
                    <td className="axc-num">
                      {formatCurrency(opportunity.amount)}
                    </td>
                    <td>
                      <StatusBadge value={opportunity.stage} />
                    </td>
                    <td className="axc-num">
                      {opportunity.probability === null ||
                      opportunity.probability === undefined
                        ? '—'
                        : `${opportunity.probability}%`}
                    </td>
                    <td className="axc-num">
                      {formatCurrency(opportunity.weightedAmount)}
                    </td>
                    <td>{formatDate(opportunity.expectedCloseDate)}</td>
                    <td>{opportunity.ownerName || '—'}</td>
                    <td className="axc-actions">
                      <button
                        type="button"
                        className="btn btn-sm btn-link-axc"
                        onClick={() =>
                          navigate(`/opportunities/${opportunity.id}/edit`)
                        }
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        className="btn btn-sm btn-link-danger"
                        onClick={() => setPendingDelete(opportunity)}
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

      {pendingDelete && (
        <ConfirmDialog
          title="Delete opportunity"
          message={`Delete the opportunity "${pendingDelete.name}"? Pipeline totals are recalculated straight away.`}
          confirmLabel="Delete"
          busy={deleting}
          onConfirm={confirmDelete}
          onCancel={() => setPendingDelete(null)}
        />
      )}
    </div>
  )
}
