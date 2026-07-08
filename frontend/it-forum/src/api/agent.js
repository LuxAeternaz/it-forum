import request from './request'

export function getAgents() {
  return request.get('/agents')
}

export function getMyAgents() {
  return request.get('/agents/my')
}

export function getAgent(id) {
  return request.get(`/agents/${id}`)
}

export function createAgent(data) {
  return request.post('/agents', data)
}

export function updateAgent(id, data) {
  return request.put(`/agents/${id}`, data)
}

export function deleteAgent(id) {
  return request.delete(`/agents/${id}`)
}
