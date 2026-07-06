<template>
  <el-container style="height:100vh">
    <el-aside :width="app.sidebarCollapsed ? '64px' : '220px'" style="background:#304156;transition:width 0.3s;overflow:hidden">
      <Sidebar />
    </el-aside>
    <el-container>
      <el-header style="background:#fff;border-bottom:1px solid #e6e6e6;display:flex;align-items:center;justify-content:space-between;padding:0 20px">
        <div style="display:flex;align-items:center">
          <el-icon style="font-size:20px;cursor:pointer;margin-right:10px" @click="app.toggleSidebar()"><Fold /></el-icon>
          <span style="font-size:16px;font-weight:bold">肉类销售管理系统</span>
        </div>
        <el-dropdown>
          <span style="cursor:pointer">{{ user.userInfo?.username || '管理员' }} <el-icon><ArrowDown /></el-icon></span>
          <template #dropdown>
            <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
          </template>
        </el-dropdown>
      </el-header>
      <el-main style="background:#f0f2f5;padding:20px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useAppStore } from '../stores/app'
import Sidebar from './Sidebar.vue'

const router = useRouter()
const user = useUserStore()
const app = useAppStore()

// 页面刷新后恢复用户会话
onMounted(async () => {
  const token = localStorage.getItem('token')
  if (token && !user.userInfo) {
    try {
      await user.fetchUserInfo()
    } catch (e) {
      user.logout()
      router.push('/login')
    }
  }
})

function handleLogout() {
  user.logout()
  router.push('/login')
}
</script>
