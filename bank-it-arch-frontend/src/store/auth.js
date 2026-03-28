import { defineStore } from 'pinia'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { authApi } from '@/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken() || '',
    userInfo: null,
    menuTree: [],
    permissions: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.realName || state.userInfo?.username || '',
    avatar: (state) => state.userInfo?.avatar || '',
    roles: (state) => state.userInfo?.roles || [],
    isSuperAdmin: (state) => {
      return state.userInfo?.roles?.some(r => r.roleCode === 'ROLE_SUPER_ADMIN')
    }
  },

  actions: {
    async login(username, password) {
      const response = await authApi.login({ username, password })
      const { token, userId, username: uname, realName, avatar, departmentName, roles, menuTree, permissions } = response.data

      this.token = token
      this.userInfo = {
        userId: userId,
        username: uname,
        realName: realName,
        avatar: avatar,
        departmentName: departmentName,
        roles: roles
      }
      this.menuTree = menuTree || []
      this.permissions = permissions || []

      setToken(token)
    },

    async getCurrentUser() {
      if (!this.token) return null

      try {
        const response = await authApi.me()
        this.userInfo = {
          userId: response.data.userId,
          username: response.data.username,
          realName: response.data.realName,
          avatar: response.data.avatar,
          departmentName: response.data.departmentName,
          roles: response.data.roles
        }
        this.menuTree = response.data.menuTree || []
        this.permissions = response.data.permissions || []
        return response.data
      } catch (error) {
        this.logout()
        throw error
      }
    },

    logout() {
      this.token = ''
      this.userInfo = null
      this.menuTree = []
      this.permissions = []
      removeToken()
    },

    hasPermission(permission) {
      if (!permission) return true
      return this.permissions.includes(permission)
    },

    hasMenuPermission(path) {
      const findMenu = (menus) => {
        for (const menu of menus) {
          if (menu.path === path) return true
          if (menu.children && findMenu(menu.children)) return true
        }
        return false
      }
      return findMenu(this.menuTree)
    }
  }
})
