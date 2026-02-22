<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">达成数据上传</div>
        <div class="page-subtitle">上传课程目标达成度数据，等待系主任审核</div>
      </div>
    </div>

    <!-- Stats -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">待审核</div><div class="stat-value">{{ myRecords.filter(r=>r.status==='待审核').length }}</div></div>
        <div class="stat-card-icon icon-orange"><el-icon><Clock /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">已通过</div><div class="stat-value">{{ myRecords.filter(r=>r.status==='已通过').length }}</div></div>
        <div class="stat-card-icon icon-green"><el-icon><CircleCheck /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">已驳回</div><div class="stat-value">{{ myRecords.filter(r=>r.status==='已驳回').length }}</div></div>
        <div class="stat-card-icon icon-red"><el-icon><CircleClose /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">总上传数</div><div class="stat-value">{{ myRecords.length }}</div></div>
        <div class="stat-card-icon icon-blue"><el-icon><UploadFilled /></el-icon></div>
      </div>
    </div>

    <!-- Upload area -->
    <div class="section-card" style="margin-bottom:16px">
      <div class="section-title" style="margin-bottom:20px">上传新数据</div>

      <el-form :model="uploadForm" label-position="top">
        <!-- Row 1: course + semester -->
        <el-row :gutter="20">
          <el-col :span="10">
            <el-form-item label="选择课程 *">
              <el-select v-model="uploadForm.courseId" style="width:100%" placeholder="请选择课程" @change="onCourseChange">
                <el-option v-for="c in courseStore.courses" :key="c.id" :label="`${c.name}（${c.code}）`" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学期 *">
              <el-select v-model="uploadForm.semester" style="width:100%" placeholder="请选择学期">
                <el-option v-for="s in semesters" :key="s" :label="s" :value="s" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- Row 2: achievement rates (dynamic based on selected course objectives) -->
        <el-row :gutter="20" v-if="objectives.length">
          <el-col :span="6" v-for="(obj, i) in objectives" :key="i">
            <el-form-item :label="`${obj.tag}达成度`">
              <el-input-number
                v-model="uploadForm.rates[i]"
                :min="0" :max="100" :precision="1"
                style="width:100%"
                :placeholder="obj.tag"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <div v-else style="font-size:13px;color:#94a3b8;margin-bottom:16px">
          请先选择课程，将自动加载该课程的目标列表
        </div>

        <!-- Excel upload area -->
        <el-form-item label="上传Excel文件（可选，支持 .xlsx / .xls）">
          <el-upload
            ref="uploadRef"
            v-model:file-list="fileList"
            :auto-upload="false"
            accept=".xlsx,.xls"
            :limit="1"
            drag
            style="width:100%"
            :on-exceed="() => ElMessage.warning('只能上传一个文件')"
          >
            <el-icon style="font-size:40px;color:#94a3b8"><UploadFilled /></el-icon>
            <div style="color:#374151;font-size:14px;margin-top:8px;font-weight:500">
              点击或拖拽 Excel 文件到此处上传
            </div>
            <div style="color:#94a3b8;font-size:12px;margin-top:4px">
              支持 .xlsx / .xls 格式，文件大小不超过 10MB
            </div>
          </el-upload>
        </el-form-item>

        <div style="display:flex;gap:10px;margin-top:8px">
          <el-button type="primary" :icon="UploadFilled" @click="doSubmit">提交数据</el-button>
          <el-button :icon="Download">下载填报模板</el-button>
        </div>
      </el-form>
    </div>

    <!-- Records table -->
    <div class="section-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <div class="section-title">我的上传记录</div>
        <el-select v-model="filterStatus" placeholder="筛选状态" clearable style="width:140px">
          <el-option label="待审核" value="待审核" />
          <el-option label="已通过" value="已通过" />
          <el-option label="已驳回" value="已驳回" />
        </el-select>
      </div>
      <el-table :data="filtered" stripe style="width:100%" table-layout="auto">
        <el-table-column prop="courseName" label="课程名称" min-width="130" />
        <el-table-column prop="semester" label="学期" min-width="160" />
        <el-table-column prop="uploadTime" label="上传时间" min-width="110" />
        <el-table-column label="目标一" min-width="90" align="center">
          <template #default="{row}">
            <span v-if="row.objective1!=null" :style="{color:getColor(row.objective1),fontWeight:600}">{{ row.objective1 }}%</span>
            <span v-else style="color:#94a3b8">-</span>
          </template>
        </el-table-column>
        <el-table-column label="目标二" min-width="90" align="center">
          <template #default="{row}">
            <span v-if="row.objective2!=null" :style="{color:getColor(row.objective2),fontWeight:600}">{{ row.objective2 }}%</span>
            <span v-else style="color:#94a3b8">-</span>
          </template>
        </el-table-column>
        <el-table-column label="目标三" min-width="90" align="center">
          <template #default="{row}">
            <span v-if="row.objective3!=null" :style="{color:getColor(row.objective3),fontWeight:600}">{{ row.objective3 }}%</span>
            <span v-else style="color:#94a3b8">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{row}">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核意见" min-width="200">
          <template #default="{row}">
            <div v-if="row.auditComment">
              <div class="audit-comment" :class="row.status==='已驳回'?'comment-red':'comment-green'">
                <span style="font-size:11px;font-weight:700;margin-right:4px">{{ row.status==='已驳回'?'驳回原因：':'审核意见：' }}</span>
                <span style="font-size:12px">{{ row.auditComment }}</span>
              </div>
            </div>
            <span v-else style="color:#94a3b8;font-size:12px">无意见</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status==='已驳回'" link type="primary" size="small" @click="reUpload(row)">重新上传</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Download, UploadFilled, Clock, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { useCourseStore } from '@/stores/courses'
