import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', () => {
    const userInfo = ref(null)
    const token = ref(localStorage.getItem('token') || '')

    // 登录 - 调用真实API
    const login = async (username, password) => {
        try {
            const res = await authApi.login({ username, password })
            if (res.code === 200) {
                const { token: userToken, userInfo: user } = res.data
                token.value = userToken
                userInfo.value = user

                localStorage.setItem('token', userToken)
                localStorage.setItem('userRole', user.roleCode)
                localStorage.setItem('userInfo', JSON.stringify(user))

                return user
            }
            throw new Error(res.message)
        } catch (error) {
            throw error
        }
    }

    // 获取当前用户信息（验证token）
    const getUserInfo = async () => {
        if (!token.value) return null
        try {
            const res = await authApi.getCurrentUser()
            if (res.code === 200) {
                userInfo.value = res.data
                return res.data
            }
        } catch (error) {
            logout()
            return null
        }
    }

    // 登出
    const logout = async () => {
        try {
            await authApi.logout()
        } catch (error) {
            console.error('登出失败', error)
        } finally {
            userInfo.value = null
            token.value = ''
            localStorage.removeItem('token')
            localStorage.removeItem('userRole')
            localStorage.removeItem('userInfo')
        }
    }

    // 恢复登录状态
    const restoreUser = async () => {
        const savedToken = localStorage.getItem('token')
        if (savedToken) {
            token.value = savedToken
            await getUserInfo()
        }
    }

    return { userInfo, token, login, logout, restoreUser, getUserInfo }
})