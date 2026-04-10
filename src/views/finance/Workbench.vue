<template>
  <div class="workbench">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div>
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-number">{{ stat.value }}</div>
              <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
                {{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%
              </div>
            </div>
            <el-icon :size="48" :color="stat.iconColor">
              <component :is="stat.icon" />
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办事项 -->
    <el-card class="todo-card">
      <template #header>
        <div class="card-header">
          <span>待办事项</span>
          <el-button text type="primary">查看更多</el-button>
        </div>
      </template>
      <el-table :data="todos" style="width: 100%">
        <el-table-column prop="title" label="事项" />
        <el-table-column prop="submitter" label="申请人" width="120" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            ¥{{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default>
            <el-button type="primary" size="small">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 最近报销 -->
    <el-card class="recent-card">
      <template #header>
        <div class="card-header">
          <span>最近报销单</span>
          <el-button text type="primary" @click="goTo('/my-expense')">全部报销</el-button>
        </div>
      </template>
      <el-table :data="recentExpenses" style="width: 100%">
        <el-table-column prop="title" label="报销事由" />
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { Odometer, Money, Tickets, User } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const userRole = computed(() => userStore.userInfo?.role)

// 统计数据（根据角色动态展示）
const stats = computed(() => {
  const baseStats = [
    { title: '本月报销总额', value: '¥84,260', trend: 12.5, icon: 'Money', iconColor: '#67c23a' },
    { title: '待审批单据', value: '8', trend: 2, icon: 'Tickets', iconColor: '#e6a23c' }
  ]
  if (userRole.value === 'finance') {
    baseStats.push(
        { title: '预算执行率', value: '68%', trend: -5, icon: 'Odometer', iconColor: '#409eff' },
        { title: '员工数量', value: '156', trend: 3, icon: 'User', iconColor: '#909399' }
    )
  } else {
    baseStats.push(
        { title: '个人报销总额', value: '¥12,380', trend: 8, icon: 'Odometer', iconColor: '#409eff' },
        { title: '我的发票', value: '23', trend: 2, icon: 'Tickets', iconColor: '#909399' }
    )
  }
  return baseStats
})

const todos = ref([
  { id: 1, title: '差旅费报销审批', submitter: '张三', amount: 3280, createTime: '2025-03-20' },
  { id: 2, title: '业务招待费审批', submitter: '李四', amount: 1560, createTime: '2025-03-19' },
  { id: 3, title: '办公用品采购审批', submitter: '王五', amount: 890, createTime: '2025-03-18' }
])

const recentExpenses = ref([
  { id: 1, title: '上海出差交通费', amount: 580, status: '已通过', createTime: '2025-03-20' },
  { id: 2, title: '客户接待用餐', amount: 1280, status: '审批中', createTime: '2025-03-19' },
  { id: 3, title: '办公用品采购', amount: 320, status: '已驳回', createTime: '2025-03-18' }
])

const getStatusType = (status) => {
  const map = { '已通过': 'success', '审批中': 'warning', '已驳回': 'danger' }
  return map[status] || 'info'
}

const goTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
.workbench {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.stat-card {
  border-radius: 12px;
}
.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-title {
  font-size: 14px;
  color: #909399;
}
.stat-number {
  font-size: 28px;
  font-weight: bold;
  margin: 8px 0;
}
.stat-trend {
  font-size: 12px;
}
.stat-trend.up {
  color: #67c23a;
}
.stat-trend.down {
  color: #f56c6c;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>