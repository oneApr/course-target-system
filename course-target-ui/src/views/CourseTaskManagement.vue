<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">课程任务管理</div>
        <div class="page-subtitle">管理课程任务信息和教师任务分配</div>
      </div>
      <div style="display:flex;gap:8px">
        <el-button type="primary" :icon="Plus" @click="openAdd">+ 添加课程</el-button>
      </div>
    </div>

    <!-- Stats -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">课程总数</div><div class="stat-value">{{ store.courseTasks.length }}</div></div>
        <div class="stat-card-icon icon-blue"><el-icon><Tickets /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">已分配</div><div class="stat-value">{{ store.courseTasks.filter(t=>t.assignments?.length).length }}</div></div>
        <div class="stat-card-icon icon-green"><el-icon><CircleCheck /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">未分配</div><div class="stat-value">{{ store.courseTasks.filter(t=>!t.assignments?.length).length }}</div></div>
        <div class="stat-card-icon icon-orange"><el-icon><Warning /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">任课教师</div><div class="stat-value">{{ uniqueTeachers }}</div></div>
        <div class="stat-card-icon icon-purple"><el-icon><UserFilled /></el-icon></div>
      </div>
    </div>

    <!-- Search -->
    <div class="section-card" style="padding:14px 20px;margin-bottom:16px">
      <el-input v-model="search" placeholder="搜索课程名称或负责人..." :prefix-icon="Search" clearable style="width:300px" />
    </div>

    <!-- Table -->
    <div class="section-card">
      <div class="section-title" style="margin-bottom:16px">课程任务列表</div>
      <el-table :data="filtered" stripe style="width:100%" table-layout="auto">
        <el-table-column label="课程名称" min-width="120">
          <template #default="{row}">{{ getCourseAttr(row.courseId, 'name') }}</template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" min-width="160" />
        <el-table-column label="学分/学时" min-width="110">
          <template #default="{row}">{{ getCourseAttr(row.courseId, 'credits') }}学分 / {{ getCourseAttr(row.courseId, 'hours') }}学时</template>
        </el-table-column>
        <el-table-column label="任课教师" min-width="200">
          <template #default="{row}">
            <div v-if="row.assignments?.length" style="display:flex;flex-direction:column;gap:3px">
              <div v-for="a in row.assignments" :key="a.id || a.teacherId" style="font-size:12px;color:#374151">
                <b>{{ a.teacherName || getTeacherName(a.teacherId) }}</b>
                <span style="color:#64748b;margin-left:4px">{{ a.classes ? a.classes.split(',').join(' ') : '' }}（{{ a.studentCount }}人）</span>
              </div>
            </div>
            <span v-else style="color:#94a3b8;font-size:12px">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="分配状态" min-width="90">
          <template #default="{row}">
            <el-tag :type="row.status === '已分配' ? 'success' : 'info'" size="small">
              {{ row.status || '未分配' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" :icon="Edit" @click="openEdit(row)" title="编辑" />
            <el-button link type="success" size="small" :icon="UserFilled" @click="openAssign(row)" title="分配教师" />
            <el-button link type="danger" size="small" :icon="Delete" @click="doDelete(row)" title="删除" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Add/Edit course dialog -->
    <el-dialog v-model="showCourseDialog" :title="isEdit?'编辑课程':'添加课程'" width="500px" destroy-on-close>
      <el-form ref="courseFormRef" :model="courseForm" :rules="courseRules" label-width="90px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="courseForm.courseId" style="width:100%" @change="onCourseChange" filterable allow-create default-first-option placeholder="选择现有课程或输入：课程名（编号）">
            <el-option v-for="c in store.courses" :key="c.id" :label="`${c.name}（${c.code}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester"><el-input v-model="courseForm.semester" placeholder="请输入学期，例如：2023-2024第一学期" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="学分" prop="credits"><el-input-number v-model="courseForm.credits" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="学时" prop="hours"><el-input-number v-model="courseForm.hours" :min="0" style="width:100%" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showCourseDialog=false">取消</el-button>
        <el-button type="primary" @click="saveCourse">{{ isEdit?'保存':'添加' }}</el-button>
      </template>
    </el-dialog>

    <!-- Assign teacher dialog -->
    <el-dialog v-model="showAssignDialog" title="分配任课教师" width="520px" destroy-on-close>
      <div style="font-size:13px;color:#64748b;margin-bottom:16px">
        为"{{ getCourseAttr(assignTarget?.courseId, 'name') }}"分配任课教师和教学班级
      </div>

      <!-- Course info banner -->
      <div v-if="assignTarget" class="course-info-banner">
        <div style="font-size:13px;font-weight:600;color:#2563eb">
          课程信息：{{ getCourseAttr(assignTarget.courseId, 'name') }} | {{ assignTarget.semester }}
        </div>
        <div v-if="assignTarget.assignments?.length" style="font-size:12px;color:#64748b;margin-top:4px">
          已分配班级：{{ assignedClasses(assignTarget).join('、') }}
        </div>
      </div>

      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-position="top" style="margin-top:16px">
        <el-form-item label="任课教师 *" prop="teacherId">
          <el-select v-model="assignForm.teacherId" style="width:100%" placeholder="请选择教师">
            <el-option v-for="t in activeTeachers" :key="t.id" :label="`${t.name}（${t.dept}）`" :value="t.id" :disabled="isTeacherTaken(t.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业年级" prop="grade">
          <el-input v-model="assignForm.grade" style="width:100%" placeholder="例如：2023级" />
        </el-form-item>
        <el-form-item prop="classes">
          <template #label>
            <span style="font-weight:600">教学班级 *</span>
            <span style="font-size:12px;color:#94a3b8;margin-left:6px">可输入新班级（按回车确认），已分配的班级不可选</span>
          </template>
          <el-select v-model="assignForm.classes" multiple filterable allow-create default-first-option placeholder="如果没有班级，请自行输入 如：01班" style="width:100%">
            <el-option v-for="cls in allClasses" :key="cls" :label="cls" :value="cls" :disabled="isClassTaken(cls)" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生人数">
          <el-input v-model.number="assignForm.studentCount" type="number" placeholder="如: 55" style="width:150px" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAssignDialog=false">取消</el-button>
        <el-button type="primary" @click="confirmAssign">分配教师</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Edit, Delete, Download, Upload, Search, UserFilled } from '@element-plus/icons-vue'
import { useCourseStore, useTeacherStore } from '@/stores/courses'
import { ElMessage, ElMessageBox } from 'element-plus'

const store = useCourseStore()
const teacherStore = useTeacherStore()
onMounted(async () => {
  await store.fetchCourses()
  await store.fetchCourseTasks()
  await teacherStore.fetchTeachers()
  await store.fetchUploadRecords()
})
const search = ref('')

// 状态统计
const uniqueTeachers = computed(() => {
  const names = new Set()
  store.courseTasks.forEach(t => t.assignments?.forEach(a => names.add(a.teacherName || getTeacherName(a.teacherId))))
  return names.size
})

const activeTeachers = computed(() => teacherStore.teachers.filter(t => t.status === '1' || t.status === '在职'))

const filtered = computed(() => {
  const q = search.value.toLowerCase()
  if (!q) return store.courseTasks
  return store.courseTasks.filter(t => {
    const courseName = getCourseAttr(t.courseId, 'name') || t.courseName || ''
    return courseName.toLowerCase().includes(q) ||
           t.assignments?.some(a => (a.teacherName || getTeacherName(a.teacherId))?.includes(q))
  })
})

function getTeacherName(id) {
  const t = teacherStore.teachers.find(x => x.id === id)
  return t ? t.name : '未知老师'
}

function getCourseAttr(courseId, attr) {
  const c = store.courses.find(x => x.id === courseId)
  return c ? c[attr] : '-'
}



// 添加/编辑课程
const showCourseDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const courseFormRef = ref(null)
const courseForm = reactive({ courseId: null, courseName: '', semester: '', credits: 3, hours: 48 })

const validateCourseInput = (rule, value, callback) => {
  if (!value) { return callback(new Error('请选择或输入课程')); }
  if (typeof value === 'string') {
    // Check if the user's manual input matches Name(Code) format
    const match = value.match(/(.+?)[\(（](.+?)[\)）]$/)
    if (!match) {
      return callback(new Error('请遵循严格格式：课程名（课程编号）'))
    }
  }
  callback()
}

const courseRules = {
  courseId: [{ required: true, validator: validateCourseInput, trigger: 'change' }],
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }],
  credits: [{ required: true, message: '请输入学分', trigger: 'change' }],
  hours: [{ required: true, message: '请输入学时', trigger: 'change' }]
}

