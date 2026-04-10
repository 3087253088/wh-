<template>
  <div class="my-approval">
    <el-tabs v-model="activeTab" @tab-change="loadData">
      <el-tab-pane label="待审批" name="pending">
        <el-card>
          <el-table :data="pendingList" v-loading="loading" stripe>
            <el-table-column prop="title" label="报销事由" min-width="200" />
            <el-table-column prop="applicantName" label="申请人" width="120" />
            <el-table-column prop="deptName" label="部门" width="120" />
            <el-table-column prop="totalAmount" label="金额" width="120">
              <template #default="{ row }">¥{{ row.totalAmount }}</template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleDetail(row)">查看详情</el-button>
                <el-button link type="success" @click="handleApprove(row)">通过</el-button>
                <el-button link type="danger" @click="handleReject(row)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
              v-model:current-page="pendingPage"
              v-model:page-size="pageSize"
              :total="pendingTotal"
              layout="total, prev, pager, next"
              @current-change="loadData"
              class="pagination"
          />
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="已审批" name="history">
        <el-card>
          <el-table :data="historyList" v-loading="loading" stripe>
            <el-table-column prop="title" label="报销事由" min-width="200" />
            <el-table-column prop="applicantName" label="申请人" width="120" />
            <el-table-column prop="totalAmount" label="金额" width="120" />
            <el-table-column prop="result" label="审批结果" width="100">
              <template #default="{ row }">
                <el-tag :type="row.result === 'approved' ? 'success' : 'danger'">
                  {{ row.result === 'approved' ? '通过' : '驳回' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="approvalTime" label="审批时间" width="180" />
            <el-table-column prop="comment" label="审批意见" min-width="150" />
          </el-table>
          <el-pagination
              v-model:current-page="historyPage"
              v-model:page-size="pageSize"
              :total="historyTotal"
              layout="total, prev, pager, next"
              @current-change="loadData"
              class="pagination"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 审批弹窗 -->
    <el-dialog :title="approvalTitle" v-model="approvalVisible" width="500px">
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="报销事由">
          <span>{{ currentExpense?.title }}</span>
        </el-form-item>
        <el-form-item label="报销金额">
          <span style="color: #f56c6c;">¥{{ currentExpense?.totalAmount }}</span>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approvalForm.comment" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalVisible = false">取消</el-button>
        <el-button :type="approvalType === 'approve' ? 'success' : 'danger'" @click="submitApproval" :loading="submitLoading">
          {{ approvalType === 'approve' ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="报销详情" v-model="detailVisible" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="报销事由">{{ currentExpense?.title }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentExpense?.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentExpense?.deptName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ currentExpense?.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentExpense?.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="报销说明" :span="2">{{ currentExpense?.description }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { approvalApi, expenseApi } from '@/api'

const activeTab = ref('pending')
const loading = ref(false)
const submitLoading = ref(false)

const pendingList = ref([])
const historyList = ref([])
const pendingTotal = ref(0)
const historyTotal = ref(0)
const pendingPage = ref(1)
const historyPage = ref(1)
const pageSize = ref(10)

const approvalVisible = ref(false)
const detailVisible = ref(false)
const approvalTitle = ref('')
const approvalType = ref('approve')
const currentExpense = ref(null)

const approvalForm = ref({ comment: '' })

const loadData = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'pending') {
      const res = await approvalApi.getPending({ pageNum: pendingPage.value, pageSize: pageSize.value })
      if (res.code === 200) {
        pendingList.value = res.data.records || []
        pendingTotal.value = res.data.total || 0
      }
    } else {
      const res = await approvalApi.getHistory({ pageNum: historyPage.value, pageSize: pageSize.value })
      if (res.code === 200) {
        historyList.value = res.data.records || []
        historyTotal.value = res.data.total || 0
      }
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleDetail = async (row) => {
  try {
    const res = await expenseApi.getDetail(row.id)
    if (res.code === 200) {
      currentExpense.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const handleApprove = (row) => {
  currentExpense.value = row
  approvalType.value = 'approve'
  approvalTitle.value = '审批通过'
  approvalForm.value.comment = ''
  approvalVisible.value = true
}

const handleReject = (row) => {
  currentExpense.value = row
  approvalType.value = 'reject'
  approvalTitle.value = '审批驳回'
  approvalForm.value.comment = ''
  approvalVisible.value = true
}

const submitApproval = async () => {
  submitLoading.value = true
  try {
    await approvalApi.approve(currentExpense.value.id, {
      result: approvalType.value === 'approve' ? 'approved' : 'rejected',
      comment: approvalForm.value.comment
    })
    ElMessage.success(approvalType.value === 'approve' ? '审批通过' : '已驳回')
    approvalVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.my-approval { padding: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>