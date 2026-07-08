import request from './request'

export function getMyProfile() {
  return request.get('/users/me')
}

export function updateProfile(data) {
  return request.put('/users/me', data)
}

export function changePassword(oldPassword, newPassword) {
  return request.put('/users/me/password', { oldPassword, newPassword })
}

export function updateAvatar(avatarUrl) {
  return request.put('/users/me/avatar', { avatarUrl })
}

export function getUserProfile(userId) {
  return request.get(`/users/${userId}`)
}

export function getUserPosts(userId, page = 1, size = 20) {
  return request.get(`/users/${userId}/posts`, { params: { page, size } })
}

export function getUserComments(userId, page = 1, size = 20) {
  return request.get(`/users/${userId}/comments`, { params: { page, size } })
}
