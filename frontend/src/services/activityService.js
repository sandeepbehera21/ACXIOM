import api from './api'

export async function listActivities(params = {}) {
  const response = await api.get('/api/activities', { params })
  return response.data
}

export async function getActivity(id) {
  const response = await api.get(`/api/activities/${id}`)
  return response.data
}

export async function createActivity(payload) {
  const response = await api.post('/api/activities', payload)
  return response.data
}

export async function updateActivity(id, payload) {
  const response = await api.put(`/api/activities/${id}`, payload)
  return response.data
}

export async function deleteActivity(id) {
  await api.delete(`/api/activities/${id}`)
}
