import request from './request'

export function follow(followeeId, followType) {
  return request.post('/follows', { followeeId, followType })
}

export function unfollow(followeeId, followType) {
  return request.delete('/follows', { data: { followeeId, followType } })
}

export function getFollowers(userId, page = 1, size = 20) {
  return request.get('/follows/followers', { params: { userId, page, size } })
}

export function getFollowing(userId, page = 1, size = 20) {
  return request.get('/follows/following', { params: { userId, page, size } })
}

export function checkFollow(followeeId, followType) {
  return request.get('/follows/status', { params: { followeeId, followType } })
}
