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
        <el-table-column label="课程编号" width="110">
          <template #default="{row}">{{ getCourseAttr(row.courseId, 'code') }}</template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" min-width="170" />
        <el-table-column label="任课教师" width="90" v-if="isDirector">
          <template #default="{row}">{{ row.teacherName }}</template>
        </el-table-column>
        <el-table-column label="系部" width="90" v-if="isDirector">
          <template #default="{row}">{{ getTeacherAttr(row.teacherId, 'dept') || getCourseAttr(row.courseId, 'dept') }}</template>
        </el-table-column>
        <el-table-column label="达成度目标列表" min-width="180">
          <template #default="{row}">
            <div v-if="row.details && row.details.length > 0" style="display:flex;gap:6px;flex-wrap:wrap">
              <span v-for="(d, i) in row.details" :key="i" class="obj-chip">
                目标{{ i + 1 }}: <b :style="{color:getColor(d.achievementRate)}">{{ d.achievementRate }}%</b>
              </span>
            </div>
            <span v-else style="color:#cbd5e1;font-size:12px">暂无数据</span>
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
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tooltip :content="row.auditComment || '无反馈信息'" placement="top" v-if="row.status==='已驳回'">
              <el-tag type="danger" size="small" style="cursor:help">
                {{ row.status }}
              </el-tag>
            </el-tooltip>
            <el-tag v-else :type="row.status==='已审核'?'success':row.status==='已提交'?'warning':row.status==='待审核'?'info':'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="warning" size="small" v-if="!isDirector && row.status === '已驳回'" @click="routeToEdit(row)">修改</el-button>
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
          <el-descriptions-item label="课程编号">{{ getCourseAttr(detail.courseId, 'code') }}</el-descriptions-item>
          <el-descriptions-item label="学期">{{ detail.semester }}</el-descriptions-item>
          <el-descriptions-item label="任课教师">{{ detail.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="综合达成度">
            <el-tag :type="detail.overall>=85?'success':detail.overall>=75?'primary':'warning'">{{ detail.overall }}%</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detail.status==='已驳回'?'danger':detail.status==='已审核'?'success':detail.status==='待审核'?'info':'warning'" size="small">{{ detail.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="驳回原因" v-if="detail.status === '已驳回'">
            <span style="color:#ef4444;font-weight:bold">{{ detail.auditComment || '未填写具体原因' }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:16px">
          <div v-if="detail.details && detail.details.length > 0">
            <div v-for="(d, i) in detail.details" :key="i" style="margin-bottom:12px">
              <div style="display:flex;justify-content:space-between;margin-bottom:4px">
                <span style="font-size:13px;color:#374151;font-weight:500">目标{{ i + 1 }}</span>
                <span :style="{color:getColor(d.achievementRate),fontWeight:600}">{{ d.achievementRate }}%</span>
              </div>
              <el-progress :percentage="d.achievementRate" :stroke-width="8"
                :color="getColor(d.achievementRate)" />
            </div>
          </div>
          <div v-else style="color:#94a3b8;font-size:12px">暂无目标列表数据</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Download, Reading, Star, TrendCharts, Warning } from '@element-plus/icons-vue'
import { useCourseStore, useTeacherStore } from '@/stores/courses'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const courseStore = useCourseStore()
const teacherStore = useTeacherStore()
const userStore = useUserStore()
const isDirector = computed(() => userStore.role === 'director')

onMounted(async () => {
  await courseStore.fetchCourses()
  await teacherStore.fetchTeachers()
  let teacherId = null
  if (!isDirector.value) {
    const matched = teacherStore.teachers.find(t => t.userId === userStore.userId)
    if (matched) teacherId = matched.id
  }
  await courseStore.fetchUploadRecords(teacherId)
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
  courseStore.uploadRecords.forEach(r => { if (r.semester) s.add(r.semester) })
  return [...s].sort((a, b) => b.localeCompare(a))
})

const filtered = computed(() => courseStore.uploadRecords.filter(r => {
  if (search.value && !r.courseName?.includes(search.value) && !r.teacherName?.includes(search.value)) return false
  if (filterCourse.value && r.courseName !== filterCourse.value) return false
  if (filterSemester.value && r.semester !== filterSemester.value) return false
  if (filterStatus.value && r.status !== filterStatus.value) return false
  // 综合达成计算：如果有 details 就取平均值
  if (!r.overall && r.details && r.details.length) {
    const sum = r.details.reduce((acc, d) => acc + (d.achievementRate || 0), 0)
    r.overall = Number((sum / r.details.length).toFixed(1))
  }
  return true
}))

function getColor(v) {
  if (v == null) return '#94a3b8'
  return v >= 85 ? '#22c55e' : v >= 75 ? '#3b82f6' : v >= 60 ? '#f59e0b' : '#ef4444'
}
function viewDetail(row) { detail.value = row; showDetail.value = true }
function getCourseAttr(id, attr) {
  const c = courseStore.courses.find(c => c.id === id)
  return c ? c[attr] : '-'
}
function getTeacherAttr(id, attr) {
  const t = teacherStore.teachers.find(t => t.userId === id || t.id === id)
  return t ? t[attr] : ''
}
function routeToEdit(row) {
  // 跳转到数据上传视图并传递记录ID供用户修改
  router.push({ path: '/teacher/data-upload', query: { editId: row.id } })
}
</script>

<style scoped>
.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px; }
.obj-chip { font-size:12px; background:#eff6ff; color:#475569; padding:2px 8px; border-radius:4px; font-weight:500; }
</style>
