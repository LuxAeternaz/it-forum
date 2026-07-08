import request from './request'

export function getDashboard() {
  return request.get('/admin/dashboard')
}

export function getAdminUsers(page = 1, size = 20) {
  return request.get('/admin/users', { params: { page, size } })
}

export function updateUserRole(userId, role) {
  return request.put(`/admin/users/${userId}/role`, { role })
}

export function updateUserStatus(userId, status) {
  return request.put(`/admin/users/${userId}/status`, { status })
}

export function getAdminPosts(page = 1, size = 20, keyword = '') {
  return request.get('/admin/posts', { params: { page, size, keyword } })
}

export function deleteAdminPost(postId) {
  return request.delete(`/admin/posts/${postId}`)
}

export function getAdminCategories() {
  return request.get('/admin/categories')
}

export function createCategory(data) {
  return request.post('/admin/categories', data)
}

export function updateCategory(id, data) {
  return request.put(`/admin/categories/${id}`, data)
}

export function deleteCategory(id) {
  return request.delete(`/admin/categories/${id}`)
}

export function getModerationLogs(page = 1, size = 20) {
  return request.get('/admin/moderation/logs', { params: { page, size } })
}

export function reviewModeration(logId) {
  return request.put(`/admin/moderation/${logId}/review`)
}

export function restoreModeration(logId) {
  return request.put(`/admin/moderation/${logId}/restore`)
}

export function reviewAllApproved() {
  return request.post('/admin/moderation/review-all-approved')
}

export function getPendingCount() {
  return request.get('/admin/moderation/pending-count')
}