import { useUserStore } from '@/stores/user'
import { uploadExcel } from '@/api/upload'
import { ElMessage } from 'element-plus'

const courseStore = useCourseStore()
const store = useCourseStore()
const userStore = useUserStore()
onMounted(async () => {
  await store.fetchCourses()
  const teacherId = userStore.role === 'teacher' ? userStore.userId : null
  await store.fetchUploadRecords(teacherId)
})
const filterStatus = ref('')
const fileList = ref([])
const uploadRef = ref()

const semesters = computed(() => {
  const s = new Set()
  // 提取所有课程定义过的学期，或者从 courseTasks 提取
  store.courses.forEach(c => {
    if (c.semester) s.add(c.semester)
  })
  store.courseTasks.forEach(t => {
    if (t.semester) s.add(t.semester)
  })
  return [...s].sort((a, b) => b.localeCompare(a))
})

const uploadForm = reactive({ courseId: null, semester: '', rates: [null, null, null] })

// 切换课程后加载目标
watch(() => uploadForm.courseId, id => { if (id) store.fetchObjectives(id) })

const objectives = computed(() => {
  if (!uploadForm.courseId) return []
  return store.objectives.filter(o => o.courseId === uploadForm.courseId).slice(0, 4)
})

function onCourseChange(id) {
  const c = store.courses.find(c => c.id === id)
  if (c) uploadForm.semester = c.semester || ''
  uploadForm.rates = [null, null, null]
}

// Only show this teacher's own records
const myRecords = computed(() => {
  if (userStore.role === 'director') return store.uploadRecords
  return store.uploadRecords.filter(r => !r.teacher || r.teacher === userStore.displayName)
})

const filtered = computed(() => {
  const base = myRecords.value
  if (!filterStatus.value) return base
  return base.filter(r => r.status === filterStatus.value)
})

function statusType(s) {
  return s === '已通过' ? 'success' : s === '已驳回' ? 'danger' : 'warning'
}
function getColor(v) { return v >= 85 ? '#22c55e' : v >= 75 ? '#3b82f6' : v >= 60 ? '#f59e0b' : '#ef4444' }

async function doSubmit() {
  if (!uploadForm.courseId) { ElMessage.warning('请选择课程'); return }
  if (!uploadForm.semester) { ElMessage.warning('请选择学期'); return }
  const hasFile = fileList.value.length > 0
  if (!hasFile) { ElMessage.warning('请上传 Excel 文件'); return }

  const fd = new FormData()
  fd.append('file', fileList.value[0].raw)
  fd.append('courseId', uploadForm.courseId)
  fd.append('teacherId', userStore.userId)
  fd.append('semester', uploadForm.semester)

  try {
    await uploadExcel(fd)
    ElMessage.success('提交成功，等待系主任审核')
    uploadForm.courseId = null; uploadForm.semester = ''; uploadForm.rates = [null, null, null]
    fileList.value = []
    const teacherId = userStore.role === 'teacher' ? userStore.userId : null
    await store.fetchUploadRecords(teacherId)
  } catch (_) {}
}

function reUpload(row) {
  uploadForm.courseId = store.courses.find(c => c.name === row.courseName)?.id || null
  uploadForm.semester = row.semester
  window.scrollTo({ top: 0, behavior: 'smooth' })
  ElMessage.info('请修改后重新提交')
}
</script>

<style scoped>
.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px; }
.icon-red { background:#fef2f2; color:#ef4444; }
.audit-comment {
  display: inline-flex;
  align-items: flex-start;
  gap: 2px;
  padding: 4px 8px;
  border-radius: 6px;
  line-height: 1.5;
}
.comment-green { background: #f0fdf4; color: #15803d; border: 1px solid #bbf7d0; }
.comment-red   { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
</style>
