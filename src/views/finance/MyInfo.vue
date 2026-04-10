<template>
  <div class="finance-info">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="avatar-card">
          <div class="avatar-wrapper">
            <el-avatar :size="120" :src="avatarUrl" :icon="UserFilled" />
            <el-upload
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleAvatarChange"
                class="avatar-upload"
            >
              <el-button type="primary" size="small" style="margin-top: 16px;">更换头像</el-button>
            </el-upload>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
              <el-button type="primary" @click="handleEdit" v-if="!editing">编辑</el-button>
              <span v-else>
                                <el-button @click="cancelEdit">取消</el-button>
                                <el-button type="primary" @click="handleSave" :loading="saveLoading">保存</el-button>
                            </span>
            </div>
          </template>
          <el-form :model="formData" :disabled="!editing" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="formData.username" disabled />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="formData.name" />
            </el-form-item>
            <el-form-item label="部门">
              <el-input v-model="formData.deptName" disabled />
            </el-form-item>
            <el-form-item label="职位">
              <el-input v-model="formData.position" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="formData.phone" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="formData.email" />
            </el-form-item>
            <el-form-item label="入职日期">
              <el-input v-model="formData.entryDate" disabled />
            </el-form-item>
          </el-form>

          <el-divider />

          <div class="password-section">
            <h4>修改密码</h4>
            <el-form :model="pwdForm" ref="pwdFormRef" label-width="100px">
              <el-form-item label="原密码">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password style="width: 300px" />
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="pwdForm.newPassword" type="password" show-password style="width: 300px" />
              </el-form-item>
              <el-form-item label="确认密码">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password style="width: 300px" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { profileApi } from '@/api'
import { UserFilled } from '@element-plus/icons-vue'

const editing = ref(false)
const saveLoading = ref(false)
const avatarUrl = ref('')

const formData = reactive({
  username: '', name: '', deptName: '', position: '', phone: '', email: '', entryDate: ''
})

const pwdForm = reactive({
  oldPassword: '', newPassword: '', confirmPassword: ''
})

const loadUserInfo = async () => {
  try {
    const res = await profileApi.getProfile()
    if (res.code === 200) {
      Object.assign(formData, res.data)
      avatarUrl.value = res.data.avatar || ''
    }
  } catch (error) {
    ElMessage.error('加载个人信息失败')
  }
}

const handleEdit = () => { editing.value = true }
const cancelEdit = () => { loadUserInfo(); editing.value = false }

const handleSave = async () => {
  saveLoading.value = true
  try {
    await profileApi.updateProfile({
      name: formData.name,
      phone: formData.phone,
      email: formData.email
    })
    ElMessage.success('保存成功')
    editing.value = false
    loadUserInfo()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

const handleChangePassword = async () => {
  if (!pwdForm.newPassword || !pwdForm.oldPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  try {
    await profileApi.changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
  }
}

const handleAvatarChange = async (file) => {
  try {
    const res = await profileApi.uploadAvatar(file.raw)
    if (res.code === 200) {
      avatarUrl.value = res.data.avatarUrl
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    ElMessage.error('头像上传失败')
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.finance-info { padding: 20px; }
.avatar-card { text-align: center; border-radius: 12px; }
.avatar-wrapper { display: flex; flex-direction: column; align-items: center; padding: 20px 0; }
.info-card { border-radius: 12px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.password-section { margin-top: 20px; }
.password-section h4 { margin-bottom: 20px; }
</style>