function onCourseChange(id) {
  const c = store.courses.find(c => c.id === id)
  if (c) { courseForm.courseName = c.name; courseForm.credits = c.credits || 3; courseForm.hours = c.hours || 48; courseForm.semester = c.semester || '' }
}
function openAdd() {
  isEdit.value = false; editId.value = null
  Object.assign(courseForm, { courseId: null, courseName: '', semester: '', credits: 3, hours: 48 })
  setTimeout(() => {
    if (courseFormRef.value) courseFormRef.value.clearValidate()
  }, 0)
  showCourseDialog.value = true
}
function openEdit(row) {
  isEdit.value = true; editId.value = row.id
  Object.assign(courseForm, { 
    courseId: row.courseId, 
    courseName: getCourseAttr(row.courseId, 'name'), 
    semester: row.semester, 
    credits: getCourseAttr(row.courseId, 'credits'), 
    hours: getCourseAttr(row.courseId, 'hours') 
  })
  showCourseDialog.value = true
  setTimeout(() => {
    if (courseFormRef.value) courseFormRef.value.clearValidate()
  }, 0)
}
async function saveCourse() {
  if (!courseFormRef.value) return
  await courseFormRef.value.validate(async (valid) => {
    if (valid) {
      let finalCourseId = courseForm.courseId
      
      // If the input is a string, it means the user created a new course manually
      if (typeof finalCourseId === 'string') {
        const match = finalCourseId.match(/(.+?)[\(（](.+?)[\)）]$/)
        if (match) {
          const newCourseName = match[1].trim()
          const newCourseCode = match[2].trim()
          try {
            // The store internally adds the course and refetches, then returns the ID
            finalCourseId = await store.addCourse({ 
              name: newCourseName, 
              code: newCourseCode,
              credits: courseForm.credits,
              hours: courseForm.hours
            })
          } catch (e) {
            ElMessage.error('新建课程基础信息失败')
            return
          }
        }
      }

      const payload = {
        courseId: finalCourseId,
        semester: courseForm.semester
      }
      if (isEdit.value) {
        payload.id = editId.value
        store.updateCourseTask(editId.value, payload)
        ElMessage.success('已更新')
      } else {
        store.addCourseTask(payload)
        ElMessage.success('已添加')
      }
      showCourseDialog.value = false
    }
  })
}
function doDelete(row) {
  const courseName = getCourseAttr(row.courseId, 'name')
  ElMessageBox.confirm(`确认删除「${courseName}」？`, '确认', { type: 'warning' })
    .then(() => { store.deleteCourseTask(row.id); ElMessage.success('已删除') })
    .catch(() => {})
}

