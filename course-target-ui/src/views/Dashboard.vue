<template>
  <div class="page-container">
    <!-- Welcome header -->
    <div class="welcome-header">
      <div>
        <div class="page-title">欢迎回来，{{ userStore.displayName }}！</div>
        <div class="page-subtitle">当前角色：{{ userStore.roleLabel }} | 这是您的工作概览和最近活动</div>
      </div>
    </div>

    <!-- Stat cards -->
    <div class="stat-cards">
      <template v-if="!isDirector">
        <div class="stat-card">
          <div class="stat-card-info"><div class="stat-label">授课课程</div><div class="stat-value">{{ courseStore.courses.length }}</div></div>
          <div class="stat-card-icon icon-blue"><el-icon><Reading /></el-icon></div>
        </div>
        <div class="stat-card">
          <div class="stat-card-info"><div class="stat-label">学生总数</div><div class="stat-value">{{ totalStudents }}</div></div>
          <div class="stat-card-icon icon-green"><el-icon><UserFilled /></el-icon></div>
        </div>
        <div class="stat-card">
          <div class="stat-card-info"><div class="stat-label">已上传数据</div><div class="stat-value">{{ courseStore.uploadRecords.length }}</div></div>
          <div class="stat-card-icon icon-purple"><el-icon><Upload /></el-icon></div>
        </div>
        <div class="stat-card">
          <div class="stat-card-info"><div class="stat-label">待提交任务</div><div class="stat-value">{{ courseStore.courseTasks.filter(t=>t.status==='未提交').length }}</div></div>
          <div class="stat-card-icon icon-orange"><el-icon><Warning /></el-icon></div>
        </div>
      </template>
      <template v-else>
        <div class="stat-card">
          <div class="stat-card-info"><div class="stat-label">管理课程</div><div class="stat-value">{{ courseStore.courses.length }}</div></div>
          <div class="stat-card-icon icon-blue"><el-icon><Reading /></el-icon></div>
        </div>
        <div class="stat-card">
          <div class="stat-card-info"><div class="stat-label">教师人数</div><div class="stat-value">{{ teacherStore.teachers.length }}</div></div>
          <div class="stat-card-icon icon-green"><el-icon><UserFilled /></el-icon></div>
        </div>
        <div class="stat-card">
          <div class="stat-card-info"><div class="stat-label">待审核数据</div><div class="stat-value">{{ courseStore.uploadRecords.filter(r=>r.status==='待审核').length }}</div></div>
          <div class="stat-card-icon icon-orange"><el-icon><Clock /></el-icon></div>
        </div>
        <div class="stat-card">
          <div class="stat-card-info"><div class="stat-label">课程目标数</div><div class="stat-value">{{ courseStore.objectives.length }}</div></div>
          <div class="stat-card-icon icon-purple"><el-icon><Aim /></el-icon></div>
        </div>
      </template>
    </div>

    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
      <!-- My courses / Course overview -->
      <div class="section-card">
        <div class="section-header">
          <div class="section-title"><el-icon><Reading /></el-icon> {{ isDirector ? '课程概览' : '我的课程' }}</div>
          <router-link class="view-all" to="/course-tasks">查看全部 →</router-link>
        </div>
        <div class="sub-label">{{ isDirector ? '全系课程列表' : '正在教授的课程' }}</div>
        <div class="course-list">
          <div v-for="task in courseStore.courseTasks.slice(0, 3)" :key="task.id" class="course-item">
            <div class="course-item-left">
              <div class="course-name">{{ task.courseName }} <el-tag size="small" style="font-size:11px;margin-left:4px" type="info">{{ task.semester?.split('第')[0] }}</el-tag></div>
              <div class="course-meta">{{ task.studentCount ?? '-' }} 名学生</div>
            </div>
            <div class="course-item-right">
              <el-tag :type="task.status==='已审核'?'success':task.status==='已提交'?'warning':'info'" size="small">{{ task.status }}</el-tag>
              <div class="achieve-rate" :style="{color: task.achievementRate ? getColor(task.achievementRate) : '#94a3b8'}">
                达成度: {{ task.achievementRate != null ? task.achievementRate + '%' : '-' }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick actions -->
      <div class="section-card">
        <div class="section-header">
          <div class="section-title"><el-icon><Lightning /></el-icon> 快捷操作</div>
          <span class="sub-label-inline">常用功能快速访问</span>
        </div>
        <div class="quick-actions">
          <div class="quick-btn" @click="router.push('/upload')">
            <el-icon><Upload /></el-icon><span>上传达成数据</span>
          </div>
          <div class="quick-btn" @click="router.push('/query')">
            <el-icon><Document /></el-icon><span>提交分析报告</span>
          </div>
          <div class="quick-btn" @click="router.push('/query')">
            <el-icon><TrendCharts /></el-icon><span>查看达成情况</span>
          </div>
          <div class="quick-btn" @click="router.push('/course-tasks')">
            <el-icon><Tickets /></el-icon><span>查看教学任务</span>
          </div>
        </div>
      </div>

      <!-- Recent activities -->
      <div class="section-card">
        <div class="section-header">
          <div class="section-title"><el-icon><Clock /></el-icon> 最近活动</div>
          <span class="sub-label-inline">您的最近操作记录</span>
        </div>
        <div class="activity-list">
          <div class="activity-item" v-for="item in activities" :key="item.id">
            <div class="activity-dot" :class="'dot-'+item.type"></div>
            <div class="activity-content">
              <div class="activity-text">{{ item.text }}</div>
              <div class="activity-time">{{ item.time }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pending tasks -->
      <div class="section-card">
        <div class="section-header">
          <div class="section-title"><el-icon><Clock /></el-icon> 待办任务</div>
          <span class="sub-label-inline">需要您处理的任务</span>
        </div>
        <div class="todo-list">
          <div class="todo-item" v-for="todo in todos" :key="todo.id">
            <div class="todo-main">
              <div class="todo-text">{{ todo.text }}</div>
              <div class="todo-deadline">截止日期 {{ todo.deadline }}</div>
            </div>
            <el-tag :type="todo.priority==='高'?'danger':'info'" size="small">{{ todo.priority }}</el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCourseStore, useTeacherStore } from '@/stores/courses'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const courseStore = useCourseStore()
const teacherStore = useTeacherStore()
onMounted(async () => {
  await courseStore.fetchCourses()
  await courseStore.fetchCourseTasks()
  await courseStore.fetchUploadRecords()
  if (userStore.role === 'director') await teacherStore.fetchTeachers()
})
const isDirector = computed(() => userStore.role === 'director')
const totalStudents = computed(() => courseStore.courseTasks.reduce((s, t) => s + (t.studentCount || 0), 0))

function getColor(v) { return v >= 85 ? '#22c55e' : v >= 75 ? '#3b82f6' : v >= 60 ? '#f59e0b' : '#ef4444' }

const activities = [
  { id: 1, text: '上传了"高等数学A"课程达成数据', time: '2小时前 · 高等数学A', type: 'upload' },
  { id: 2, text: '审核了"线性代数"课程目标', time: '4小时前 · 线性代数', type: 'check' },
  { id: 3, text: '提交了课程达成分析报告', time: '1天前 · 概率论与数理统计', type: 'submit' },
  { id: 4, text: '分配了教学任务', time: '2天前 · 数据结构', type: 'assign' },
]

const todos = [
  { id: 1, text: '提交"线性代数"课程达成数据', deadline: '2024/1/20', priority: '高' },
  { id: 2, text: '审核教师提交的达成报告', deadline: '2024/1/22', priority: '中' },
  { id: 3, text: '完善课程目标与指标点关联', deadline: '2024/1/25', priority: '中' },
]
</script>

<style scoped>
.welcome-header { margin-bottom: 20px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.view-all { font-size: 12px; color: #2563eb; text-decoration: none; cursor: pointer; }
.view-all:hover { text-decoration: underline; }
.sub-label { font-size: 12px; color: #94a3b8; margin-bottom: 14px; }
.sub-label-inline { font-size: 12px; color: #94a3b8; }
.course-list { display: flex; flex-direction: column; gap: 12px; }
.course-item { display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; background: #f8fafc; border-radius: 8px; border: 1px solid #e5e7eb; }
.course-item-left { display: flex; flex-direction: column; gap: 4px; }
.course-name { font-size: 14px; font-weight: 600; color: #1e293b; }
.course-meta { font-size: 12px; color: #94a3b8; }
.course-item-right { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.achieve-rate { font-size: 12px; font-weight: 600; }
.quick-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.quick-btn { display: flex; align-items: center; gap: 10px; padding: 14px 16px; background: #f8fafc; border: 1px solid #e5e7eb; border-radius: 8px; cursor: pointer; font-size: 14px; color: #374151; transition: all 0.15s; }
.quick-btn:hover { background: #eff6ff; border-color: #bfdbfe; color: #2563eb; }
.quick-btn .el-icon { font-size: 18px; color: #2563eb; }
.activity-list { display: flex; flex-direction: column; gap: 12px; }
.activity-item { display: flex; gap: 12px; align-items: flex-start; }
.activity-dot { width: 8px; height: 8px; border-radius: 50%; margin-top: 5px; flex-shrink: 0; }
.dot-upload { background: #3b82f6; }
.dot-check { background: #22c55e; }
.dot-submit { background: #8b5cf6; }
.dot-assign { background: #f59e0b; }
.activity-content { flex: 1; }
.activity-text { font-size: 13px; color: #374151; font-weight: 500; }
.activity-time { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.todo-list { display: flex; flex-direction: column; gap: 10px; }
.todo-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #f8fafc; border-radius: 8px; border: 1px solid #e5e7eb; }
.todo-text { font-size: 13px; color: #374151; font-weight: 500; margin-bottom: 2px; }
.todo-deadline { font-size: 12px; color: #94a3b8; }
</style>
