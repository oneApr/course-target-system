import request from './request'

/** 登录 */
export function login(data) {
    return request.post('/api/auth/login', data)
}

/** 注册 */
export function register(data) {
    return request.post('/api/auth/register', data)
}

/** 忘记密码 / 重置密码 */
export function resetPassword(data) {
    return request.put('/api/auth/reset-password', data)
}

/** 获取当前用户信息 + 菜单 */
export function getUserInfo() {
    return request.get('/api/auth/info')
}

/** 退出登录 */
export function logout() {
    return request.post('/api/auth/logout')
}
