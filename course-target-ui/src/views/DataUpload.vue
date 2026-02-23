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
                <el-option v-for="c in availableCourses" :key="c.id" :label="`${c.name}（${c.code}）`" :value="c.id" />
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
        <el-row :gutter="20" v-if="objectives.length && uploadForm.rates.length === objectives.length">
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
        <el-table-column label="达成度目标列表" min-width="180">
          <template #default="{row}">
            <div v-if="row.details && row.details.length > 0" style="display:flex;gap:6px;flex-wrap:wrap">
              <span v-for="(d, i) in row.details" :key="i" class="obj-chip">
                目标{{ i + 1 }}: <b :style="{color:getColor(d.achievementRate)}">{{ d.achievementRate }}%</b>
              </span>
            </div>
            <span v-else style="color:#94a3b8;font-size:12px">暂无数据</span>
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
            <el-button v-if="row.status==='已驳回'" link type="primary" size="small" @click="reUpload(row)">修改数据</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Edit Dialog -->
    <el-dialog v-model="editDialogVisible" title="修改驳回数据" width="600px" destroy-on-close>
      <div style="margin-bottom:16px;font-size:13px;color:#64748b;line-height:1.6">
        <div style="font-weight:600;font-size:14px;color:#0f172a">{{ editRow?.courseName }}</div>
        <div>学期：{{ editRow?.semester }}&nbsp;&nbsp;&nbsp;上传时间：{{ editRow?.uploadTime }}</div>
        <div style="margin-top:8px;padding:8px 12px;background:#fef2f2;border-radius:6px;color:#b91c1c;border:1px solid #fecaca">
          <b>驳回原因：</b>{{ editRow?.auditComment || '无' }}
        </div>
      </div>
      
      <el-form label-position="top">
        <el-row :gutter="20">
          <el-col :span="8" v-for="(obj, i) in editObjectives" :key="i">
            <el-form-item :label="`${obj.tag}达成度 (%)`">
              <el-input-number
                v-model="editRates[i]"
                :min="0" :max="100" :precision="1"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div style="font-size:12px;color:#94a3b8;margin-top:8px">修改后将重新提交给系主任审核</div>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消修改</el-button>
        <el-button type="primary" :icon="UploadFilled" @click="doSaveEdit">确认重新提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Download, UploadFilled, Clock, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { useCourseStore, useTeacherStore } from '@/stores/courses'
import { useUserStore } from '@/stores/user'
import { uploadExcel, addUploadRecord, updateUploadRecord } from '@/api/upload'
import { ElMessage } from 'element-plus'

const route = useRoute()
const courseStore = useCourseStore()
const store = useCourseStore()
const teacherStore = useTeacherStore()
const userStore = useUserStore()

const currentTeacherId = computed(() => {
  if (userStore.role !== 'teacher') return null
  const t = teacherStore.teachers.find(x => x.userId === userStore.userId)
  return t ? t.id : null
})

