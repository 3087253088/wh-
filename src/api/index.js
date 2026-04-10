import request from '@/utils/request'

// ==================== 认证模块 ====================
export const authApi = {
    login: (data) => request.post('/auth/login', data),
    getCurrentUser: () => request.get('/auth/current'),
    logout: () => request.post('/auth/logout')
}

// ==================== 报销模块 ====================
export const expenseApi = {
    getList: (params) => request.get('/expense/list', { params }),
    getDetail: (id) => request.get(`/expense/${id}`),
    save: (data) => request.post('/expense', data),
    update: (id, data) => request.put(`/expense/${id}`, data),
    delete: (id) => request.delete(`/expense/${id}`),
    submit: (id) => request.post(`/expense/${id}/submit`),
    recall: (id) => request.post(`/expense/${id}/recall`)
}

// ==================== 发票模块 ====================
export const invoiceApi = {
    getMyList: (params) => request.get('/invoice/list', { params }),
    getAllList: (params) => request.get('/invoice/all', { params }),
    save: (data) => request.post('/invoice', data),
    delete: (id) => request.delete(`/invoice/${id}`),
    verify: (id) => request.post(`/invoice/${id}/verify`),
    batchVerify: (ids) => request.post('/invoice/batch-verify', { ids })
}

// ==================== 审批模块 ====================
export const approvalApi = {
    getPending: (params) => request.get('/approval/pending', { params }),
    getHistory: (params) => request.get('/approval/history', { params }),
    approve: (taskId, data) => request.post(`/approval/${taskId}`, data)
}

// ==================== 预算模块 ====================
export const budgetApi = {
    getList: (params) => request.get('/budget/list', { params }),
    save: (data) => request.post('/budget', data),
    update: (id, data) => request.put(`/budget/${id}`, data),
    delete: (id) => request.delete(`/budget/${id}`),
    getStatistics: (params) => request.get('/budget/statistics', { params })
}

// ==================== 员工模块 ====================
export const employeeApi = {
    getList: (params) => request.get('/employee/list', { params }),
    getDetail: (id) => request.get(`/employee/${id}`),
    save: (data) => request.post('/employee', data),
    update: (id, data) => request.put(`/employee/${id}`, data),
    updateStatus: (id, status) => request.put(`/employee/${id}/status`, { status })
}

// ==================== 用户管理模块（管理员） ====================
export const userApi = {
    getList: (params) => request.get('/user/list', { params }),
    save: (data) => request.post('/user', data),
    update: (id, data) => request.put(`/user/${id}`, data),
    updateStatus: (id, status) => request.put(`/user/${id}/status`, { status }),
    resetPassword: (id) => request.put(`/user/${id}/reset-password`),
    assignRole: (id, roleCode) => request.put(`/user/${id}/role`, { roleCode })
}

// ==================== 个人信息模块 ====================
export const profileApi = {
    getProfile: () => request.get('/user/profile'),
    updateProfile: (data) => request.put('/user/profile', data),
    changePassword: (data) => request.put('/user/password', data),
    uploadAvatar: (file) => {
        const formData = new FormData()
        formData.append('file', file)
        return request.post('/user/avatar', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
    }
}

// ==================== 工作台模块 ====================
export const dashboardApi = {
    getStatistics: () => request.get('/dashboard/statistics'),
    getTodos: () => request.get('/dashboard/todos'),
    getExpenseTrend: (months) => request.get('/dashboard/expense-trend', { params: { months } })
}