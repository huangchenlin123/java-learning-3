import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getUserInfo } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const menus = ref([])

  async function login(username, password) {
    const res = await loginApi({ username, password })
    const data = res.data.data
    token.value = data.token || data.tokenValue || data
    localStorage.setItem('token', token.value)
    await fetchUserInfo()
  }

  async function fetchUserInfo() {
    const res = await getUserInfo()
    const data = res.data.data
    userInfo.value = data.user || data
    menus.value = data.menus || []
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    menus.value = []
    localStorage.removeItem('token')
  }

  return { token, userInfo, menus, login, fetchUserInfo, logout }
})
