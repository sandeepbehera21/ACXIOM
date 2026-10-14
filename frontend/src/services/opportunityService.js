import api from './api'

function listParams({ search, stage, status, page = 0, size = 10 }) {
  const params = { page, size }
  if (search) params.search = search
  if (stage) params.stage = stage
  if (status) params.status = status
  return params
}

export async function listOpportunities(options = {}) {
  const { data } = await api.get('/api/opportunities', {
    params: listParams(options),
  })
  return data
}

export async function getOpportunity(id) {
  const { data } = await api.get(`/api/opportunities/${id}`)
  return data
}

export async function createOpportunity(payload) {
  const { data } = await api.post('/api/opportunities', payload)
  return data
}

export async function updateOpportunity(id, payload) {
  const { data } = await api.put(`/api/opportunities/${id}`, payload)
  return data
}

export async function deleteOpportunity(id) {
  const { data } = await api.delete(`/api/opportunities/${id}`)
  return data
}
