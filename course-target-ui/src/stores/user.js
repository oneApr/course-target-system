import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, logout as apiLogout, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

    const isLoggedIn = computed(() => !!token.value)
    // 后端角色：director = 系主任，teacher = 任课教师
    const role = computed(() => userInfo.value?.role || '')
    const username = computed(() => userInfo.value?.username || '')
    const displayName = computed(() => userInfo.value?.displayName || '')
    const userId = computed(() => userInfo.value?.userId || null)

    /** 登录：调用后端接口，token 存入 localStorage */
    async function login(usernameInput, passwordInput) {
        try {
            const res = await apiLogin({ username: usernameInput, password: passwordInput })
            const data = res.data
            token.value = data.token
            localStorage.setItem('token', data.token)

            // 获取当前用户信息+菜单
            const infoRes = await getUserInfo()
            const infoData = infoRes.data
            userInfo.value = {
                userId: infoData.userId,
                username: infoData.username,
                displayName: infoData.displayName,
                role: infoData.roleKey,
                menus: infoData.menus || []
            }
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
            return { success: true }
        } catch (e) {
            return { success: false, message: e.message || '登录失败' }
        }
    }

    /** 退出登录 */
    async function logout() {
        try { await apiLogout() } catch (_) { }
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    return {
        token, userInfo,
        isLoggedIn, role, username, displayName, userId,
        login, logout
    }
}, { persist: false })
