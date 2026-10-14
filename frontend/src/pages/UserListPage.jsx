import { useEffect, useState } from 'react'
import EmptyState from '../components/EmptyState'
import ErrorAlert from '../components/ErrorAlert'
import LoadingState from '../components/LoadingState'
import Modal from '../components/Modal'
import PageHeader from '../components/PageHeader'
import SearchInput from '../components/SearchInput'
import {
  createUser,
  listUsers,
  unlockUser,
  updateUserRole,
  updateUserStatus,
} from '../services/userService'
import { extractApiError } from '../services/api'
import { useAuth } from '../services/AuthContext'
import { ROLES } from '../utils/constants'
import { formatDate, formatEnumLabel } from '../utils/format'

export default function UserListPage() {
  const { user: currentUser } = useAuth()
  const isAdmin = currentUser?.role === ROLES.ADMIN

  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [notice, setNotice] = useState('')

  // Create User Modal
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [createSubmitting, setCreateSubmitting] = useState(false)
  const [createError, setCreateError] = useState('')
  const [createForm, setCreateForm] = useState({
    name: '',
    email: '',
    password: '',
    role: 'SALES_EXECUTIVE',
  })

  // Role Change Modal
  const [roleModalUser, setRoleModalUser] = useState(null)
  const [selectedRole, setSelectedRole] = useState('')
  const [roleSubmitting, setRoleSubmitting] = useState(false)
  const [roleError, setRoleError] = useState('')

  useEffect(() => {
    let active = true
    async function fetchUsers() {
      setLoading(true)
      setError('')
      try {
        const data = await listUsers({ search })
        if (active) setItems(data)
      } catch (err) {
        if (active) setError(extractApiError(err).message)
      } finally {
        if (active) setLoading(false)
      }
    }
    fetchUsers()
    return () => {
      active = false
    }
  }, [search])

  async function loadData() {
    try {
      const data = await listUsers({ search })
      setItems(data)
    } catch (err) {
      setError(extractApiError(err).message)
    }
  }

  async function handleCreateUser(e) {
    e.preventDefault()
    setCreateSubmitting(true)
    setCreateError('')
    try {
      await createUser(createForm)
      setNotice(`User ${createForm.email} created successfully.`)
      setShowCreateModal(false)
      setCreateForm({
        name: '',
        email: '',
        password: '',
        role: 'SALES_EXECUTIVE',
      })
      loadData()
    } catch (err) {
      setCreateError(extractApiError(err).message)
    } finally {
      setCreateSubmitting(false)
    }
  }

  async function handleToggleStatus(targetUser) {
    try {
      await updateUserStatus(targetUser.id, !targetUser.active)
      setNotice(
        `User ${targetUser.email} was ${
          !targetUser.active ? 'activated' : 'deactivated'
        }.`
      )
      loadData()
    } catch (err) {
      setError(extractApiError(err).message)
    }
  }

  async function handleUnlock(targetUser) {
    try {
      await unlockUser(targetUser.id)
      setNotice(`User ${targetUser.email} has been unlocked.`)
      loadData()
    } catch (err) {
      setError(extractApiError(err).message)
    }
  }

  async function handleSaveRole(e) {
    e.preventDefault()
    if (!roleModalUser || !selectedRole) return
    setRoleSubmitting(true)
    setRoleError('')
    try {
      await updateUserRole(roleModalUser.id, selectedRole)
      setNotice(`Role updated for ${roleModalUser.email}.`)
      setRoleModalUser(null)
      loadData()
    } catch (err) {
      setRoleError(extractApiError(err).message)
    } finally {
      setRoleSubmitting(false)
    }
  }

  return (
    <div>
      <PageHeader
        title="User Administration"
        subtitle="Manage user accounts, roles, and security lockouts."
        action={
          isAdmin && (
            <button
              type="button"
              className="btn btn-sm btn-axc-primary"
              onClick={() => {
                setCreateError('')
                setShowCreateModal(true)
              }}
            >
              + Create User
            </button>
          )
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

      <div className="axc-filter-bar mb-3">
        <div style={{ maxWidth: '300px' }}>
          <SearchInput
            value={search}
            onChange={(val) => setSearch(val)}
            placeholder="Search name or email…"
          />
        </div>
      </div>

      <div className="axc-card">
        {loading ? (
          <LoadingState label="Loading users…" />
        ) : items.length === 0 ? (
          <EmptyState
            title="No users found"
            message="No user records matched your search query."
          />
        ) : (
          <div className="axc-table-wrap">
            <table className="axc-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Lockout</th>
                  <th>Created</th>
                  {isAdmin && <th className="text-end">Actions</th>}
                </tr>
              </thead>
              <tbody>
                {items.map((row) => (
                  <tr key={row.id}>
                    <td className="fw-medium">{row.name}</td>
                    <td>{row.email}</td>
                    <td>
                      <span className="badge bg-primary text-uppercase">
                        {formatEnumLabel(row.role)}
                      </span>
                    </td>
                    <td>
                      {row.active ? (
                        <span className="badge bg-success">Active</span>
                      ) : (
                        <span className="badge bg-danger">Inactive</span>
                      )}
                    </td>
                    <td>
                      {row.lockedOut ? (
                        <span className="badge bg-warning text-dark">
                          Locked Out
                        </span>
                      ) : (
                        <span className="text-muted">Normal</span>
                      )}
                    </td>
                    <td>{formatDate(row.createdDate)}</td>
                    {isAdmin && (
                      <td className="text-end">
                        <div className="btn-group btn-group-sm">
                          {row.lockedOut && (
                            <button
                              type="button"
                              className="btn btn-outline-warning btn-sm"
                              onClick={() => handleUnlock(row)}
                            >
                              Unlock
                            </button>
                          )}
                          <button
                            type="button"
                            className="btn btn-outline-secondary btn-sm"
                            disabled={row.id === currentUser?.id}
                            onClick={() => {
                              setRoleModalUser(row)
                              setSelectedRole(row.role)
                              setRoleError('')
                            }}
                          >
                            Change Role
                          </button>
                          <button
                            type="button"
                            className={`btn btn-sm ${
                              row.active
                                ? 'btn-outline-danger'
                                : 'btn-outline-success'
                            }`}
                            disabled={row.id === currentUser?.id}
                            onClick={() => handleToggleStatus(row)}
                          >
                            {row.active ? 'Deactivate' : 'Activate'}
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Create User Modal */}
      <Modal
        open={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        title="Create New User"
      >
        <form onSubmit={handleCreateUser}>
          {createError && (
            <div className="mb-3">
              <ErrorAlert
                message={createError}
                onDismiss={() => setCreateError('')}
              />
            </div>
          )}

          <div className="mb-3">
            <label className="form-label required">Full Name</label>
            <input
              type="text"
              className="form-control form-control-sm"
              required
              value={createForm.name}
              onChange={(e) =>
                setCreateForm({ ...createForm, name: e.target.value })
              }
            />
          </div>

          <div className="mb-3">
            <label className="form-label required">Email</label>
            <input
              type="email"
              className="form-control form-control-sm"
              required
              value={createForm.email}
              onChange={(e) =>
                setCreateForm({ ...createForm, email: e.target.value })
              }
            />
          </div>

          <div className="mb-3">
            <label className="form-label required">Password</label>
            <input
              type="password"
              className="form-control form-control-sm"
              required
              value={createForm.password}
              onChange={(e) =>
                setCreateForm({ ...createForm, password: e.target.value })
              }
              placeholder="Min 8 chars, uppercase, lowercase, digit, special char"
            />
          </div>

          <div className="mb-3">
            <label className="form-label required">Role</label>
            <select
              className="form-select form-select-sm"
              value={createForm.role}
              onChange={(e) =>
                setCreateForm({ ...createForm, role: e.target.value })
              }
            >
              <option value="SALES_EXECUTIVE">Sales Executive</option>
              <option value="MANAGER">Manager</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>

          <div className="d-flex justify-content-end gap-2">
            <button
              type="button"
              className="btn btn-sm btn-outline-secondary"
              onClick={() => setShowCreateModal(false)}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-sm btn-axc-primary"
              disabled={createSubmitting}
            >
              {createSubmitting ? 'Creating…' : 'Create User'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Role Change Modal */}
      <Modal
        open={Boolean(roleModalUser)}
        onClose={() => setRoleModalUser(null)}
        title="Change User Role"
      >
        <form onSubmit={handleSaveRole}>
          {roleError && (
            <div className="mb-3">
              <ErrorAlert
                message={roleError}
                onDismiss={() => setRoleError('')}
              />
            </div>
          )}

          <p className="small text-muted mb-3">
            Changing role for <strong>{roleModalUser?.email}</strong>.
          </p>

          <div className="mb-3">
            <label className="form-label required">Role</label>
            <select
              className="form-select form-select-sm"
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value)}
            >
              <option value="SALES_EXECUTIVE">Sales Executive</option>
              <option value="MANAGER">Manager</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>

          <div className="d-flex justify-content-end gap-2">
            <button
              type="button"
              className="btn btn-sm btn-outline-secondary"
              onClick={() => setRoleModalUser(null)}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-sm btn-axc-primary"
              disabled={roleSubmitting}
            >
              {roleSubmitting ? 'Saving…' : 'Update Role'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
