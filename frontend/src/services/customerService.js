import api from './api'

function listParams({ search, status, page = 0, size = 10 }) {
  const params = { page, size }
  if (search) params.search = search
  if (status) params.status = status
  return params
}

export async function listCustomers(options = {}) {
  const { data } = await api.get('/api/customers', {
    params: listParams(options),
  })
  return data
}

export async function getCustomer(id) {
  const { data } = await api.get(`/api/customers/${id}`)
  return data
}

export async function createCustomer(payload) {
  const { data } = await api.post('/api/customers', payload)
  return data
}

export async function updateCustomer(id, payload) {
  const { data } = await api.put(`/api/customers/${id}`, payload)
  return data
}

export async function deleteCustomer(id) {
  const { data } = await api.delete(`/api/customers/${id}`)
  return data
}
