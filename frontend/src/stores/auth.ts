import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUserApi, loginApi, registerApi, updateProfileApi } from '../api/auth'
import type { LoginRequest, RegisterRequest, UpdateProfileRequest } from '../api/auth'
import type { User } from '../types/api'
import { TOKEN_KEY } from '../utils/http'

const USER_KEY = 'seafish_user'
const ROLES_KEY = 'seafish_roles'

function readJson<T>(key: string, fallback: T): T {
  try { const value = localStorage.getItem(key); return value ? JSON.parse(value) as T : fallback } catch { return fallback }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<User | null>(readJson(USER_KEY, null))
  const roles = ref<string[]>(readJson(ROLES_KEY, []))
  const loading = ref(false)
  const isLoggedIn = computed(() => Boolean(token.value))
  const displayName = computed(() => user.value?.nickname || user.value?.username || '游客')
  const isAdmin = computed(() => roles.value.includes('ADMIN'))
  const isVolunteer = computed(() => roles.value.includes('VOLUNTEER'))

  function persist() {
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    localStorage.setItem(ROLES_KEY, JSON.stringify(roles.value))
  }
  async function login(request: LoginRequest) {
    loading.value = true
    try {
      const { data } = await loginApi(request)
      token.value = data.data.accessToken; user.value = data.data.user; roles.value = data.data.roles; persist()
    } finally { loading.value = false }
  }
  async function register(request: RegisterRequest) {
    loading.value = true
    try { await registerApi(request); await login({ username: request.username, password: request.password }) }
    finally { loading.value = false }
  }
  async function refreshCurrentUser() {
    if (!token.value) return
    const { data } = await getCurrentUserApi(); user.value = data.data.user; roles.value = data.data.roles; persist()
  }
  async function updateProfile(request: UpdateProfileRequest) {
    const { data } = await updateProfileApi(request); user.value = data.data; persist()
  }
  function hasAnyRole(requiredRoles: string[]) { return requiredRoles.some((role) => roles.value.includes(role)) }
  function logout() {
    token.value = ''; user.value = null; roles.value = []
    localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY); localStorage.removeItem(ROLES_KEY)
  }
  return { token, user, roles, loading, isLoggedIn, displayName, isAdmin, isVolunteer, login, register, refreshCurrentUser, updateProfile, hasAnyRole, logout }
})
