<template>
  <div class="my-invoice">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="发票号码">
          <el-input v-model="searchForm.invoiceCode" placeholder="请输入发票号码" clearable />
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
          <el-button type="success" @click="handleAdd">+ 录入发票</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="list-card">
      <el-table :data="invoiceList" v-loading="loading" stripe>
        <el-table-column prop="invoiceCode" label="发票号码" width="180" />
        <el-table-column prop="invoiceTypeText" label="发票类型" width="120" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="drawer" label="开票方" min-width="180" />
        <el-table-column prop="verifyStatusText" label="验证状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getVerifyStatusType(row.verifyStatus)">{{ row.verifyStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="录入时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            <el-button link type="success" size="small" v-if="row.verifyStatus === 'unverified'" @click="handleVerify(row)">验真</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
          class="pagination"
      />
    </el-card>

    <!-- 录入发票弹窗 -->
    <el-dialog title="录入发票" v-model="dialogVisible" width="550px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="发票号码" prop="invoiceCode">
          <el-input v-model="formData.invoiceCode" placeholder="请输入发票号码" />
        </el-form-item>
        <el-form-item label="发票类型" prop="invoiceType">
          <el-select v-model="formData.invoiceType" placeholder="请选择发票类型">
            <el-option label="增值税专用发票" value="special" />
            <el-option label="增值税普通发票" value="normal" />
            <el-option label="电子发票" value="electronic" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="formData.amount" :precision="2" :min="0.01" placeholder="请输入金额" />
        </el-form-item>
        <el-form-item label="开票日期" prop="invoiceDate">
          <el-date-picker v-model="formData.invoiceDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="开票方" prop="drawer">
          <el-input v-model="formData.drawer" placeholder="请输入开票方名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saveLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 发票详情弹窗 -->
    <el-dialog title="发票详情" v-model="viewVisible" width="550px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="发票号码">{{ viewData.invoiceCode }}</el-descriptions-item>
        <el-descriptions-item label="发票类型">{{ viewData.invoiceTypeText }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ viewData.amount }}</el-descriptions-item>
        <el-descriptions-item label="开票日期">{{ viewData.invoiceDate }}</el-descriptions-item>
        <el-descriptions-item label="开票方" :span="2">{{ viewData.drawer }}</el-descriptions-item>
        <el-descriptions-item label="验证状态">{{ viewData.verifyStatusText }}</el-descriptions-item>
        <el-descriptions-item label="录入时间">{{ viewData.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { invoiceApi } from '@/api'

const loading = ref(false)
const saveLoading = ref(false)
const invoiceList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({ invoiceCode: '', verifyStatus: '' })

const dialogVisible = ref(false)
const viewVisible = ref(false)
const formRef = ref(null)
const formData = reactive({ invoiceCode: '', invoiceType: '', amount: null, invoiceDate: '', drawer: '' })
const viewData = ref({})

const rules = {
  invoiceCode: [{ required: true, message: '请输入发票号码', trigger: 'blur' }],
  invoiceType: [{ required: true, message: '请选择发票类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  invoiceDate: [{ required: true, message: '请选择开票日期', trigger: 'change' }],
  drawer: [{ required: true, message: '请输入开票方', trigger: 'blur' }]
}

const getVerifyStatusType = (status) => {
  const map = { unverified: 'info', verified: 'success', failed: 'danger' }
  return map[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await invoiceApi.getMyList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      invoiceCode: searchForm.invoiceCode,
      verifyStatus: searchForm.verifyStatus
    })
    if (res.code === 200) {
      invoiceList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageNum.value = 1; loadData() }
const resetSearch = () => { searchForm.invoiceCode = ''; searchForm.verifyStatus = ''; handleSearch() }
const handleAdd = () => { resetForm(); dialogVisible.value = true }

const handleView = (row) => {
  viewData.value = row
  viewVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该发票吗？', '提示', { type: 'warning' })
    await invoiceApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleVerify = async (row) => {
  try {
    await ElMessageBox.confirm('确定验真该发票吗？', '提示')
    await invoiceApi.verify(row.id)
    ElMessage.success('验真成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('验真失败')
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saveLoading.value = true
      try {
        await invoiceApi.save(formData)
        ElMessage.success('保存成功')
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
  Object.assign(formData, { invoiceCode: '', invoiceType: '', amount: null, invoiceDate: '', drawer: '' })
}

onMounted(() => { loadData() })
</script>

<style scoped>
.my-invoice { display: flex; flex-direction: column; gap: 20px; }
.search-card, .list-card { border-radius: 12px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
