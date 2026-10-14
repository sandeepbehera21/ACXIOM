import api from './api'

export async function getPipelineReport() {
  const { data } = await api.get('/api/reports/pipeline')
  return data
}
