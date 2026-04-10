import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/',
        component: () => import('@/views/Layout.vue'),
        redirect: '/workbench',
        children: [
            {
                path: 'workbench',
                name: 'Workbench',
                component: () => import('@/views/common/Workbench.vue'),
                meta: { title: '工作台', requiresAuth: true }
            },
            {
                path: 'my-expense',
                name: 'MyExpense',
                component: () => import('@/views/common/MyExpense.vue'),
                meta: { title: '我的报销', requiresAuth: true }
            },
            {
                path: 'my-invoice',
                name: 'MyInvoice',
                component: () => import('@/views/common/MyInvoice.vue'),
                meta: { title: '我的发票', requiresAuth: true }
            },
            {
                path: 'my-info',
                name: 'MyInfo',
                component: () => import('@/views/common/MyInfo.vue'),
                meta: { title: '我的信息', requiresAuth: true }
            },
            {
                path: 'my-approval',
                name: 'MyApproval',
                component: () => import('@/views/finance/MyApproval.vue'),
                meta: { title: '我的审批', requiresAuth: true }
            },
            {
                path: 'budget-manage',
                name: 'BudgetManage',
                component: () => import('@/views/finance/BudgetManage.vue'),
                meta: { title: '预算管理', requiresAuth: true }
            },
            {
                path: 'invoice-manage',
                name: 'InvoiceManage',
                component: () => import('@/views/finance/InvoiceManage.vue'),
                meta: { title: '发票管理', requiresAuth: true }
            },
            {
                path: 'employee-info',
                name: 'EmployeeInfo',
                component: () => import('@/views/finance/EmployeeInfo.vue'),
                meta: { title: '员工信息', requiresAuth: true }
            },
            {
                path: 'my-expense',
                name: 'MyExpense',
                component: () => import('@/views/finance/MyExpense.vue'),  // 使用 finance 目录
                meta: { title: '我的报销', requiresAuth: true }
            },
            {
                path: 'my-invoice',
                name: 'MyInvoice',
                component: () => import('@/views/finance/MyInvoice.vue'),  // 使用 finance 目录
                meta: { title: '我的发票', requiresAuth: true }
            },
            {
                path: 'my-info',
                name: 'MyInfo',
                component: () => import('@/views/finance/MyInfo.vue'),  // 使用 finance 目录
                meta: { title: '我的信息', requiresAuth: true }
            },
            {
                path: 'my-approval',
                name: 'MyApproval',
                component: () => import('@/views/admin/MyApproval.vue'),  // 使用 admin 目录
                meta: { title: '我的审批', requiresAuth: true }
            },
            {
                path: 'personnel-manage',
                name: 'PersonnelManage',
                component: () => import('@/views/admin/PersonnelManage.vue'),
                meta: { title: '人员管理', requiresAuth: true }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫：权限控制
router.beforeEach(async (to, from, next) => {
    const token = localStorage.getItem('token')
    const userStore = useUserStore()

    if (to.path === '/login') {
        if (token) {
            const isValid = await userStore.getUserInfo()
            if (isValid) {
                next('/workbench')
            } else {
                next()
            }
        } else {
            next()
        }
        return
    }

    if (!token) {
        next('/login')
        return
    }

    if (!userStore.userInfo) {
        const isValid = await userStore.getUserInfo()
        if (!isValid) {
            next('/login')
            return
        }
    }

    next()
})

export default router
