import request from './request'

export function like(targetType, targetId) {
  return request.post('/likes', { targetType, targetId })
}

export function unlike(targetType, targetId) {
  return request.delete('/likes', { data: { targetType, targetId } })
}

export function getLikeStatus(targetType, targetIds) {
  return request.get('/likes/status', { params: { targetType, targetIds: targetIds.join(',') } })
}
