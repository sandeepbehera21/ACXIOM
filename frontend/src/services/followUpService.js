import api from './api'

export async function listFollowUps(params = {}) {
  const response = await api.get('/api/followups', { params })
  return response.data
}

export async function getFollowUp(id) {
  const response = await api.get(`/api/followups/${id}`)
  return response.data
}

export async function createFollowUp(payload) {
  const response = await api.post('/api/followups', payload)
  return response.data
}

export async function updateFollowUp(id, payload) {
  const response = await api.put(`/api/followups/${id}`, payload)
  return response.data
}

export async function deleteFollowUp(id) {
  await api.delete(`/api/followups/${id}`)
}
