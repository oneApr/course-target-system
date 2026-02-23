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
        <el-table-column label="课程名称" min-width="130">
          <template #default="{row}">{{ getCourseAttr(row.courseId, 'name') }}</template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" min-width="160" />
        <el-table-column label="任课教师" min-width="90">
          <template #default="{row}">
            {{ row.assignments?.map(a => a.teacherName || getTeacherName(a.teacherId)).join('、') || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="所属系部" min-width="100">
          <template #default="{row}">{{ getCourseAttr(row.courseId, 'dept') }}</template>
        </el-table-column>
        <el-table-column label="学分/学时" min-width="100">
          <template #default="{row}">{{ getCourseAttr(row.courseId, 'credits') }}学分 / {{ getCourseAttr(row.courseId, 'hours') }}学时</template>
        </el-table-column>
        <el-table-column label="学生人数" min-width="80" align="center">
          <template #default="{row}">
            {{ row.assignments?.reduce((sum, a) => sum + (a.studentCount || 0), 0) || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="达成度" min-width="80" align="center">
          <template #default="{row}">
            <span v-if="getTaskAchievementRate(row) !== null" :style="{ color: getColor(getTaskAchievementRate(row)), fontWeight: 600 }">{{ getTaskAchievementRate(row) }}%</span>
            <span v-else style="color:#94a3b8">-</span>
          </template>
        </el-table-column>
        <el-table-column label="任务状态" min-width="90">
          <template #default="{row}">
            <el-tag :type="row.status === '已分配' ? 'success' : 'info'" size="small">
              {{ row.status || '未分配' }}
            </el-tag>
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
import { useCourseStore, useTeacherStore } from '@/stores/courses'
import { useUserStore } from '@/stores/user'
import { getUploadList } from '@/api/upload'

const store = useCourseStore()
const userStore = useUserStore()
const teacherStore = useTeacherStore()

const allUploadRecords = ref([])

onMounted(async () => {
  await store.fetchCourses()
  await teacherStore.fetchTeachers()
  await store.fetchCourseTasks(null) // 获取所有任务
  
  try {
    const res = await getUploadList()
    if (res.code === 200) {
      allUploadRecords.value = res.data || []
    }
  } catch (e) {}
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

// 教师只能看到分配给自己的任务
const myTasks = computed(() => {
  if (userStore.role === 'director') return store.courseTasks
  
  // 通过老师的 userId 找到真实的 teacher 
  const teacher = teacherStore.teachers.find(t => t.userId === userStore.userId)
  if (!teacher) return []
  
  return store.courseTasks.filter(t =>
    t.assignments?.some(a => a.teacherId === teacher.id)
  )
})

const filtered = computed(() => myTasks.value.filter(t => {
  if (filterSemester.value && t.semester !== filterSemester.value) return false
  const cName = getCourseAttr(t.courseId, 'name') || t.courseName || ''
  if (search.value && !cName.includes(search.value) && !t.assignments?.some(a => (a.teacherName || getTeacherName(a.teacherId))?.includes(search.value))) return false
  return true
}))

function getCourseAttr(courseId, attr) {
  const c = store.courses.find(c => c.id === courseId)
  return c ? c[attr] : '-'
}

function getTeacherName(id) {
  const t = teacherStore.teachers.find(x => x.id === id)
  return t ? t.name : '未知老师'
}

const achMap = computed(() => {
  const map = {}
  allUploadRecords.value.forEach(r => {
    if (!r.courseId || !r.semester) return
    const key = String(r.courseId) + '-' + String(r.semester).trim()
    if (!map[key]) map[key] = { total: 0, count: 0 }
    if (r.details && r.details.length > 0) {
      r.details.forEach(d => {
        const val = Number(d.achievementRate)
        if (!isNaN(val)) {
          map[key].total += val
          map[key].count++
        }
      })
    }
  })
  
  const result = {}
  for (const k in map) {
    if (map[k].count > 0) {
      result[k] = (map[k].total / map[k].count).toFixed(1)
    }
  }
  return result
})

function getTaskAchievementRate(row) {
  if (!row.courseId || !row.semester) return null
  const key = String(row.courseId) + '-' + String(row.semester).trim()
  return achMap.value[key] || null
}

function getColor(v) { return v >= 85 ? '#22c55e' : v >= 75 ? '#3b82f6' : v >= 60 ? '#f59e0b' : '#ef4444' }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
</style>
