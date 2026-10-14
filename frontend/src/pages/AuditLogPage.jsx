import EmptyState from '../components/EmptyState'
import ErrorAlert from '../components/ErrorAlert'
import LoadingState from '../components/LoadingState'
import PageHeader from '../components/PageHeader'
import Pagination from '../components/Pagination'
import StatusBadge from '../components/StatusBadge'
import { usePagedList } from '../components/usePagedList'
import { listAuditLogs } from '../services/auditService'
import { AUDIT_PAGE_SIZE } from '../utils/constants'
import { formatDateTime } from '../utils/format'

/** Audit rows are read-only; nothing on this page writes. */
function auditUser(log) {
  return (
    log.userName ||
    log.user?.name ||
    log.user?.email ||
    log.userEmail ||
    log.username ||
    '—'
  )
}

export default function AuditLogPage() {
  const list = usePagedList({ fetcher: listAuditLogs, size: AUDIT_PAGE_SIZE })
  const { items, loading, error } = list

  return (
    <div>
      <PageHeader
        title="Audit Log"
        subtitle="Append-only record of actions taken in the system."
      />

      {error && (
        <div className="mb-3">
          <ErrorAlert message={error} onDismiss={() => list.setError('')} />
        </div>
      )}

      <div className="axc-card">
        <div className="axc-card__header">
          <h2 className="axc-section-title">Recent activity</h2>
          <span className="axc-cell-sub">
            {list.totalElements} entr{list.totalElements === 1 ? 'y' : 'ies'}
          </span>
        </div>

        {loading ? (
          <LoadingState label="Loading audit log…" />
        ) : items.length === 0 ? (
          <EmptyState
            title="No audit entries"
            message="Activity is recorded as users sign in and change records."
          />
        ) : (
          <div className="axc-table-wrap">
            <table className="axc-table">
              <caption className="visually-hidden">Audit log entries</caption>
              <thead>
                <tr>
                  <th scope="col">Timestamp</th>
                  <th scope="col">User</th>
                  <th scope="col">Module</th>
                  <th scope="col">Action</th>
                  <th scope="col">Entity</th>
                  <th scope="col">Record ID</th>
                  <th scope="col">Result</th>
                </tr>
              </thead>
              <tbody>
                {items.map((log) => (
                  <tr key={log.id}>
                    <td>{formatDateTime(log.createdDate || log.timestamp)}</td>
                    <td>{auditUser(log)}</td>
                    <td>{log.module || '—'}</td>
                    <td>
                      <StatusBadge value={log.action} />
                    </td>
                    <td>{log.entityName || log.entity || '—'}</td>
                    <td className="axc-mono">{log.recordId || '—'}</td>
                    <td>
                      {log.result ? (
                        <StatusBadge value={log.result} />
                      ) : (
                        <span className="axc-cell-sub">—</span>
                      )}
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
    </div>
  )
}
