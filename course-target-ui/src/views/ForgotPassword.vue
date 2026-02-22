<template>
  <div class="login-page">
    <div class="login-card">

      <div class="left-panel">
        <div class="brand-row">
          <div class="brand-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
              <path d="M12 3L1 9l11 6 11-6-11-6z" fill="#1E293B"/>
              <path d="M1 9v6" stroke="#1E293B" stroke-width="1.5" stroke-linecap="round"/>
              <path d="M5 11.5v5c0 0 2.5 2.5 7 2.5s7-2.5 7-2.5v-5" stroke="#1E293B" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h1 class="brand-title">课程目标达成情况管理系统</h1>
        </div>
        <ul class="feature-list">
          <li class="feature-item"><span class="bullet">•</span>覆盖课程目标制定、教学任务管理、达成度数据统计与分析，支持课程全过程质量监控。</li>
          <li class="feature-item"><span class="bullet">•</span>面向任课教师与系主任，实现多角色协同与权限管理。</li>
          <li class="feature-item"><span class="bullet">•</span>基于数据驱动教学改进，辅助课程持续建设与教学质量提升。</li>
        </ul>
      </div>

      <!-- RIGHT panel: 2-step forgot password -->
      <div class="right-panel">
        <div class="back-link" @click="router.push('/login')">← 返回登录</div>

        <!-- Step indicator -->
        <div class="steps-bar">
          <div class="step-circle" :class="{ active: step >= 1, done: step > 1 }">1</div>
          <div class="step-line" :class="{ done: step > 1 }"></div>
          <div class="step-circle" :class="{ active: step >= 2 }">2</div>
        </div>

        <div class="form-header">
          <h2 class="form-title">找回密码</h2>
          <p class="form-subtitle" v-if="step === 1">请输入您的注册用户名</p>
          <p class="form-subtitle" v-else>请设置新密码</p>
        </div>

        <!-- Step 1 -->
        <template v-if="step === 1">
          <div class="field-group">
            <label class="field-label">旧用户名：</label>
            <input v-model="form.username" class="custom-input" placeholder="请输入用户名" type="text" @keyup.enter="nextStep" />
          </div>
          <button class="login-btn" :class="{ loading }" @click="nextStep">
            <span v-if="!loading">下一步</span>
            <span v-else>验证中...</span>
          </button>
        </template>

        <!-- Step 2 -->
        <template v-else>
          <div class="field-group">
            <label class="field-label">新密码：</label>
            <input v-model="form.newPassword" class="custom-input" placeholder="请输入新密码" type="password" />
          </div>
          <div class="field-group">
            <label class="field-label">确认密码：</label>
            <input v-model="form.confirmPassword" class="custom-input" placeholder="请再次输入新密码" type="password" @keyup.enter="doReset" />
          </div>
          <div class="step-actions">
            <button class="btn-back" @click="step = 1">上一步</button>
            <button class="login-btn flex-1" :class="{ loading }" @click="doReset">
              <span v-if="!loading">确认修改</span>
              <span v-else>提交中...</span>
            </button>
          </div>
        </template>

        <div class="register-row" style="margin-top:16px">记起密码了？<a class="blue-link" @click.prevent="router.push('/login')">立即登录</a></div>
      </div>
    </div>
    <transition name="fade">
      <div class="error-toast" v-if="msg" :class="msgType">{{ msg }}</div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { resetPassword } from '@/api/auth'

const router = useRouter()
const step = ref(1)
const loading = ref(false)
const msg = ref('')
const msgType = ref('error')
const form = reactive({ username: '', newPassword: '', confirmPassword: '' })

let timer = null
function showMsg(text, type = 'error') {
  msg.value = text; msgType.value = type
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => { msg.value = '' }, 3000)
}

function nextStep() {
  if (!form.username) { showMsg('请输入用户名'); return }
  step.value = 2
}

