<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">达成情况查询</div>
        <div class="page-subtitle" v-if="isDirector">查询全系课程目标达成情况</div>
        <div class="page-subtitle" v-else>查询我的课程目标达成情况</div>
      </div>
      <el-button :icon="Download">导出报告</el-button>
    </div>


    <div class="section-card" style="margin-bottom:16px;padding:16px 24px">
      <div style="display:flex;gap:12px;flex-wrap:wrap;align-items:center">
        <el-input v-model="search" placeholder="搜索课程名称/教师..." :prefix-icon="Search" clearable style="width:220px" />
        <el-select v-model="filterCourse" placeholder="选择课程" clearable style="width:180px">
          <el-option v-for="c in courseStore.courses" :key="c.id" :label="c.name" :value="c.name" />
        </el-select>
        <el-select v-model="filterSemester" placeholder="选择学期" clearable style="width:200px">
          <el-option v-for="s in allSemesters" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="审核状态" clearable style="width:140px">
          <el-option label="已审核" value="已审核" />
          <el-option label="待审核" value="待审核" />
          <el-option label="已提交" value="已提交" />
          <el-option label="未提交" value="未提交" />
        </el-select>
        <el-button type="primary" :icon="Search">查询</el-button>
      </div>
    </div>


    <div class="stat-cards" style="margin-bottom:16px">
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">总课程数</div><div class="stat-value">{{ filtered.length }}</div></div>
        <div class="stat-card-icon icon-blue"><el-icon><Reading /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">优秀 (≥85%)</div><div class="stat-value">{{ filtered.filter(r=>r.overall!=null&&r.overall>=85).length }}</div></div>
        <div class="stat-card-icon icon-green"><el-icon><Star /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">良好 (75~85%)</div><div class="stat-value">{{ filtered.filter(r=>r.overall!=null&&r.overall>=75&&r.overall<85).length }}</div></div>
        <div class="stat-card-icon icon-blue"><el-icon><TrendCharts /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">待提交</div><div class="stat-value">{{ filtered.filter(r=>r.status==='未提交').length }}</div></div>
        <div class="stat-card-icon icon-orange"><el-icon><Warning /></el-icon></div>
      </div>
    </div>


    <div class="section-card">
      <div class="section-title" style="margin-bottom:16px">查询结果（共 {{ filtered.length }} 条）</div>
      <el-table :data="filtered" stripe style="width:100%">
        <el-table-column prop="courseName" label="课程名称" min-width="130" />
        <el-table-column prop="courseCode" label="课程编号" width="110" />
        <el-table-column prop="semester" label="学期" min-width="170" />
        <el-table-column prop="teacher" label="任课教师" width="90" v-if="isDirector" />
        <el-table-column prop="department" label="系部" width="90" v-if="isDirector" />
        <el-table-column label="目标一" width="90" align="center">
          <template #default="{row}">
            <span v-if="row.objective1!=null" :style="{color:getColor(row.objective1),fontWeight:600}">{{ row.objective1 }}%</span>
            <span v-else style="color:#cbd5e1">-</span>
          </template>
        </el-table-column>
        <el-table-column label="目标二" width="90" align="center">
          <template #default="{row}">
            <span v-if="row.objective2!=null" :style="{color:getColor(row.objective2),fontWeight:600}">{{ row.objective2 }}%</span>
            <span v-else style="color:#cbd5e1">-</span>
          </template>
        </el-table-column>
        <el-table-column label="目标三" width="90" align="center">
          <template #default="{row}">
            <span v-if="row.objective3!=null" :style="{color:getColor(row.objective3),fontWeight:600}">{{ row.objective3 }}%</span>
            <span v-else style="color:#cbd5e1">-</span>
          </template>
        </el-table-column>
        <el-table-column label="综合达成" width="100" align="center">
          <template #default="{row}">
            <span v-if="row.overall!=null">
              <el-tag :type="row.overall>=85?'success':row.overall>=75?'primary':row.overall>=60?'warning':'danger'" size="small">
                {{ row.overall }}%
              </el-tag>
            </span>
            <span v-else style="color:#cbd5e1">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status==='已审核'?'success':row.status==='已提交'?'warning':row.status==='待审核'?'info':'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:16px;display:flex;justify-content:flex-end">
        <el-pagination :page-size="10" :total="filtered.length" layout="total, prev, pager, next" background />
      </div>
    </div>


    <el-dialog v-model="showDetail" title="达成情况详情" width="560px">
      <div v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课程名称">{{ detail.courseName }}</el-descriptions-item>
          <el-descriptions-item label="课程编号">{{ detail.courseCode }}</el-descriptions-item>
          <el-descriptions-item label="学期">{{ detail.semester }}</el-descriptions-item>
          <el-descriptions-item label="任课教师">{{ detail.teacher }}</el-descriptions-item>
          <el-descriptions-item label="综合达成度">
            <el-tag :type="detail.overall>=85?'success':detail.overall>=75?'primary':'warning'">{{ detail.overall }}%</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:16px">
          <div v-for="(val, key) in { '目标一': detail.objective1, '目标二': detail.objective2, '目标三': detail.objective3 }" :key="key" style="margin-bottom:12px">
            <div style="display:flex;justify-content:space-between;margin-bottom:4px">
              <span style="font-size:13px;color:#374151;font-weight:500">{{ key }}</span>
              <span :style="{color:getColor(val),fontWeight:600}">{{ val ?? '-' }}%</span>
            </div>
            <el-progress v-if="val!=null" :percentage="val" :stroke-width="8"
              :color="val>=85?'#22c55e':val>=75?'#3b82f6':val>=60?'#f59e0b':'#ef4444'" />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, Download } from '@element-plus/icons-vue'
import { useCourseStore } from '@/stores/courses'
import { useUserStore } from '@/stores/user'

const courseStore = useCourseStore()
const userStore = useUserStore()
const isDirector = computed(() => userStore.role === 'director')

onMounted(async () => {
  await courseStore.fetchCourses()
})

const search = ref('')
const filterCourse = ref('')
const filterSemester = ref('')
const filterStatus = ref('')
const showDetail = ref(false)
const detail = ref(null)

const allSemesters = computed(() => {
  const s = new Set()
  courseStore.courses.forEach(c => { if (c.semester) s.add(c.semester) })
  courseStore.queryResults.forEach(r => { if (r.semester) s.add(r.semester) })
  return [...s].sort((a, b) => b.localeCompare(a))
})

const filtered = computed(() => courseStore.queryResults.filter(r => {
  if (search.value && !r.courseName.includes(search.value) && !r.teacher?.includes(search.value)) return false
  if (filterCourse.value && r.courseName !== filterCourse.value) return false
  if (filterSemester.value && r.semester !== filterSemester.value) return false
  if (filterStatus.value && r.status !== filterStatus.value) return false
  return true
}))

function getColor(v) {
  if (v == null) return '#94a3b8'
  return v >= 85 ? '#22c55e' : v >= 75 ? '#3b82f6' : v >= 60 ? '#f59e0b' : '#ef4444'
}
function viewDetail(row) { detail.value = row; showDetail.value = true }
</script>

<style scoped>
.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px; }
</style>
