import api from './api'

export async function listUsers(params = {}) {
  const response = await api.get('/api/users', { params })
  return response.data
}

export async function getUser(id) {
  const response = await api.get(`/api/users/${id}`)
  return response.data
}

export async function createUser(payload) {
  const response = await api.post('/api/users', payload)
  return response.data
}

export async function updateUserRole(id, role) {
  const response = await api.patch(`/api/users/${id}/role`, { role })
  return response.data
}

export async function updateUserStatus(id, active) {
  const response = await api.patch(`/api/users/${id}/status`, { active })
  return response.data
}

export async function unlockUser(id) {
  const response = await api.post(`/api/users/${id}/unlock`)
  return response.data
}