async function doReset() {
  if (!form.newPassword) { showMsg('请输入新密码'); return }
  if (form.newPassword !== form.confirmPassword) { showMsg('两次密码不一致'); return }
  loading.value = true
  try {
    await resetPassword({ username: form.username, newPassword: form.newPassword })
    showMsg('密码修改成功，即将跳转登录', 'success')
    setTimeout(() => router.push('/login'), 1500)
  } catch (e) {
    showMsg(e.message || '修改失败，请检查用户名是否正确')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { position: fixed; inset: 0; display: flex; align-items: center; justify-content: center; overflow: hidden; background-image: url('/login-bg.png'); background-size: cover; background-position: center; background-repeat: no-repeat; background-color: #dde8f5; }
.login-card { position: relative; z-index: 1; display: flex; width: 860px; min-height: 480px; border-radius: 16px; overflow: hidden; box-shadow: 0 8px 40px rgba(0,0,0,.12); }
.left-panel { flex: 1; background: rgba(238,242,247,.88); backdrop-filter: blur(12px); padding: 56px 44px; display: flex; flex-direction: column; justify-content: center; }
.brand-row { display: flex; align-items: center; gap: 14px; margin-bottom: 36px; }
.brand-icon { width: 46px; height: 46px; background: #fff; border-radius: 10px; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(0,0,0,.08); flex-shrink: 0; }
.brand-title { font-size: 20px; font-weight: 700; color: #1e293b; line-height: 1.35; }
.feature-list { list-style: none; display: flex; flex-direction: column; gap: 18px; }
.feature-item { display: flex; gap: 10px; font-size: 13px; color: #475569; line-height: 1.7; }
.bullet { color: #64748b; font-size: 16px; flex-shrink: 0; }
.right-panel { width: 380px; flex-shrink: 0; background: #fff; padding: 40px 44px; display: flex; flex-direction: column; justify-content: center; }
.back-link { font-size: 13px; color: #2563eb; cursor: pointer; margin-bottom: 16px; display: inline-block; }
.back-link:hover { text-decoration: underline; }
/* Steps */
.steps-bar { display: flex; align-items: center; justify-content: center; gap: 0; margin-bottom: 20px; }
.step-circle { width: 28px; height: 28px; border-radius: 50%; background: #e2e8f0; color: #94a3b8; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 700; transition: all .3s; }
.step-circle.active { background: #2563eb; color: #fff; }
.step-circle.done { background: #22c55e; color: #fff; }
.step-line { width: 48px; height: 2px; background: #e2e8f0; transition: all .3s; }
.step-line.done { background: #22c55e; }
.form-header { margin-bottom: 24px; text-align: center; }
.form-title { font-size: 30px; font-weight: 700; color: #1e293b; margin-bottom: 6px; }
.form-subtitle { font-size: 13px; color: #94a3b8; }
.field-group { margin-bottom: 16px; }
.field-label { display: block; font-size: 13px; color: #374151; font-weight: 500; margin-bottom: 6px; }
.custom-input { width: 100%; height: 38px; padding: 0 12px; border: 1px solid #e2e8f0; border-radius: 6px; font-size: 14px; color: #1e293b; background: #fff; outline: none; transition: border-color .2s; box-sizing: border-box; font-family: inherit; }
.custom-input:focus { border-color: #2563eb; box-shadow: 0 0 0 2px rgba(37,99,235,.1); }
.custom-input::placeholder { color: #cbd5e1; }
.step-actions { display: flex; gap: 10px; align-items: center; }
.btn-back { height: 40px; padding: 0 16px; background: #f1f5f9; color: #475569; border: none; border-radius: 6px; font-size: 14px; cursor: pointer; font-family: inherit; }
.btn-back:hover { background: #e2e8f0; }
.flex-1 { flex: 1; }
.login-btn { width: 100%; height: 40px; background: #2563eb; color: #fff; border: none; border-radius: 6px; font-size: 15px; font-weight: 600; cursor: pointer; transition: background .2s; font-family: inherit; margin-bottom: 0; }
.login-btn:hover:not(.loading) { background: #1d4ed8; }
.login-btn.loading { background: #93c5fd; cursor: not-allowed; }
.blue-link { color: #2563eb; text-decoration: none; cursor: pointer; }
.blue-link:hover { text-decoration: underline; }
.register-row { text-align: center; font-size: 13px; color: #6b7280; }
.error-toast { position: fixed; top: 24px; left: 50%; transform: translateX(-50%); padding: 10px 24px; border-radius: 8px; font-size: 14px; z-index: 9999; }
.error-toast.error { background: #ef4444; color: #fff; }
.error-toast.success { background: #22c55e; color: #fff; }
.fade-enter-active, .fade-leave-active { transition: opacity .3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
