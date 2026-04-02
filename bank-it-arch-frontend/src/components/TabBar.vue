<template>
  <div class="tab-bar">
    <div class="tab-bar__tabs" ref="tabsContainer">
      <div
        v-for="tab in tabsStore.tabs"
        :key="tab.path"
        :class="['tab-item', { 'tab-item--active': tab.path === tabsStore.activeTabPath }]"
        @click="handleTabClick(tab)"
        @contextmenu.prevent="handleContextMenu($event, tab)"
      >
        <span class="tab-item__title">{{ tab.title }}</span>
        <span
          v-if="tab.closable"
          class="tab-item__close"
          @click.stop="handleClose(tab)"
        >
          <el-icon><Close /></el-icon>
        </span>
      </div>
    </div>

    <!-- Context Menu -->
    <teleport to="body">
      <div
        v-if="contextMenu.visible"
        class="context-menu"
        :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
        @click.stop
      >
        <div class="context-menu__item" @click="handleContextAction('closeCurrent')">
          <el-icon><Close /></el-icon>
          <span>关闭当前</span>
        </div>
        <div class="context-menu__item" @click="handleContextAction('closeOther')">
          <el-icon><Delete /></el-icon>
          <span>关闭其他</span>
        </div>
        <div class="context-menu__item" @click="handleContextAction('closeAll')">
          <el-icon><FolderRemove /></el-icon>
          <span>关闭所有</span>
        </div>
      </div>
    </teleport>

    <!-- Click outside to close context menu -->
    <div
      v-if="contextMenu.visible"
      class="context-menu-overlay"
      @click="closeContextMenu"
      @contextmenu.prevent
    ></div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import { Close, Delete, FolderRemove } from '@element-plus/icons-vue'

export default defineComponent({
  name: 'TabBar',
  components: { Close, Delete, FolderRemove }
})
</script>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTabsStore } from '@/store/tabs'

const router = useRouter()
const tabsStore = useTabsStore()

const contextMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  currentTab: null
})

const handleTabClick = (tab) => {
  tabsStore.setActiveTab(tab.path)
  router.push(tab.path)
}

const handleClose = (tab) => {
  const newPath = tabsStore.removeTab(tab.path)
  if (newPath) {
    router.push(newPath)
  }
}

const handleContextMenu = (event, tab) => {
  if (!tab.closable) return

  contextMenu.visible = true
  contextMenu.x = event.clientX
  contextMenu.y = event.clientY
  contextMenu.currentTab = tab
}

const handleContextAction = (action) => {
  const tab = contextMenu.currentTab
  closeContextMenu()

  if (!tab) return

  switch (action) {
    case 'closeCurrent':
      {
        const newPath = tabsStore.removeTab(tab.path)
        if (newPath) {
          router.push(newPath)
        }
      }
      break
    case 'closeOther':
      tabsStore.closeOtherTabs(tab.path)
      router.push(tab.path)
      break
    case 'closeAll':
      tabsStore.closeAllTabs()
      router.push('/')
      break
  }
}

const closeContextMenu = () => {
  contextMenu.visible = false
  contextMenu.currentTab = null
}

const handleGlobalClick = () => {
  if (contextMenu.visible) {
    closeContextMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', handleGlobalClick)
})

onUnmounted(() => {
  document.removeEventListener('click', handleGlobalClick)
})
</script>

<style scoped lang="scss">
.tab-bar {
  height: 40px;
  background: var(--bg-dark-2);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: flex-end;
  padding: 0 8px;

  &__tabs {
    display: flex;
    align-items: flex-end;
    gap: 2px;
    overflow-x: auto;
    flex: 1;
    scrollbar-width: none;

    &::-webkit-scrollbar {
      display: none;
    }
  }
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: var(--bg-dark-3);
  border: 1px solid var(--border);
  border-bottom: none;
  border-radius: 6px 6px 0 0;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  max-width: 180px;

  &:hover {
    background: var(--bg-hover);
  }

  &--active {
    background: var(--bg-card);
    border-color: var(--border-light);
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -1px;
      left: 0;
      right: 0;
      height: 2px;
      background: var(--bg-card);
    }
  }

  &__title {
    font-size: 0.8125rem;
    color: var(--text-secondary);
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &--active &__title {
    color: var(--text-primary);
    font-weight: 500;
  }

  &__close {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 16px;
    height: 16px;
    border-radius: 4px;
    color: var(--text-muted);
    transition: all 0.2s ease;

    &:hover {
      background: var(--bg-hover);
      color: var(--text-primary);
    }
  }
}

.context-menu {
  position: fixed;
  z-index: 9999;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 4px;
  min-width: 140px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);

  &__item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.875rem;
    color: var(--text-secondary);
    transition: all 0.15s ease;

    &:hover {
      background: var(--bg-hover);
      color: var(--text-primary);
    }
  }
}

.context-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9998;
}
</style>