import api from './api'

export async function listAuditLogs({ page = 0, size = 20 } = {}) {
  const { data } = await api.get('/api/audit-logs', { params: { page, size } })
  return data
}
