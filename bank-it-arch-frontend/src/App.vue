<template>
  <div class="app-container" v-if="isLoggedIn">
    <header class="app-header">
      <div class="logo">
        <div class="logo__icon">
          <el-icon><Grid /></el-icon>
        </div>
        <span class="logo__text">银行IT架构管理平台</span>
      </div>
      <div class="header-right" style="margin-left: auto; display: flex; align-items: center; gap: 16px;">
        <el-badge :value="3" :max="99" class="notification-badge">
          <el-icon size="20" color="var(--text-secondary)"><Bell /></el-icon>
        </el-badge>
        <el-dropdown trigger="click" @command="handleUserCommand">
          <div class="user-info" style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
            <el-avatar :size="32" style="background: var(--primary);">{{ userInitials }}</el-avatar>
            <span style="color: var(--text-primary);">{{ authStore.username }}</span>
            <el-icon color="var(--text-secondary)"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    <div class="app-main">
      <aside class="app-sidebar">
        <el-menu
          :default-active="route.path"
          :collapse="isCollapsed"
          class="app-sidebar__menu"
          background-color="transparent"
          text-color="var(--text-secondary)"
          active-text-color="var(--accent)"
          :unique-opened="false"
        >
          <SidebarMenuItem :menus="visibleMenus" :go="go" :get-icon="getIcon" />
        </el-menu>
        <div class="sidebar-collapse-toggle" @click="toggleCollapse">
          <el-icon :size="16">
            <ArrowLeft v-if="!isCollapsed" />
            <ArrowRight v-else />
          </el-icon>
        </div>
      </aside>
      <main class="app-content">
        <router-view :key="$route.fullPath" />
      </main>
    </div>
  </div>

  <router-view v-else />
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import {
  Grid,
  Bell,
  ArrowDown,
  ArrowLeft,
  ArrowRight,
  HomeFilled,
  DataLine,
  Document,
  Tickets,
  TrendCharts,
  User,
  Box,
  Connection,
  Coin,
  PieChart,
  CircleCheck,
  Management,
  Right,
  DataAnalysis,
  Cloudy,
  Postcard,
  Menu,
  DataBoard,
  DocumentChecked,
  Operation,
  Discount,
  Setting
} from '@element-plus/icons-vue'
import SidebarMenuItem from './components/SidebarMenuItem.vue'

export default {
  name: 'App',
  components: {
    Grid,
    Bell,
    ArrowDown,
    ArrowLeft,
    ArrowRight,
    HomeFilled,
    DataLine,
    Document,
    Tickets,
    TrendCharts,
    User,
    Box,
    Connection,
    Coin,
    PieChart,
    CircleCheck,
    Management,
    Right,
    DataAnalysis,
    Cloudy,
    Postcard,
    Menu,
    DataBoard,
    DocumentChecked,
    Operation,
    Discount,
    Setting,
    SidebarMenuItem
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const authStore = useAuthStore()
    const isCollapsed = ref(false)

    const isLoggedIn = computed(() => authStore.isLoggedIn)
    const visibleMenus = computed(() => {
      const menus = authStore.menuTree || []
      return menus.filter(menu => {
        return menu.menuType !== 'BUTTON' && menu.isVisible === 1
      })
    })

    const userInitials = computed(() => {
      const name = authStore.username
      if (!name) return '?'
      return name.slice(0, 1).toUpperCase()
    })

    const iconMap = {
      'HomeFilled': 'HomeFilled',
      'Home': 'HomeFilled',
      'DataLine': 'DataLine',
      'Document': 'Document',
      'Tickets': 'Tickets',
      'TrendCharts': 'TrendCharts',
      'User': 'User',
      'Grid': 'Grid',
      'Box': 'Box',
      'Connection': 'Connection',
      'Coin': 'Coin',
      'PieChart': 'PieChart',
      'CircleCheck': 'CircleCheck',
      'Management': 'Management',
      'Right': 'Right',
      'DataAnalysis': 'DataAnalysis',
      'Cloud': 'Cloudy',
      'Postcard': 'Postcard',
      'Menu': 'Menu',
      'DataBoard': 'DataBoard',
      'DocumentChecked': 'DocumentChecked',
      'Operation': 'Operation',
      'Discount': 'Discount',
      'Setting': 'Setting',
      // Element Plus 图标（带 el-icon 前缀的）
      'el-icon-home': 'HomeFilled',
      'el-icon-data-line': 'DataLine',
      'el-icon-document': 'Document',
      'el-icon-tickets': 'Tickets',
      'el-icon-trendcharts': 'TrendCharts',
      'el-icon-user': 'User',
      'el-icon-grid': 'Grid',
      'el-icon-box': 'Box',
      'el-icon-connection': 'Connection',
      'el-icon-coin': 'Coin',
      'el-icon-pie-chart': 'PieChart',
      'el-icon-circle-check': 'CircleCheck',
      'el-icon-s-management': 'Management',
      'el-icon-s-data': 'DataLine',
      'el-icon-s-grid': 'Document',
      'el-icon-s-claim': 'Tickets',
      'el-icon-s-report': 'TrendCharts',
      'el-icon-s-setting': 'Setting',
      'el-icon-s-opportunity': 'PieChart',
      'el-icon-cloud': 'Cloudy',
      'el-icon-postcard': 'Postcard',
      'el-icon-menu': 'Menu',
      'el-icon-monitor': 'HomeFilled'
    }

    const getIcon = (iconName) => {
      return iconMap[iconName] || 'Document'
    }

    const go = (path) => {
      if (path) {
        router.push(path).catch(() => {})
      }
    }

    const toggleCollapse = () => {
      isCollapsed.value = !isCollapsed.value
    }

    const handleUserCommand = (command) => {
      switch (command) {
        case 'logout':
          authStore.logout()
          router.push('/login')
          break
        case 'profile':
          break
        case 'password':
          break
      }
    }

    onMounted(async () => {
      if (authStore.isLoggedIn && !authStore.userInfo) {
        try {
          await authStore.getCurrentUser()
        } catch (error) {
          console.error('Failed to get current user:', error)
        }
      }
    })

    return {
      route,
      isCollapsed,
      isLoggedIn,
      authStore,
      visibleMenus,
      userInitials,
      getIcon,
      go,
      toggleCollapse,
      handleUserCommand
    }
  }
}
</script>
