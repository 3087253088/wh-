<template>
  <div class="invoice-manage">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div><div class="stat-title">总发票数</div><div class="stat-number">{{ statistics.total }}</div></div>
            <el-icon :size="40" color="#409eff"><Document /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div><div class="stat-title">已验证</div><div class="stat-number" style="color: #67c23a;">{{ statistics.verified }}</div></div>
            <el-icon :size="40" color="#67c23a"><CircleCheck /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div><div class="stat-title">未验证</div><div class="stat-number" style="color: #e6a23c;">{{ statistics.unverified }}</div></div>
            <el-icon :size="40" color="#e6a23c"><Warning /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div><div class="stat-title">验真失败</div><div class="stat-number" style="color: #f56c6c;">{{ statistics.failed }}</div></div>
            <el-icon :size="40" color="#f56c6c"><Close /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="发票号码">
          <el-input v-model="searchForm.invoiceCode" placeholder="请输入发票号码" clearable />
        </el-form-item>
        <el-form-item label="开票方">
          <el-input v-model="searchForm.drawer" placeholder="请输入开票方" clearable />
        </el-form-item>
        <el-form-item label="验证状态">
          <el-select v-model="searchForm.verifyStatus" placeholder="请选择状态" clearable>
            <el-option label="未验证" value="unverified" />
            <el-option label="已验证" value="verified" />
            <el-option label="验真失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="warning" @click="handleBatchVerify" :disabled="selectedRows.length === 0">批量验真</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 发票列表 -->
    <el-card class="list-card">
      <el-table :data="invoiceList" v-loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="invoiceCode" label="发票号码" width="180" />
        <el-table-column prop="invoiceType" label="发票类型" width="120">
          <template #default="{ row }">{{ row.invoiceTypeText }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">¥{{ row.amount?.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column prop="drawer" label="开票方" min-width="180" />
          <el-table-column prop="userName" label="所属员工" width="100" />
          <el-table-column prop="verifyStatusText" label="验证状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getVerifyStatusType(row.verifyStatus)">{{ row.verifyStatusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="录入时间" width="160" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
              <el-button link type="success" size="small" v-if="row.verifyStatus === 'unverified'" @click="handleVerify(row)">验真</el-button>
            </template>
          </el-table-column>
        </el-table-column>
      </el-table>
      <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
          class="pagination"
      />
    </el-card>

    <!-- 发票详情弹窗 -->
    <el-dialog title="发票详情" v-model="viewVisible" width="550px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="发票号码">{{ viewData.invoiceCode }}</el-descriptions-item>
        <el-descriptions-item label="发票类型">{{ viewData.invoiceTypeText }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ viewData.amount }}</el-descriptions-item>
        <el-descriptions-item label="开票日期">{{ viewData.invoiceDate }}</el-descriptions-item>
        <el-descriptions-item label="开票方" :span="2">{{ viewData.drawer }}</el-descriptions-item>
        <el-descriptions-item label="所属员工">{{ viewData.userName }}</el-descriptions-item>
        <el-descriptions-item label="验证状态">{{ viewData.verifyStatusText }}</el-descriptions-item>
        <el-descriptions-item label="验证时间" v-if="viewData.verifyTime">{{ viewData.verifyTime }}</el-descriptions-item>
        <el-descriptions-item label="录入时间">{{ viewData.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { invoiceApi } from '@/api'
import { Document, CircleCheck, Warning, Close } from '@element-plus/icons-vue'

const loading = ref(false)
const invoiceList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const selectedRows = ref([])

const statistics = reactive({ total: 0, verified: 0, unverified: 0, failed: 0 })

const searchForm = reactive({ invoiceCode: '', drawer: '', verifyStatus: '' })

const viewVisible = ref(false)
const viewData = ref({})

const getVerifyStatusType = (status) => {
  const map = { unverified: 'info', verified: 'success', failed: 'danger' }
  return map[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await invoiceApi.getAllList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      invoiceCode: searchForm.invoiceCode,
      drawer: searchForm.drawer,
      verifyStatus: searchForm.verifyStatus
    })
    if (res.code === 200) {
      invoiceList.value = res.data.records || []
      total.value = res.data.total || 0
      // 更新统计数据
      statistics.total = res.data.total || 0
      statistics.verified = invoiceList.value.filter(i => i.verifyStatus === 'verified').length
      statistics.unverified = invoiceList.value.filter(i => i.verifyStatus === 'unverified').length
      statistics.failed = invoiceList.value.filter(i => i.verifyStatus === 'failed').length
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageNum.value = 1; loadData() }
const resetSearch = () => { searchForm.invoiceCode = ''; searchForm.drawer = ''; searchForm.verifyStatus = ''; handleSearch() }

const handleView = (row) => {
  viewData.value = row
  viewVisible.value = true
}

const handleVerify = async (row) => {
  try {
    await ElMessageBox.confirm('确定验真该发票吗？', '提示', { type: 'info' })
    await invoiceApi.verify(row.id)
    ElMessage.success('验真成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('验真失败')
  }
}

const handleBatchVerify = async () => {
  if (selectedRows.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定验真选中的 ${selectedRows.value.length} 张发票吗？`, '提示', { type: 'info' })
    const ids = selectedRows.value.map(row => row.id)
    const res = await invoiceApi.batchVerify(ids)
    if (res.code === 200) {
      ElMessage.success(`批量验真完成：成功${res.data.success}张，失败${res.data.failed}张`)
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('批量验真失败')
  }
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

onMounted(() => { loadData() })
</script>

<style scoped>
.invoice-manage { display: flex; flex-direction: column; gap: 20px; }
.stats-row { margin-bottom: 0; }
.stat-item { display: flex; justify-content: space-between; align-items: center; }
.stat-title { font-size: 14px; color: #909399; }
.stat-number { font-size: 28px; font-weight: bold; margin-top: 8px; }
.search-card, .list-card { border-radius: 12px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
