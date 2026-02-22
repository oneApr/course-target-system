<template>
  <div class="main-layout">
    <!-- WHITE Sidebar -->
    <aside class="sidebar" ref="sidebarRef">
      <!-- Canvas for particle effects -->
      <canvas ref="particleCanvas" class="particle-canvas" />

      <div class="sidebar-logo">
        <div class="logo-icon-wrap">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M12 3L1 9l11 6 11-6-11-6z" fill="#2563eb"/>
            <path d="M1 9v6" stroke="#2563eb" stroke-width="1.5" stroke-linecap="round"/>
            <path d="M5 11.5v5c0 0 2.5 2.5 7 2.5s7-2.5 7-2.5v-5" stroke="#2563eb" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <span class="logo-text">课程目标达成系统</span>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          active-class="nav-item--active"
          @click.native="onNavClick($event, item)"
          @click="onNavClick($event, item)"
        >
          <span class="nav-indicator" />
          <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          <span class="nav-label">{{ item.label }}</span>
          <span class="nav-ripple" />
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="user-row">
          <div class="user-avatar">
            <img
              :src="userStore.role === 'director' ? '/director.svg' : '/teacher.svg'"
              alt="avatar"
              class="avatar-img"
            />
          </div>
          <div class="user-info">
            <div class="user-name">{{ userStore.displayName }}</div>
            <div class="user-role">{{ userStore.roleLabel }}</div>
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </button>
      </div>
    </aside>

    <!-- Main content -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const sidebarRef = ref(null)
const particleCanvas = ref(null)

const adminMenuItems = [
  { path: '/dashboard', label: '首页', icon: 'HomeFilled' },
  { path: '/course-tasks', label: '课程任务查看', icon: 'Tickets' },
  { path: '/upload', label: '达成数据上传', icon: 'Upload' },
  { path: '/query', label: '达成情况查询', icon: 'Search' },
]

const directorMenuItems = [
  { path: '/dashboard', label: '首页', icon: 'HomeFilled' },
  { path: '/course-objectives', label: '课程目标管理', icon: 'Reading' },
  { path: '/objective-maintenance', label: '课程目标维护', icon: 'Edit' },
  { path: '/course-task-management', label: '课程任务管理', icon: 'Management' },
  { path: '/course-tasks', label: '课程任务查看', icon: 'Tickets' },
  { path: '/teacher-management', label: '教师信息管理', icon: 'UserFilled' },
  { path: '/data-audit', label: '达成数据审核', icon: 'CircleCheck' },
  { path: '/visualization', label: '数据可视化', icon: 'DataLine' },
  { path: '/query', label: '达成情况查询', icon: 'Search' },
]

const menuItems = computed(() =>
  userStore.role === 'director' ? directorMenuItems : adminMenuItems
)

async function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

let animFrame = null
let particles = []

const COLORS = ['#3b82f6', '#60a5fa', '#93c5fd', '#bfdbfe', '#2563eb', '#818cf8', '#a78bfa']

class Particle {
  constructor(x, y) {
    this.x = x
    this.y = y
    const angle = Math.random() * Math.PI * 2
    const speed = 1 + Math.random() * 3.5
    this.vx = Math.cos(angle) * speed
    this.vy = Math.sin(angle) * speed
    this.size = 2 + Math.random() * 4
    this.alpha = 1
    this.decay = 0.025 + Math.random() * 0.025
    this.color = COLORS[Math.floor(Math.random() * COLORS.length)]
    this.shape = Math.random() > 0.5 ? 'circle' : 'square'
  }
  update() {
    this.x += this.vx
    this.y += this.vy
    this.vy += 0.08   
    this.vx *= 0.97
    this.alpha -= this.decay
    this.size *= 0.97
  }
  draw(ctx) {
    ctx.save()
    ctx.globalAlpha = Math.max(0, this.alpha)
    ctx.fillStyle = this.color
    if (this.shape === 'circle') {
      ctx.beginPath()
      ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
      ctx.fill()
    } else {
      ctx.fillRect(this.x - this.size / 2, this.y - this.size / 2, this.size, this.size)
    }
    ctx.restore()
  }
}

