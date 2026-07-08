import request from './request'

export function polishText(content, instruction) {
  return request.post('/ai/polish', { content, instruction })
}

export function getAgentStatus() {
  return request.get('/ai/agent/status')
}
