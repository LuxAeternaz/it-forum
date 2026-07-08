import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken, getRefreshToken, setRefreshToken, removeRefreshToken, getUser, setUser, removeUser, clearAuth } from '../utils/storage'
import * as authApi from '../api/auth'
import * as userApi from '../api/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken() || '')
  const refreshToken = ref(getRefreshToken() || '')
  const user = ref(getUser() || null)

  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => user.value?.id)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function saveAuth(loginResponse) {
    token.value = loginResponse.token
    refreshToken.value = loginResponse.refreshToken
    user.value = { id: loginResponse.userId, username: loginResponse.username, avatarUrl: loginResponse.avatarUrl, role: loginResponse.role }
    setToken(loginResponse.token)
    setRefreshToken(loginResponse.refreshToken)
    setUser(user.value)
  }

  async function login(email, password) {
    const res = await authApi.login(email, password)
    saveAuth(res)
    return res
  }

  async function register(username, email, password) {
    const res = await authApi.register(username, email, password)
    saveAuth(res)
    return res
  }

  async function doRefreshToken() {
    const rt = refreshToken.value || getRefreshToken()
    if (!rt) return false
    try {
      const res = await authApi.refreshToken(rt)
      token.value = res.token
      refreshToken.value = res.refreshToken
      user.value = { id: res.userId, username: res.username, avatarUrl: res.avatarUrl, role: res.role }
      setToken(res.token)
      setRefreshToken(res.refreshToken)
      setUser(user.value)
      return true
    } catch {
      return false
    }
  }

  async function fetchProfile() {
    const res = await userApi.getMyProfile()
    user.value = res
    setUser(res)
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    user.value = null
    clearAuth()
    window.location.href = '/login'
  }

  return { token, refreshToken, user, isLoggedIn, userId, isAdmin, login, register, doRefreshToken, fetchProfile, logout }
})
