import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器 — 携带 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['token'] = token
  }
  return config
}, error => Promise.reject(error))

// 响应拦截器 — 统一处理错误
request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code && data.code !== 200) {
      // 401 未登录 → 跳转登录页
      if (data.code === 401) {
        localStorage.removeItem('token')
        window.location.hash = '#/login'
        return Promise.reject(new Error(data.msg || '未登录'))
      }
      ElMessage.error(data.msg || '请求失败')
      return Promise.reject(new Error(data.msg))
    }
    return response
  },
  error => {
    // HTTP 级别的 401
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.hash = '#/login'
      return Promise.reject(error)
    }
    const msg = error.response?.data?.msg || error.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
