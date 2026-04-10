<template>
  <div class="budget-manage">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="部门">
          <el-select v-model="searchForm.deptId" placeholder="请选择部门" clearable>
            <el-option label="市场部" :value="1" />
            <el-option label="销售部" :value="2" />
            <el-option label="技术部" :value="3" />
            <el-option label="行政部" :value="4" />
            <el-option label="财务部" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用类型">
          <el-select v-model="searchForm.expenseType" placeholder="请选择费用类型" clearable>
            <el-option label="差旅费" value="travel" />
            <el-option label="业务招待费" value="entertainment" />
            <el-option label="办公用品费" value="office" />
            <el-option label="培训费" value="training" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份">
          <el-select v-model="searchForm.year" placeholder="请选择年份">
            <el-option label="2024" :value="2024" />
            <el-option label="2025" :value="2025" />
            <el-option label="2026" :value="2026" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="handleAdd">+ 编制预算</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="list-card">
      <el-table :data="budgetList" v-loading="loading" stripe>
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="expenseTypeText" label="费用类型" width="120" />
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="month" label="月份" width="80" />
        <el-table-column prop="budgetAmount" label="预算金额" width="150">
          <template #default="{ row }">¥{{ row.budgetAmount?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="usedAmount" label="已使用金额" width="150">
          <template #default="{ row }">¥{{ row.usedAmount?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="remainingAmount" label="剩余金额" width="150">
          <template #default="{ row }">
                        <span :style="{ color: row.remainingAmount < 0 ? '#f56c6c' : '#67c23a' }">
                            ¥{{ row.remainingAmount?.toLocaleString() }}
                        </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleAdjust(row)">调整</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 编制/调整预算弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="部门" prop="deptId">
          <el-select v-model="formData.deptId" placeholder="请选择部门">
            <el-option label="市场部" :value="1" />
            <el-option label="销售部" :value="2" />
            <el-option label="技术部" :value="3" />
            <el-option label="行政部" :value="4" />
            <el-option label="财务部" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用类型" prop="expenseType">
          <el-select v-model="formData.expenseType" placeholder="请选择费用类型">
            <el-option label="差旅费" value="travel" />
            <el-option label="业务招待费" value="entertainment" />
            <el-option label="办公用品费" value="office" />
            <el-option label="培训费" value="training" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份" prop="year">
          <el-select v-model="formData.year" placeholder="请选择年份">
            <el-option label="2024" :value="2024" />
            <el-option label="2025" :value="2025" />
            <el-option label="2026" :value="2026" />
          </el-select>
        </el-form-item>
        <el-form-item label="月份" prop="month">
          <el-select v-model="formData.month" placeholder="请选择月份">
            <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="预算金额" prop="budgetAmount">
          <el-input-number v-model="formData.budgetAmount" :precision="2" :min="0" placeholder="请输入预算金额" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saveLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { budgetApi } from '@/api'

const loading = ref(false)
const saveLoading = ref(false)
const budgetList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({ deptId: '', expenseType: '', year: 2025 })

const dialogVisible = ref(false)
const dialogTitle = ref('编制预算')
const formRef = ref(null)
const formData = reactive({ id: null, deptId: '', expenseType: '', year: 2025, month: null, budgetAmount: null })

const rules = {
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  expenseType: [{ required: true, message: '请选择费用类型', trigger: 'change' }],
  year: [{ required: true, message: '请选择年份', trigger: 'change' }],
  month: [{ required: true, message: '请选择月份', trigger: 'change' }],
  budgetAmount: [{ required: true, message: '请输入预算金额', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await budgetApi.getList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      deptId: searchForm.deptId,
      expenseType: searchForm.expenseType,
      year: searchForm.year
    })
    if (res.code === 200) {
      budgetList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageNum.value = 1; loadData() }
const resetSearch = () => { searchForm.deptId = ''; searchForm.expenseType = ''; searchForm.year = 2025; handleSearch() }
const handleAdd = () => { dialogTitle.value = '编制预算'; resetForm(); dialogVisible.value = true }

const handleAdjust = (row) => {
  dialogTitle.value = '调整预算'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该预算吗？', '提示', { type: 'warning' })
    await budgetApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saveLoading.value = true
      try {
        if (formData.id) {
          await budgetApi.update(formData.id, { budgetAmount: formData.budgetAmount })
          ElMessage.success('调整成功')
        } else {
          await budgetApi.save(formData)
          ElMessage.success('编制成功')
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
  Object.assign(formData, { id: null, deptId: '', expenseType: '', year: 2025, month: null, budgetAmount: null })
}

onMounted(() => { loadData() })
</script>

<style scoped>
.budget-manage { display: flex; flex-direction: column; gap: 20px; }
.search-card, .list-card { border-radius: 12px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
