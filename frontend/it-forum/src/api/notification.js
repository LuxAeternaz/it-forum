import request from './request'

export function getNotifications({ type, page = 1, size = 20 } = {}) {
  return request.get('/notifications', { params: { type, page, size } })
}

export function getUnreadCount() {
  return request.get('/notifications/unread-count')
}

export function markRead(id) {
  return request.put(`/notifications/${id}/read`)
}

export function markAllRead() {
  return request.put('/notifications/read-all')
}