onMounted(async () => {
  await store.fetchCourses()
  await store.fetchCourseTasks()
  await teacherStore.fetchTeachers()
  await store.fetchUploadRecords(currentTeacherId.value)

  if (route.query.editId) {
    const editId = parseInt(route.query.editId)
    const target = store.uploadRecords.find(r => r.id === editId)
    if (target && target.status === '已驳回') {
      reUpload(target)
    }
  }
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

const uploadForm = reactive({ courseId: null, semester: '', rates: [] })

const availableCourses = computed(() => {
  const submittedIds = new Set(myRecords.value.map(r => r.courseId))
  return store.courses.filter(c => !submittedIds.has(c.id))
})

// 编辑弹窗相关状态
const editDialogVisible = ref(false)
const editRecordId = ref(null) 
const editRow = ref(null)
const editObjectives = ref([])
const editRates = ref([])

// 移除了依赖外部 watch 触发的手法，在 select change 里明确顺序执行
const objectives = computed(() => {
  if (!uploadForm.courseId) return []
  return store.objectives.filter(o => o.courseId === uploadForm.courseId)
})

async function onCourseChange(id) {
  const c = store.courses.find(c => c.id === id)
  if (c) {
    uploadForm.semester = c.semester || ''
  } else {
    uploadForm.semester = ''
  }
  // 强制同步等待数据到达
  await store.fetchObjectives(id)
  
  // 这时候 store.objectives 里面应该已经有了这门课的目标
  const currentObjs = store.objectives.filter(o => o.courseId === id)
  const newArray = Array(currentObjs.length).fill(null)
  uploadForm.rates.splice(0, uploadForm.rates.length, ...newArray)
}

// 仅展示老师自己的上传记录
const myRecords = computed(() => {
  return store.uploadRecords
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
  
  if (myRecords.value.some(r => r.courseId === uploadForm.courseId)) {
    ElMessage.error('该门课程您已提交过，供用户选择的课程列表中已将其隐藏，若需修改请从下方列表操作')
    return
  }
  
  const hasFile = fileList.value.length > 0
  const hasRates = uploadForm.rates.some(r => r != null)

  if (!hasFile && !hasRates) { ElMessage.warning('请填写达成度或上传 Excel 文件'); return }

  try {
    const details = []
    objectives.value.forEach((obj, i) => {
      if (uploadForm.rates[i] != null) {
        details.push({
          objectiveId: obj.id,
          achievementRate: uploadForm.rates[i]
        })
      }
    })

    if (hasFile) {
      const fd = new FormData()
      fd.append('file', fileList.value[0].raw)
      fd.append('courseId', uploadForm.courseId)
      fd.append('teacherId', currentTeacherId.value)
      fd.append('semester', uploadForm.semester)
      if (details.length > 0) {
        fd.append('details', JSON.stringify(details))
      }
      await uploadExcel(fd)
    } else {
      const payload = {
        courseId: uploadForm.courseId,
        teacherId: currentTeacherId.value,
        semester: uploadForm.semester,
        details: details
      }
      await addUploadRecord(payload)
    }

    ElMessage.success('提交成功，等待系主任审核')
    uploadForm.courseId = null; uploadForm.semester = ''; uploadForm.rates = []
    fileList.value = []
    await store.fetchUploadRecords(currentTeacherId.value)
  } catch (_) {}
}

async function reUpload(row) {
  editRecordId.value = row.id
  editRow.value = row
  
  // 等待目标获取完毕后回显达成情况
  await store.fetchObjectives(row.courseId)
  editObjectives.value = store.objectives.filter(o => o.courseId === row.courseId)
  
  // 初始化一个带有所有空的rates数组
  const rates = Array(editObjectives.value.length).fill(null)
  if (row.details) {
    row.details.forEach(d => {
      const index = editObjectives.value.findIndex(o => o.id === d.objectiveId)
      if (index !== -1) rates[index] = d.achievementRate
    })
  }
  editRates.value = rates

  editDialogVisible.value = true
}

async function doSaveEdit() {
  const details = []
  editObjectives.value.forEach((obj, i) => {
    if (editRates.value[i] != null) {
      details.push({ objectiveId: obj.id, achievementRate: editRates.value[i] })
    }
  })
  
  if (details.length === 0) {
    ElMessage.warning('请至少填写一项达成度指标才可提交')
    return
  }

  const payload = {
    courseId: editRow.value.courseId,
    teacherId: editRow.value.teacherId,
    semester: editRow.value.semester,
    status: '待审核',
    details: details
  }

  try {
    await updateUploadRecord(editRecordId.value, payload)
    ElMessage.success('修改成功，已再次提交审核！')
    editDialogVisible.value = false
    
    // 强制系统重新抓取最新的记录状态
    const teacherId = userStore.role === 'teacher' ? userStore.userId : null
    await store.fetchUploadRecords(teacherId)
  } catch (e) {
    ElMessage.error('重新提交失败')
  }
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
.obj-chip { font-size:12px; background:#eff6ff; color:#475569; padding:2px 8px; border-radius:4px; font-weight:500; }
</style>
