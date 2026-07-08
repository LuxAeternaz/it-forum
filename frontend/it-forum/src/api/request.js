import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, getRefreshToken, setToken, setRefreshToken, clearAuth } from '../utils/storage'
import * as authApi from './auth'

const request = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  timeout: 30000
})

let isRefreshing = false
let refreshSubscribers = []

function onRefreshed(newToken) {
  refreshSubscribers.forEach(cb => cb(newToken))
  refreshSubscribers = []
}

function addRefreshSubscriber(cb) {
  refreshSubscribers.push(cb)
}

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    return data.data
  },
  async (error) => {
    const originalRequest = error.config
    const status = error.response?.status

    // Token expired or invalid — try refresh
    if (status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      if (!isRefreshing) {
        isRefreshing = true
        const rt = getRefreshToken()
        if (rt) {
          try {
            const res = await authApi.refreshToken(rt)
            setToken(res.token)
            setRefreshToken(res.refreshToken)
            isRefreshing = false
            onRefreshed(res.token)
            originalRequest.headers.Authorization = `Bearer ${res.token}`
            return request(originalRequest)
          } catch {
            isRefreshing = false
            refreshSubscribers = []
            clearAuth()
            ElMessage.error('登录已过期，请重新登录')
            setTimeout(() => { window.location.href = '/login' }, 500)
            return Promise.reject(error)
          }
        } else {
          isRefreshing = false
          clearAuth()
          ElMessage.error('请先登录')
          setTimeout(() => { window.location.href = '/login' }, 500)
          return Promise.reject(error)
        }
      } else {
        // Another request is already refreshing — wait for it
        return new Promise((resolve) => {
          addRefreshSubscriber((newToken) => {
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            resolve(request(originalRequest))
          })
        })
      }
    }

    // Forbidden
    if (status === 403) {
      ElMessage.error(error.response.data?.message || '权限不足')
      return Promise.reject(error)
    }

    // Other errors — show server message if available
    const msg = error.response?.data?.message
    if (msg) {
      ElMessage.error(msg)
    } else if (!error.response) {
      ElMessage.error('网络连接失败，请检查网络')
    }
    return Promise.reject(error)
  }
)

export default request
