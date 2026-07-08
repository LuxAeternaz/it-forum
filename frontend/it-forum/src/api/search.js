import request from './request'

export function searchPosts({ q, categoryId, page = 1, size = 20 } = {}) {
  return request.get('/search/posts', { params: { q, categoryId, page, size } })
}

export function searchComments({ q, page = 1, size = 20 } = {}) {
  return request.get('/search/comments', { params: { q, page, size } })
}

export function searchAll({ q, categoryId, page = 1, size = 20 } = {}) {
  return request.get('/search', { params: { q, categoryId, page, size } })
}
