import request from './request'

export function getMentionSuggestions(q, limit = 8) {
  return request.get('/mentions/suggest', { params: { q, limit } })
}
