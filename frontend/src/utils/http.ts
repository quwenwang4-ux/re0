import axios, { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'

export const TOKEN_KEY = 'seafish_access_token'
const http = axios.create({ baseURL: '/api', timeout: 15_000 })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<{ message?: string }>) => {
    const status = error.response?.status
    const message = error.response?.data?.message || '网络连接异常，请稍后重试'
    if (status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem('seafish_user')
      localStorage.removeItem('seafish_roles')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.assign(`/login?redirect=${encodeURIComponent(window.location.pathname)}`)
      }
    } else ElMessage.error(message)
    return Promise.reject(error)
  },
)

export default http
