import request from './request'

export function bookmark(postId) {
  return request.post(`/bookmarks/${postId}`)
}

export function unbookmark(postId) {
  return request.delete(`/bookmarks/${postId}`)
}

export function getBookmarks(page = 1, size = 20) {
  return request.get('/bookmarks', { params: { page, size } })
}

export function checkBookmarked(postId) {
  return request.get(`/bookmarks/${postId}/status`)
}