// 分配教师
const showAssignDialog = ref(false)
const assignTarget = ref(null)
const assignFormRef = ref(null)

const validateGradeInput = (rule, value, callback) => {
  if (value && !/^20\d{2}级$/.test(value)) {
    return callback(new Error('专业年级必须为 xxxx年级 格式，例如：2023级'))
  }
  callback()
}

const validateClassesInput = (rule, value, callback) => {
  if (!value || value.length === 0) {
    return callback(new Error('请选择至少一个教学班级'))
  }
  const isAllValid = value.every(cls => /班$/.test(cls) && cls.length >= 2)
  if (!isAllValid) {
    return callback(new Error('输入的班级必须以“班”结尾，例如：01班、计科2班'))
  }
  callback()
}

const assignRules = {
  teacherId: [{ required: true, message: '请选择任课教师', trigger: 'change' }],
  grade: [{ validator: validateGradeInput, trigger: 'blur' }],
  classes: [{ required: true, validator: validateClassesInput, trigger: 'change' }]
}

const grades = computed(() => {
  const gs = new Set()
  store.courseTasks.forEach(t => t.assignments?.forEach(a => { if (a.grade) gs.add(a.grade) }))
  return [...gs].sort()
})
const allClasses = computed(() => {
  const cs = new Set()
  store.courseTasks.forEach(t => t.assignments?.forEach(a => {
    if (a.classes) a.classes.split(',').forEach(c => cs.add(c.trim()))
  }))
  return [...cs].sort()
})
const assignForm = reactive({ teacherId: null, grade: '', classes: [], studentCount: 0 })

function assignedClasses(task) {
  return (task.assignments || []).flatMap(a => a.classes ? a.classes.split(',') : [])
}

function isTeacherTaken(teacherId) {
  if (!assignTarget.value || !assignTarget.value.assignments) return false
  return assignTarget.value.assignments.some(a => a.teacherId === teacherId)
}

function isClassTaken(cls) {
  const taken = assignedClasses(assignTarget.value)
  return taken.includes(cls) && !assignForm.classes.includes(cls)
}
function openAssign(row) {
  assignTarget.value = row
  Object.assign(assignForm, { teacherId: null, grade: '', classes: [], studentCount: 0 })
  showAssignDialog.value = true
}
async function confirmAssign() {
  if (!assignFormRef.value) return
  await assignFormRef.value.validate(async (valid) => {
    if (valid) {
      const payload = {
        taskId: assignTarget.value.id,
        teacherId: assignForm.teacherId,
        grade: assignForm.grade,
        classes: assignForm.classes.join(','),
        studentCount: assignForm.studentCount || 0,
        assignStatus: '已分配'
      }
      await store.assignTeacher(payload)
      const teacherName = activeTeachers.value.find(t => t.id === assignForm.teacherId)?.name || '未知老师'
      const courseName = getCourseAttr(assignTarget.value.courseId, 'name')
      ElMessage.success(`已为"${courseName}"分配${teacherName}`)
      showAssignDialog.value = false
    }
  })
}
</script>

<style scoped>
.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px; }
.course-info-banner {
  background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px; padding: 12px 16px;
}
.class-grid { display:flex; flex-wrap:wrap; gap:8px; }
.class-btn {
  width: 64px; text-align:center; padding: 6px 0; border: 1px solid #d1d5db; border-radius: 6px;
  font-size: 13px; color: #374151; cursor: pointer; transition: all .15s; user-select: none;
}
.class-btn:hover:not(.class-btn--disabled) { border-color: #2563eb; color: #2563eb; }
.class-btn--active { background: #2563eb; color: #fff !important; border-color: #2563eb !important; }
.class-btn--disabled { background: #f3f4f6; color: #9ca3af; cursor: not-allowed; text-decoration: line-through; }
</style>
