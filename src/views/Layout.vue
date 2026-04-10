<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo" :class="{ collapsed: isCollapse }">
        <el-icon><DataLine /></el-icon>
        <span v-show="!isCollapse">财务一体化平台</span>
      </div>
      <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          background-color="#1e2a3a"
          text-color="#9aaebf"
          active-text-color="#67c23a"
          router
      >
        <template v-for="item in menuList" :key="item.path">
          <el-menu-item :index="item.path" v-if="!item.children">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon @click="toggleCollapse" class="collapse-btn">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userInfo?.avatar" :icon="UserFilled" />
              <span class="username">{{ userInfo?.name }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="info">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import {
  DataLine, Fold, Expand, UserFilled, ArrowDown,
  Odometer, Money, Tickets, Checked, Coin, Document, User
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

// 根据角色生成菜单
const menuList = computed(() => {
  const role = userInfo.value?.roleCode
  const menus = {
    admin: [
      { path: '/my-approval', title: '我的审批', icon: 'Checked' },
      { path: '/personnel-manage', title: '人员管理', icon: 'User' }
    ],
    finance: [
      { path: '/workbench', title: '工作台', icon: 'Odometer' },
      { path: '/my-expense', title: '我的报销', icon: 'Money' },
      { path: '/my-invoice', title: '我的发票', icon: 'Tickets' },
      { path: '/my-approval', title: '我的审批', icon: 'Checked' },
      { path: '/budget-manage', title: '预算管理', icon: 'Coin' },
      { path: '/invoice-manage', title: '发票管理', icon: 'Document' },
      { path: '/employee-info', title: '员工信息', icon: 'User' },
      { path: '/my-info', title: '我的信息', icon: 'UserFilled' }
    ],
    employee: [
      { path: '/workbench', title: '工作台', icon: 'Odometer' },
      { path: '/my-expense', title: '我的报销', icon: 'Money' },
      { path: '/my-invoice', title: '我的发票', icon: 'Tickets' },
      { path: '/my-info', title: '我的信息', icon: 'UserFilled' }
    ]
  }
  return menus[role] || []
})

const currentTitle = computed(() => {
  const matched = route.matched.find(item => item.meta?.title)
  return matched?.meta?.title || route.name || '工作台'
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command) => {
  if (command === 'info') {
    router.push('/my-info')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

onMounted(() => {
  userStore.restoreUser()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.sidebar {
  background-color: #1e2a3a;
  transition: width 0.3s;
  overflow-x: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background: #0f1a24;
}
.logo.collapsed {
  font-size: 20px;
}
.logo span {
  white-space: nowrap;
}
.el-menu {
  border-right: none;
}
.header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.username {
  font-size: 14px;
}
.main-content {
  background: #f5f7fa;
  padding: 20px;
}
</style>
