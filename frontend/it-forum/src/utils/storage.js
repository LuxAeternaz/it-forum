export function getToken() {
  return localStorage.getItem('token')
}

export function setToken(token) {
  localStorage.setItem('token', token)
}

export function removeToken() {
  localStorage.removeItem('token')
}

export function getRefreshToken() {
  return localStorage.getItem('refreshToken')
}

export function setRefreshToken(token) {
  localStorage.setItem('refreshToken', token)
}

export function removeRefreshToken() {
  localStorage.removeItem('refreshToken')
}

export function getUser() {
  const u = localStorage.getItem('user')
  return u ? JSON.parse(u) : null
}

export function setUser(user) {
  localStorage.setItem('user', JSON.stringify(user))
}

export function removeUser() {
  localStorage.removeItem('user')
}

export function clearAuth() {
  removeToken()
  removeRefreshToken()
  removeUser()
}