function burst(x, y, count = 28) {
  for (let i = 0; i < count; i++) {
    particles.push(new Particle(x, y))
  }
}

function animate() {
  const canvas = particleCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  particles = particles.filter(p => p.alpha > 0)
  particles.forEach(p => { p.update(); p.draw(ctx) })
  animFrame = requestAnimationFrame(animate)
}

function resizeCanvas() {
  const canvas = particleCanvas.value
  const sidebar = sidebarRef.value
  if (!canvas || !sidebar) return
  canvas.width = sidebar.offsetWidth
  canvas.height = sidebar.offsetHeight
}

function onNavClick(event, item) {
  const sidebar = sidebarRef.value
  if (!sidebar) return
  const rect = sidebar.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top
  burst(x, y, 32)
}

onMounted(() => {
  resizeCanvas()
  animate()
  window.addEventListener('resize', resizeCanvas)
})
onUnmounted(() => {
  if (animFrame) cancelAnimationFrame(animFrame)
  window.removeEventListener('resize', resizeCanvas)
})
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}


.sidebar {
  position: relative;
  width: 200px;
  min-width: 200px;
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}


.particle-canvas {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.sidebar-logo {
  position: relative;
  z-index: 1;
  padding: 18px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #f1f5f9;
}

.logo-icon-wrap {
  width: 32px;
  height: 32px;
  background: #f1f5f9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s, transform 0.2s;
}
.logo-icon-wrap:hover {
  background: #e0e7ff;
  transform: rotate(-5deg) scale(1.08);
}

.logo-text {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.3;
}

/* Navigation */
.sidebar-nav {
  position: relative;
  z-index: 1;
  flex: 1;
  padding: 10px 8px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border-radius: 8px;
  color: #64748b;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.18s, background 0.18s, transform 0.15s;
  overflow: hidden;
  cursor: pointer;
}

.nav-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  width: 3px;
  height: 60%;
  border-radius: 0 3px 3px 0;
  background: #2563eb;
  transition: transform 0.2s cubic-bezier(.34,1.56,.64,1);
}

.nav-item:hover .nav-indicator {
  transform: translateY(-50%) scaleY(0.5);
}

.nav-item--active .nav-indicator {
  transform: translateY(-50%) scaleY(1) !important;
}

.nav-item:hover {
  background: #f8fafc;
  color: #1e293b;
  transform: translateX(2px);
}

.nav-item--active {
  background: #eff6ff;
  color: #2563eb !important;
  font-weight: 600;
  transform: translateX(4px);
}

.nav-item:active {
  transform: translateX(2px) scale(0.97);
  transition: transform 0.08s;
}

.nav-icon {
  font-size: 15px;
  min-width: 15px;
  transition: transform 0.2s cubic-bezier(.34,1.56,.64,1);
}

.nav-item:hover .nav-icon {
  transform: scale(1.15) rotate(-5deg);
}

.nav-item--active .nav-icon {
  transform: scale(1.1);
}

.nav-label {
  white-space: nowrap;
}


.sidebar-footer {
  position: relative;
  z-index: 1;
  padding: 10px 8px;
  border-top: 1px solid #f1f5f9;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  margin-bottom: 4px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info { flex: 1; overflow: hidden; }
.user-name { font-size: 13px; font-weight: 600; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.user-role { font-size: 11px; color: #94a3b8; }

.logout-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 12px;
  background: none;
  border: none;
  border-radius: 8px;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
}
.logout-btn:hover {
  background: #fef2f2;
  color: #ef4444;
  transform: translateX(2px);
}

.main-content {
  flex: 1;
  overflow-y: auto;
  background: #f8fafc;
}
</style>
