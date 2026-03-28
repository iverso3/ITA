<template>
  <template v-for="menu in menus" :key="menu.id">
    <!-- 无子菜单或只有按钮子菜单 → 渲染为菜单项 -->
    <el-menu-item
      v-if="!hasRealChildren(menu)"
      :index="menu.path"
      @click="go(menu.path)"
    >
      <el-icon v-if="menu.icon"><component :is="getIcon(menu.icon)" /></el-icon>
      <template #title>{{ menu.menuName }}</template>
    </el-menu-item>

    <!-- 有子菜单 → 渲染为子菜单（递归） -->
    <el-sub-menu
      v-else
      :index="menu.path"
    >
      <template #title>
        <el-icon v-if="menu.icon"><component :is="getIcon(menu.icon)" /></el-icon>
        <span>{{ menu.menuName }}</span>
      </template>
      <!-- 递归渲染子菜单 -->
      <SidebarMenuItem :menus="getRealChildren(menu.children)" :go="go" :get-icon="getIcon" />
    </el-sub-menu>
  </template>
</template>

<script>
export default {
  name: 'SidebarMenuItem'
}
</script>

<script setup>
const props = defineProps({
  menus: {
    type: Array,
    required: true
  },
  go: {
    type: Function,
    required: true
  },
  getIcon: {
    type: Function,
    required: true
  }
})

// 过滤出非按钮的子菜单
const hasRealChildren = (menu) => {
  if (!menu.children || menu.children.length === 0) return false
  return menu.children.some(child => child.menuType !== 'BUTTON')
}

const getRealChildren = (children) => {
  if (!children) return []
  return children.filter(child => child.menuType !== 'BUTTON')
}
</script>
