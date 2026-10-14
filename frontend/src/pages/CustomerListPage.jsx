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
import { deleteCustomer, listCustomers } from '../services/customerService'
import { extractApiError } from '../services/api'
import { CUSTOMER_STATUSES, PAGE_SIZE } from '../utils/constants'
import { formatDate, formatEnumLabel } from '../utils/format'

const statusOptions = [
  { value: '', label: 'All statuses' },
  ...CUSTOMER_STATUSES.map((value) => ({
    value,
    label: formatEnumLabel(value),
  })),
]

export default function CustomerListPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const list = usePagedList({ fetcher: listCustomers, size: PAGE_SIZE })

  const [pendingDelete, setPendingDelete] = useState(null)
  const [deleting, setDeleting] = useState(false)
  const [notice, setNotice] = useState(location.state?.notice || '')
  const [actionError, setActionError] = useState('')

  async function confirmDelete() {
    if (!pendingDelete) return
    setDeleting(true)
    setActionError('')
    try {
      await deleteCustomer(pendingDelete.id)
      setNotice(`"${pendingDelete.name}" was deactivated.`)
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
        title="Customers"
        subtitle="Accounts owned by the sales team."
        action={
          <Link className="btn btn-sm btn-axc-primary" to="/customers/new">
            New customer
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
          <LoadingState label="Loading customers…" />
        ) : items.length === 0 ? (
          <EmptyState
            title="No customers found"
            message={
              list.searchInput || list.status
                ? 'Try a different search term or clear the status filter.'
                : 'Create the first customer to get started.'
            }
          />
        ) : (
          <div className="axc-table-wrap">
            <table className="axc-table">
              <caption className="visually-hidden">Customer records</caption>
              <thead>
                <tr>
                  <th scope="col">Code</th>
                  <th scope="col">Customer</th>
                  <th scope="col">Company</th>
                  <th scope="col">City</th>
                  <th scope="col">Status</th>
                  <th scope="col">Owner</th>
                  <th scope="col">Created</th>
                  <th scope="col" className="axc-actions">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody>
                {items.map((customer) => (
                  <tr key={customer.id}>
                    <td className="axc-mono">{customer.customerCode}</td>
                    <td>
                      <Link
                        className="axc-cell-strong d-block"
                        to={`/customers/${customer.id}/edit`}
                      >
                        {customer.name}
                      </Link>
                      <span className="axc-cell-sub">
                        {customer.email}
                        {customer.phone ? ` · ${customer.phone}` : ''}
                      </span>
                    </td>
                    <td>{customer.companyName || '—'}</td>
                    <td>
                      {customer.city || '—'}
                      {customer.state ? `, ${customer.state}` : ''}
                    </td>
                    <td>
                      <StatusBadge value={customer.status} />
                    </td>
                    <td>{customer.ownerName || '—'}</td>
                    <td>{formatDate(customer.createdDate)}</td>
                    <td className="axc-actions">
                      <button
                        type="button"
                        className="btn btn-sm btn-link-axc"
                        onClick={() =>
                          navigate(`/customers/${customer.id}/edit`)
                        }
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        className="btn btn-sm btn-link-danger"
                        onClick={() => setPendingDelete(customer)}
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
          title="Deactivate customer"
          message={`Deactivating "${pendingDelete.name}" keeps the record and its history for reporting, but marks it inactive so it no longer appears as an active account.`}
          confirmLabel="Deactivate"
          busy={deleting}
          onConfirm={confirmDelete}
          onCancel={() => setPendingDelete(null)}
        />
      )}
    </div>
  )
}
