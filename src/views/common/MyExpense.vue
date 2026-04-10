<template>
  <div class="my-expense">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="报销事由">
          <el-input v-model="searchForm.title" placeholder="请输入报销事由" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="draft" />
            <el-option label="审批中" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="handleAdd">+ 新增报销</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 报销列表 -->
    <el-card class="list-card">
      <el-table :data="expenseList" v-loading="loading" stripe>
        <el-table-column prop="title" label="报销事由" min-width="200" />
        <el-table-column prop="totalAmount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="warning" size="small" v-if="row.status === 'draft'" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" v-if="row.status === 'draft'" @click="handleDelete(row)">删除</el-button>
            <el-button link type="success" size="small" v-if="row.status === 'draft'" @click="handleSubmit(row)">提交</el-button>
            <el-button link type="info" size="small" v-if="row.status === 'pending'" @click="handleRecall(row)">撤回</el-button>
          </template>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="报销事由" prop="title">
          <el-input v-model="formData.title" placeholder="请输入报销事由" />
        </el-form-item>
        <el-form-item label="费用类型" prop="expenseType">
          <el-select v-model="formData.expenseType" placeholder="请选择费用类型">
            <el-option label="差旅费" value="travel" />
            <el-option label="业务招待费" value="entertainment" />
            <el-option label="办公用品费" value="office" />
            <el-option label="交通费" value="transportation" />
            <el-option label="培训费" value="training" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="报销金额" prop="amount">
          <el-input-number v-model="formData.amount" :precision="2" :min="0.01" placeholder="请输入金额" />
        </el-form-item>
        <el-form-item label="发生日期" prop="expenseDate">
          <el-date-picker v-model="formData.expenseDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="发票号" prop="invoiceNo">
          <el-input v-model="formData.invoiceNo" placeholder="请输入发票号" />
        </el-form-item>
        <el-form-item label="报销说明" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入报销说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saveLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog title="报销详情" v-model="viewVisible" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="报销事由">{{ viewData.title }}</el-descriptions-item>
        <el-descriptions-item label="报销金额">¥{{ viewData.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(viewData.status) }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ viewData.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="报销说明" :span="2">{{ viewData.description }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="viewData.approvalHistory?.length" style="margin-top: 20px;">
        <h4>审批记录</h4>
        <el-timeline>
          <el-timeline-item v-for="item in viewData.approvalHistory" :key="item.id" :timestamp="item.approvalTime">
            {{ item.approverName }}：{{ item.result === 'approved' ? '通过' : '驳回' }} - {{ item.comment }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { expenseApi } from '@/api'

const loading = ref(false)
const saveLoading = ref(false)
const expenseList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({ title: '', status: '' })

const dialogVisible = ref(false)
const viewVisible = ref(false)
const dialogTitle = ref('新增报销')
const formRef = ref(null)
const formData = reactive({
  id: null, title: '', expenseType: '', amount: null, expenseDate: '', invoiceNo: '', description: ''
})
const viewData = ref({})

const rules = {
  title: [{ required: true, message: '请输入报销事由', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入报销金额', trigger: 'blur' }],
  expenseDate: [{ required: true, message: '请选择发生日期', trigger: 'change' }]
}

const getStatusType = (status) => {
  const map = { draft: 'info', pending: 'warning', approved: 'success', rejected: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { draft: '草稿', pending: '审批中', approved: '已通过', rejected: '已驳回' }
  return map[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await expenseApi.getList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      title: searchForm.title,
      status: searchForm.status
    })
    if (res.code === 200) {
      expenseList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageNum.value = 1; loadData() }
const resetSearch = () => { searchForm.title = ''; searchForm.status = ''; handleSearch() }
const handleAdd = () => { dialogTitle.value = '新增报销'; resetForm(); dialogVisible.value = true }

const handleView = async (row) => {
  try {
    const res = await expenseApi.getDetail(row.id)
    if (res.code === 200) {
      viewData.value = res.data
      viewVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑报销'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该报销单吗？', '提示', { type: 'warning' })
    await expenseApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleSubmit = async (row) => {
  try {
    await ElMessageBox.confirm('确定提交该报销单吗？', '提示')
    await expenseApi.submit(row.id)
    ElMessage.success('提交成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('提交失败')
  }
}

const handleRecall = async (row) => {
  try {
    await ElMessageBox.confirm('确定撤回该报销单吗？', '提示')
    await expenseApi.recall(row.id)
    ElMessage.success('撤回成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('撤回失败')
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saveLoading.value = true
      try {
        if (formData.id) {
          await expenseApi.update(formData.id, formData)
          ElMessage.success('修改成功')
        } else {
          await expenseApi.save(formData)
          ElMessage.success('保存成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error('保存失败')
      } finally {
        saveLoading.value = false
      }
    }
  })
}

const resetForm = () => {
  Object.assign(formData, { id: null, title: '', expenseType: '', amount: null, expenseDate: '', invoiceNo: '', description: '' })
}

onMounted(() => { loadData() })
</script>

<style scoped>
.my-expense { display: flex; flex-direction: column; gap: 20px; }
.search-card, .list-card { border-radius: 12px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
