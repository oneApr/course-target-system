<template>
  <div class="login-page">

    <!-- Centered two-panel card -->
    <div class="login-card">

      <!-- LEFT panel: frosted glass, system intro -->
      <div class="left-panel">
        <div class="brand-row">
          <div class="brand-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 3L1 9l11 6 11-6-11-6z" fill="#1E293B" stroke="#1E293B" stroke-width="0.5" stroke-linejoin="round"/>
              <path d="M1 9v6" stroke="#1E293B" stroke-width="1.5" stroke-linecap="round"/>
              <path d="M5 11.5v5c0 0 2.5 2.5 7 2.5s7-2.5 7-2.5v-5" stroke="#1E293B" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h1 class="brand-title">课程目标达成情况管理系统</h1>
        </div>

        <ul class="feature-list">
          <li class="feature-item">
            <span class="bullet">•</span>
            覆盖课程目标制定、教学任务管理、达成度数据统计与分析，支持课程全过程质量监控。
          </li>
          <li class="feature-item">
            <span class="bullet">•</span>
            面向任课教师与系主任，实现多角色协同与权限管理。
          </li>
          <li class="feature-item">
            <span class="bullet">•</span>
            基于数据驱动教学改进，辅助课程持续建设与教学质量提升。
          </li>
        </ul>
      </div>

      <!-- RIGHT panel: pure white, login form -->
      <div class="right-panel">
        <div class="form-header">
          <h2 class="form-title">欢迎使用</h2>
          <p class="form-subtitle">请登录您的账号</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent>
          <div class="field-group">
            <label class="field-label">用户名</label>
            <el-form-item prop="username" class="bare-form-item">
              <input
                v-model="form.username"
                class="custom-input"
                placeholder="请输入用户名"
                type="text"
                @keyup.enter="handleLogin"
              />
            </el-form-item>
          </div>

          <div class="field-group">
            <label class="field-label">密码</label>
            <el-form-item prop="password" class="bare-form-item">
              <input
                v-model="form.password"
                class="custom-input"
                placeholder="请输入密码"
                type="password"
                @keyup.enter="handleLogin"
              />
            </el-form-item>
          </div>

          <div class="options-row">
            <label class="remember-label">
              <input type="checkbox" v-model="form.remember" class="checkbox" />
              <span>记住密码</span>
            </label>
            <a class="forgot-link" @click.prevent="goForgot">忘记密码?</a>
          </div>

          <button
            class="login-btn"
            :class="{ loading: loading }"
            @click="handleLogin"
          >
            <span v-if="!loading">登录</span>
            <span v-else>登录中...</span>
          </button>

          <div class="agreement-row">
            <input type="checkbox" v-model="form.agreed" class="checkbox" />
            <span class="agreement-text">
              已阅读并同意<a href="#" class="blue-link">《用户协议》</a>和<a href="#" class="blue-link">《隐私政策》</a>
            </span>
          </div>

          <div class="register-row">
            还没有账号？<a class="blue-link" @click.prevent="goRegister" style="cursor:pointer">立即注册</a>
          </div>
        </el-form>
      </div>
    </div>

    <!-- Error message -->
    <transition name="fade">
      <div class="error-toast" v-if="errorMsg">{{ errorMsg }}</div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const goRegister = () => router.push('/register')
const goForgot = () => router.push('/forgot-password')
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const errorMsg = ref('')

const form = reactive({
  username: '',
  password: '',
  remember: false,
  agreed: false
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

let errorTimer = null
function showError(msg) {
  errorMsg.value = msg
  if (errorTimer) clearTimeout(errorTimer)
  errorTimer = setTimeout(() => { errorMsg.value = '' }, 3000)
}

async function handleLogin() {
  if (!form.username) { showError('请输入用户名'); return }
  if (!form.password) { showError('请输入密码'); return }
  loading.value = true
  // 调用后端真实接口
  const result = await userStore.login(form.username, form.password)
  loading.value = false
  if (result.success) {
    router.push('/dashboard')
  } else {
    showError(result.message || '用户名或密码错误')
  }
}
</script>

<style scoped>
/* ===== Full-page background ===== */
.login-page {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background-image: url('/login-bg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-color: #dde8f5;
}

/* ===== Two-panel card ===== */
.login-card {
  position: relative;
  z-index: 1;
  display: flex;
  width: 860px;
  min-height: 480px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* ===== LEFT panel ===== */
.left-panel {
  flex: 1;
  background: rgba(238, 242, 247, 0.88);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 56px 44px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 36px;
}

.brand-icon {
  width: 46px;
  height: 46px;
  background: #ffffff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  flex-shrink: 0;
}

.brand-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.35;
  letter-spacing: 0;
}

.feature-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.feature-item {
  display: flex;
  gap: 10px;
  font-size: 13px;
  color: #475569;
  line-height: 1.7;
}

.bullet {
  color: #64748b;
  font-size: 16px;
  line-height: 1.5;
  flex-shrink: 0;
  margin-top: 1px;
}

/* ===== RIGHT panel ===== */
.right-panel {
  width: 380px;
  flex-shrink: 0;
  background: #ffffff;
  padding: 48px 44px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 28px;
  text-align: center;
}

.form-title {
  font-size: 30px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 6px;
}

.form-subtitle {
  font-size: 13px;
  color: #94a3b8;
}

/* ===== Form fields ===== */
.field-group {
  margin-bottom: 18px;
}

.field-label {
  display: block;
  font-size: 13px;
  color: #374151;
  font-weight: 500;
  margin-bottom: 7px;
}

.bare-form-item {
  margin-bottom: 0 !important;
}

.bare-form-item :deep(.el-form-item__content) {
  display: block;
}

.bare-form-item :deep(.el-form-item__error) {
  font-size: 11px;
  margin-top: 3px;
}

.custom-input {
  width: 100%;
  height: 38px;
  padding: 0 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  color: #1e293b;
  background: #ffffff;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
  box-sizing: border-box;
}

.custom-input::placeholder {
  color: #cbd5e1;
}

.custom-input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.1);
}

/* ===== Options row ===== */
.options-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.remember-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
}

.checkbox {
  width: 14px;
  height: 14px;
  cursor: pointer;
  accent-color: #2563eb;
}

.forgot-link {
  font-size: 13px;
  color: #2563eb;
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}

/* ===== Login button ===== */
.login-btn {
  width: 100%;
  height: 40px;
  background: #2563eb;
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  font-family: inherit;
  margin-bottom: 18px;
}

.login-btn:hover:not(.loading) {
  background: #1d4ed8;
}

.login-btn.loading {
  background: #93c5fd;
  cursor: not-allowed;
}

/* ===== Agreement ===== */
.agreement-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 12px;
}

.agreement-text {
  line-height: 1.5;
}

.blue-link {
  color: #2563eb;
  text-decoration: none;
}

.blue-link:hover {
  text-decoration: underline;
}

/* ===== Register ===== */
.register-row {
  text-align: center;
  font-size: 13px;
  color: #6b7280;
}

/* ===== Error toast ===== */
.error-toast {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  background: #ef4444;
  color: white;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  z-index: 9999;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
