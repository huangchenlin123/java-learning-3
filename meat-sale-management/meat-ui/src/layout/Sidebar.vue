<template>
  <div style="height:60px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:18px;font-weight:bold;border-bottom:1px solid rgba(255,255,255,0.1)">
    <span v-if="!app.sidebarCollapsed">🐷 肉类管理</span>
    <span v-else>🐷</span>
  </div>
  <el-menu
    :default-active="route.path"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
    :collapse="app.sidebarCollapsed"
    router
    style="border-right:none"
  >
    <template v-for="menu in user.menus" :key="menu.id">
      <!-- 有子菜单 → 可折叠 -->
      <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path || menu.id.toString()">
        <template #title>
          <el-icon><component :is="menu.icon || 'Menu'" /></el-icon>
          <span>{{ menu.menuName }}</span>
        </template>
        <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.path">
          <el-icon><component :is="child.icon || 'Menu'" /></el-icon>
          <span>{{ child.menuName }}</span>
        </el-menu-item>
      </el-sub-menu>
      <!-- 无子菜单 → 直接跳转 -->
      <el-menu-item v-else :index="menu.path || '/'">
        <el-icon><component :is="menu.icon || 'Menu'" /></el-icon>
        <span>{{ menu.menuName }}</span>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { useAppStore } from '../stores/app'
import { useUserStore } from '../stores/user'
const route = useRoute()
const app = useAppStore()
const user = useUserStore()
</script>
