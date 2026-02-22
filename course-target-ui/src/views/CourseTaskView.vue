<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">课程任务查看</div>
        <div class="page-subtitle">查看所有教学任务状态</div>
      </div>
    </div>

    <!-- Filters -->
    <div class="section-card" style="margin-bottom:16px;padding:16px 24px">
      <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
        <el-select v-model="filterSemester" placeholder="选择学期" clearable style="width:200px">
          <el-option label="全部学期" value="" />
          <el-option v-for="s in allSemesters" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input v-model="search" placeholder="搜索课程名称..." :prefix-icon="Search" clearable style="width:220px" />
        <el-button type="primary" :icon="Search">查询</el-button>
      </div>
    </div>

    <!-- Table -->
    <div class="section-card">
      <div class="section-title" style="margin-bottom:16px">课程任务列表（共 {{ filtered.length }} 条）</div>
      <el-table :data="filtered" stripe style="width:100%" table-layout="auto">
        <el-table-column prop="courseName" label="课程名称" min-width="130" />
        <el-table-column prop="semester" label="学期" min-width="160" />
        <el-table-column prop="teacher" label="任课教师" min-width="90" />
        <el-table-column prop="teacherDept" label="所属系部" min-width="100" />
        <el-table-column label="学分/学时" min-width="100">
          <template #default="{row}">{{ row.credits }}学分 / {{ row.hours }}学时</template>
        </el-table-column>
        <el-table-column prop="studentCount" label="学生人数" min-width="80" align="center" />
        <el-table-column label="达成度" min-width="80" align="center">
          <template #default="{row}">
            <span v-if="row.achievementRate !== null" :style="{ color: getColor(row.achievementRate), fontWeight: 600 }">{{ row.achievementRate }}%</span>
            <span v-else style="color:#94a3b8">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80">
          <template #default="{row}">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:16px;display:flex;justify-content:flex-end">
        <el-pagination :page-size="10" :total="filtered.length" layout="total, prev, pager, next" background />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useCourseStore } from '@/stores/courses'
import { useUserStore } from '@/stores/user'

const store = useCourseStore()
const userStore = useUserStore()
onMounted(() => {
  const teacherId = userStore.role === 'teacher' ? userStore.userId : null
  store.fetchCourseTasks(teacherId)
})
const search = ref('')
const filterSemester = ref('')

const allSemesters = computed(() => {
  const s = new Set()
  store.courseTasks.forEach(t => {
    if (t.semester) s.add(t.semester)
  })
  return [...s].sort((a, b) => b.localeCompare(a))
})

// Teacher only sees their own assigned tasks
const myTasks = computed(() => {
  if (userStore.role === 'director') return store.courseTasks
  const name = userStore.displayName
  return store.courseTasks.filter(t =>
    t.teacher?.includes(name) ||
    t.assignments?.some(a => a.teacher === name)
  )
})

const filtered = computed(() => myTasks.value.filter(t => {
  if (filterSemester.value && t.semester !== filterSemester.value) return false
  if (search.value && !t.courseName.includes(search.value) && !t.teacher?.includes(search.value)) return false
  return true
}))

function getColor(v) { return v >= 85 ? '#22c55e' : v >= 75 ? '#3b82f6' : v >= 60 ? '#f59e0b' : '#ef4444' }
function statusType(s) { return s === '已审核' ? 'success' : s === '已提交' ? 'warning' : 'info' }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
</style>
