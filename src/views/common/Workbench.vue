<template>
  <div class="workbench">
    <!-- 统计卡片 - 根据角色显示不同内容 -->
    <el-row :gutter="20">
      <!-- 财务/管理员：显示4个卡片 -->
      <template v-if="userRole === 'finance' || userRole === 'admin'">
        <el-col :span="6" v-for="stat in financeStats" :key="stat.title">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div>
                <div class="stat-title">{{ stat.title }}</div>
                <div class="stat-number">{{ stat.value }}</div>
              </div>
              <el-icon :size="48" :color="stat.iconColor">
                <component :is="stat.icon" />
              </el-icon>
            </div>
          </el-card>
        </el-col>
      </template>
      <!-- 普通员工：显示3个卡片 -->
      <template v-else>
        <el-col :span="8" v-for="stat in employeeStats" :key="stat.title">
          <el-card shadow="hover" class="stat-card" @click="stat.title === '个人信息' ? goToMyInfo() : null">
            <div class="stat-content">
              <div>
                <div class="stat-title">{{ stat.title }}</div>
                <div class="stat-number">{{ stat.value }}</div>
              </div>
              <el-icon :size="48" :color="stat.iconColor">
                <component :is="stat.icon" />
              </el-icon>
            </div>
          </el-card>
        </el-col>
      </template>
    </el-row>

    <!-- 待办事项 - 仅财务/管理员可见 -->
    <el-card class="todo-card" v-if="userRole === 'finance' || userRole === 'admin'">
      <template #header>
        <div class="card-header">
          <span>待办事项</span>
        </div>
      </template>
      <el-table :data="todos" v-loading="todoLoading" style="width: 100%">
        <el-table-column prop="title" label="事项" />
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="goToApproval(row.id)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 费用趋势图 - 仅财务/管理员可见 -->
    <el-card class="trend-card" v-if="userRole === 'finance' || userRole === 'admin'">
      <template #header>
        <div class="card-header">
          <span>近6个月费用趋势</span>
        </div>
      </template>
      <div class="chart-container">
        <canvas id="expenseChart" ref="chartCanvas"></canvas>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { dashboardApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Chart, registerables } from 'chart.js'
import { Odometer, Money, Tickets, User } from '@element-plus/icons-vue'

Chart.register(...registerables)

const router = useRouter()
const userStore = useUserStore()
const userRole = computed(() => userStore.userInfo?.roleCode)

// 财务/管理员的统计数据
const financeStats = ref([
  { title: '本月报销总额', value: '¥0', icon: 'Money', iconColor: '#67c23a' },
  { title: '待审批单据', value: '0', icon: 'Tickets', iconColor: '#e6a23c' },
  { title: '预算执行率', value: '0%', icon: 'Odometer', iconColor: '#409eff' },
  { title: '员工数量', value: '0', icon: 'User', iconColor: '#909399' }
])

// 普通员工的统计数据
const employeeStats = ref([
  { title: '个人报销总额', value: '¥0', icon: 'Money', iconColor: '#67c23a' },
  { title: '我的发票', value: '0', icon: 'Tickets', iconColor: '#409eff' },
  { title: '个人信息', value: '查看', icon: 'User', iconColor: '#909399' }
])

const todos = ref([])
const todoLoading = ref(false)
let chart = null
const chartCanvas = ref(null)

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await dashboardApi.getStatistics()
    if (res.code === 200) {
      const data = res.data

      if (userRole.value === 'finance' || userRole.value === 'admin') {
        // 财务/管理员数据
        financeStats.value = [
          { title: '本月报销总额', value: `¥${data.monthExpenseTotal?.toLocaleString() || 0}`, icon: 'Money', iconColor: '#67c23a' },
          { title: '待审批单据', value: data.pendingApprovalCount || 0, icon: 'Tickets', iconColor: '#e6a23c' },
          { title: '预算执行率', value: `${data.budgetExecutionRate || 0}%`, icon: 'Odometer', iconColor: '#409eff' },
          { title: '员工数量', value: data.employeeCount || 0, icon: 'User', iconColor: '#909399' }
        ]
      } else {
        // 普通员工数据
        employeeStats.value = [
          { title: '个人报销总额', value: `¥${data.myExpenseTotal?.toLocaleString() || 0}`, icon: 'Money', iconColor: '#67c23a' },
          { title: '我的发票', value: data.myInvoiceCount || 0, icon: 'Tickets', iconColor: '#409eff' },
          { title: '个人信息', value: '查看', icon: 'User', iconColor: '#909399' }
        ]
      }
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

// 加载待办事项（仅财务/管理员需要）
const loadTodos = async () => {
  // 只有财务/管理员才加载待办事项
  if (userRole.value !== 'finance' && userRole.value !== 'admin') {
    return
  }

  todoLoading.value = true
  try {
    const res = await dashboardApi.getTodos()
    if (res.code === 200) {
      todos.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载待办事项失败')
  } finally {
    todoLoading.value = false
  }
}

// 加载费用趋势（仅财务/管理员需要）
const loadExpenseTrend = async () => {
  // 只有财务/管理员才加载费用趋势
  if (userRole.value !== 'finance' && userRole.value !== 'admin') {
    return
  }

  try {
    const res = await dashboardApi.getExpenseTrend(6)
    if (res.code === 200) {
      const data = res.data
      renderChart(data.months, data.amounts)
    }
  } catch (error) {
    ElMessage.error('加载费用趋势失败')
  }
}

// 渲染图表
const renderChart = (months, amounts) => {
  if (chart) {
    chart.destroy()
  }
  const ctx = chartCanvas.value?.getContext('2d')
  if (ctx) {
    chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: months,
        datasets: [{
          label: '报销总额 (元)',
          data: amounts,
          borderColor: '#2d6a4f',
          backgroundColor: 'rgba(45, 106, 79, 0.05)',
          tension: 0.3,
          fill: true
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true
      }
    })
  }
}

const goToApproval = (id) => {
  router.push(`/my-approval?taskId=${id}`)
}

// 点击个人信息卡片跳转
const goToMyInfo = () => {
  router.push('/my-info')
}

onMounted(() => {
  loadStatistics()
  loadTodos()
  loadExpenseTrend()
})
</script>

<style scoped>
.workbench {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.stat-card {
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}
.stat-card:hover {
  transform: translateY(-5px);
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
  margin-top: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chart-container {
  height: 300px;
}
</style>