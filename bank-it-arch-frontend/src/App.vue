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
        <el-tooltip :content="isDark ? '切换到亮色模式' : '切换到深色模式'" placement="bottom">
          <el-button text @click="toggleTheme" class="theme-toggle-btn">
            <el-icon size="20" color="var(--text-secondary)">
              <Sunny v-if="isDark" />
              <Moon v-else />
            </el-icon>
          </el-button>
        </el-tooltip>
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
          <SidebarMenuItem :menus="visibleMenus" :go="handleMenuClick" :get-icon="getIcon" />
        </el-menu>
        <div class="sidebar-collapse-toggle" @click="toggleCollapse">
          <el-icon :size="16">
            <ArrowLeft v-if="!isCollapsed" />
            <ArrowRight v-else />
          </el-icon>
        </div>
      </aside>
      <main class="app-content">
        <TabBar />
        <div class="tab-content">
          <component :is="activeComponent" :key="tabsStore.activeTabPath" />
        </div>
      </main>
    </div>
  </div>

  <router-view v-else />
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useTabsStore } from '@/store/tabs'
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
  Setting,
  Sunny,
  Moon
} from '@element-plus/icons-vue'
import SidebarMenuItem from './components/SidebarMenuItem.vue'
import TabBar from './components/TabBar.vue'

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
    Sunny,
    Moon,
    SidebarMenuItem,
    TabBar
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const authStore = useAuthStore()
    const tabsStore = useTabsStore()
    const isCollapsed = ref(false)
    const isDark = ref(false)

    const isLoggedIn = computed(() => authStore.isLoggedIn)

    // Routes that should not be added to tab bar
    const noTabRoutes = ['/login', '/404', '/403', '/500']

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

    // Get the component for the current active tab path
    const activeComponent = computed(() => {
      // Use route.matched to get the current active route's component
      // This properly handles dynamic routes like /arch/application/:id
      if (route.matched && route.matched.length > 0) {
        const matchedRecord = route.matched[route.matched.length - 1]
        return matchedRecord.components?.default || matchedRecord.components || null
      }
      return null
    })

    const handleMenuClick = (path) => {
      if (path) {
        tabsStore.addTab(path)
        router.push(path).catch(() => {})
      }
    }

    const toggleCollapse = () => {
      isCollapsed.value = !isCollapsed.value
    }

    const toggleTheme = () => {
      isDark.value = !isDark.value
      if (isDark.value) {
        document.documentElement.classList.add('theme-night')
      } else {
        document.documentElement.classList.remove('theme-night')
      }
      localStorage.setItem('theme', isDark.value ? 'night' : 'day')
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
      // Load theme from localStorage
      const savedTheme = localStorage.getItem('theme')
      if (savedTheme === 'night') {
        isDark.value = true
        document.documentElement.classList.add('theme-night')
      }

      if (authStore.isLoggedIn && !authStore.userInfo) {
        try {
          await authStore.getCurrentUser()
        } catch (error) {
          console.error('Failed to get current user:', error)
        }
      }

      // Sync initial route to tab
      if (!noTabRoutes.includes(route.path) && route.path !== '/' && !tabsStore.hasTab(route.path)) {
        tabsStore.addTab(route.path)
      } else {
        tabsStore.setActiveTab(route.path)
      }
    })

    // Watch for route changes (e.g., browser back/forward)
    watch(() => route.path, (newPath) => {
      if (tabsStore.hasTab(newPath)) {
        tabsStore.setActiveTab(newPath)
      } else if (!noTabRoutes.includes(newPath) && newPath !== '/') {
        tabsStore.addTab(newPath)
      }
    })

    return {
      route,
      isCollapsed,
      isDark,
      isLoggedIn,
      authStore,
      tabsStore,
      visibleMenus,
      userInitials,
      getIcon,
      activeComponent,
      handleMenuClick,
      toggleCollapse,
      toggleTheme,
      handleUserCommand
    }
  }
}
</script>

<style scoped lang="scss">
.theme-toggle-btn {
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.theme-toggle-btn:hover {
  background: var(--bg-hover);
}

.tab-content {
  flex: 1;
  overflow: auto;
  background: var(--bg-card);
}
</style>
