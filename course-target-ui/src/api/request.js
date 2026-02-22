import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: '/fmk',        // Vite proxy 转发到 http://localhost:8088/fmk
    timeout: 15000
})

// ── 请求拦截：自动注入 Token 
request.interceptors.request.use(config => {
    let token = localStorage.getItem('token')
    // 过滤掉 'null' 字符串或非法的乱码 token，防止 Axios 抛出 String contains non ISO-8859-1 code point
    if (token && token !== 'null' && token !== 'undefined' && /^[\x20-\x7E]+$/.test(token)) {
        config.headers['Authorization'] = `Bearer ${token}`
    } else if (token === 'null' || token === 'undefined') {
        localStorage.removeItem('token') // 清除错误缓存
    }
    return config
}, error => Promise.reject(error))

// ── 响应拦截：统一处理业务错误码 
request.interceptors.response.use(res => {
    const data = res.data
    if (data.code === 200) {
        return data   // 返回整个 { code, msg, data } 对象，调用方用 .data 取值
    }
    // 401 —— 未登录/Token 过期
    if (data.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        window.location.href = '/#/login'
        return Promise.reject(new Error(data.msg))
    }
    ElMessage.error(data.msg || '请求失败')
    return Promise.reject(new Error(data.msg))
}, error => {
    ElMessage.error(error.response?.data?.msg || error.message || '网络异常，请稍后重试')
    return Promise.reject(error)
})

export default request
