import request from './request'

export function getDraft(postId) {
  return request.get('/drafts', { params: { postId } })
}

export function saveDraft(data) {
  return request.put('/drafts', data)
}

export function deleteDraft(postId) {
  return request.delete('/drafts', { params: { postId } })
}
