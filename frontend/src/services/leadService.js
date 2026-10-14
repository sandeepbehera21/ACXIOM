import api from './api'

function listParams({ search, status, page = 0, size = 10 }) {
  const params = { page, size }
  if (search) params.search = search
  if (status) params.status = status
  return params
}

export async function listLeads(options = {}) {
  const { data } = await api.get('/api/leads', { params: listParams(options) })
  return data
}

export async function getLead(id) {
  const { data } = await api.get(`/api/leads/${id}`)
  return data
}

export async function createLead(payload) {
  const { data } = await api.post('/api/leads', payload)
  return data
}

export async function updateLead(id, payload) {
  const { data } = await api.put(`/api/leads/${id}`, payload)
  return data
}

export async function deleteLead(id) {
  const { data } = await api.delete(`/api/leads/${id}`)
  return data
}

/**
 * Converts a lead into a customer, optionally creating an opportunity.
 * Returns { customer, opportunity }.
 */
export async function convertLead(id, payload) {
  const { data } = await api.post(`/api/leads/${id}/convert`, payload)
  return data
}
