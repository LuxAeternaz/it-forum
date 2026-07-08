import request from './request'

export function getPosts({ categoryId, sort, page = 1, size = 20 } = {}) {
  return request.get('/posts', { params: { categoryId, sort, page, size } })
}

export function getPostDetail(id) {
  return request.get(`/posts/${id}`)
}

export function createPost(data) {
  return request.post('/posts', data)
}

export function updatePost(id, data) {
  return request.put(`/posts/${id}`, data)
}

export function deletePost(id) {
  return request.delete(`/posts/${id}`)
}
