<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">课程任务管理</div>
        <div class="page-subtitle">管理课程任务信息和教师任务分配</div>
      </div>
      <div style="display:flex;gap:8px">
        <el-button :icon="Download">下载模板</el-button>
        <el-button :icon="Upload">批量导入</el-button>
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
        <el-table-column prop="courseName" label="课程名称" min-width="120" />
        <el-table-column prop="semester" label="学期" min-width="160" />
        <el-table-column label="学分/学时" min-width="110">
          <template #default="{row}">{{ row.credits }}学分 / {{ row.hours }}学时</template>
        </el-table-column>
        <el-table-column label="任课教师" min-width="200">
          <template #default="{row}">
            <div v-if="row.assignments?.length" style="display:flex;flex-direction:column;gap:3px">
              <div v-for="a in row.assignments" :key="a.teacher" style="font-size:12px;color:#374151">
                <b>{{ a.teacher }}</b>
                <span style="color:#64748b;margin-left:4px">{{ a.classes?.join(' ') }}（{{ a.studentCount }}人）</span>
              </div>
            </div>
            <span v-else style="color:#94a3b8;font-size:12px">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="分配状态" min-width="90">
          <template #default="{row}">
            <el-tag :type="row.assignments?.length ? 'success' : 'info'" size="small">
              {{ row.assignments?.length ? '已分配' : '未分配' }}
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
      <el-form :model="courseForm" label-width="90px">
        <el-form-item label="课程">
          <el-select v-model="courseForm.courseId" style="width:100%" @change="onCourseChange">
            <el-option v-for="c in store.courses" :key="c.id" :label="`${c.name}（${c.code}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期"><el-input v-model="courseForm.semester" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="学分"><el-input-number v-model="courseForm.credits" :min="0.5" :step="0.5" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="学时"><el-input-number v-model="courseForm.hours" :min="8" style="width:100%" /></el-form-item></el-col>
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
        为"{{ assignTarget?.courseName }}"分配任课教师和教学班级
      </div>

      <!-- Course info banner -->
      <div v-if="assignTarget" class="course-info-banner">
        <div style="font-size:13px;font-weight:600;color:#2563eb">
          课程信息：{{ assignTarget.courseName }} | {{ assignTarget.semester }}
        </div>
        <div v-if="assignTarget.assignments?.length" style="font-size:12px;color:#64748b;margin-top:4px">
          已分配班级：{{ assignedClasses(assignTarget).join('、') }}
        </div>
      </div>

      <el-form :model="assignForm" label-position="top" style="margin-top:16px">
        <el-form-item label="任课教师 *">
          <el-select v-model="assignForm.teacher" style="width:100%" placeholder="请选择教师">
            <el-option v-for="t in activeTeachers" :key="t.id" :label="`${t.name}（${t.dept}）`" :value="t.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业年级">
          <el-select v-model="assignForm.grade" style="width:100%" placeholder="请选择专业年级">
            <el-option v-for="g in grades" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <template #label>
            <span style="font-weight:600">教学班级 *</span>
            <span style="font-size:12px;color:#94a3b8;margin-left:6px">请选择要分配的班级（已分配的班级不可再选）</span>
          </template>
          <div class="class-grid">
            <div
              v-for="cls in allClasses"
              :key="cls"
              class="class-btn"
              :class="{
                'class-btn--active': assignForm.classes.includes(cls),
                'class-btn--disabled': isClassTaken(cls)
              }"
              @click="toggleClass(cls)"
            >{{ cls }}</div>
          </div>
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
})
const search = ref('')

// Stats
const uniqueTeachers = computed(() => {
  const names = new Set()
  store.courseTasks.forEach(t => t.assignments?.forEach(a => names.add(a.teacher)))
  return names.size
})

const activeTeachers = computed(() => teacherStore.teachers.filter(t => t.status === '在职'))

const filtered = computed(() => {
  const q = search.value.toLowerCase()
  if (!q) return store.courseTasks
  return store.courseTasks.filter(t =>
    t.courseName?.toLowerCase().includes(q) ||
    t.assignments?.some(a => a.teacher?.includes(q))
  )
})

// ===== Add/Edit Course =====
const showCourseDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const courseForm = reactive({ courseId: null, courseName: '', semester: '', credits: 3, hours: 48 })

function onCourseChange(id) {
  const c = store.courses.find(c => c.id === id)
  if (c) { courseForm.courseName = c.name; courseForm.credits = c.credits || 3; courseForm.hours = c.hours || 48; courseForm.semester = c.semester || '' }
}
function openAdd() {
  isEdit.value = false; editId.value = null
  Object.assign(courseForm, { courseId: null, courseName: '', semester: '', credits: 3, hours: 48 })
  showCourseDialog.value = true
}
function openEdit(row) {
  isEdit.value = true; editId.value = row.id
  Object.assign(courseForm, { courseId: row.courseId, courseName: row.courseName, semester: row.semester, credits: row.credits, hours: row.hours })
  showCourseDialog.value = true
}
function saveCourse() {
  if (!courseForm.courseId) { ElMessage.warning('请选择课程'); return }
  if (isEdit.value) {
    store.updateCourseTask(editId.value, { ...courseForm })
    ElMessage.success('已更新')
  } else {
    store.addCourseTask({ ...courseForm })
    ElMessage.success('已添加')
  }
  showCourseDialog.value = false
}
function doDelete(row) {
  ElMessageBox.confirm(`确认删除「${row.courseName}」？`, '确认', { type: 'warning' })
    .then(() => { store.deleteCourseTask(row.id); ElMessage.success('已删除') })
    .catch(() => {})
}

// ===== Assign Teacher =====
const showAssignDialog = ref(false)
const assignTarget = ref(null)
const grades = ['2021级', '2022级', '2023级', '2024级']
const allClasses = ['01班','02班','03班','04班','05班','06班','07班','08班','09班','10班']
const assignForm = reactive({ teacher: '', grade: '', classes: [], studentCount: 0 })

function assignedClasses(task) {
  return (task.assignments || []).flatMap(a => a.classes || [])
}
function isClassTaken(cls) {
  const taken = assignedClasses(assignTarget.value)
  return taken.includes(cls) && !assignForm.classes.includes(cls)
}
function toggleClass(cls) {
  if (isClassTaken(cls)) return
  const idx = assignForm.classes.indexOf(cls)
  if (idx === -1) assignForm.classes.push(cls)
  else assignForm.classes.splice(idx, 1)
}
function openAssign(row) {
  assignTarget.value = row
  Object.assign(assignForm, { teacher: '', grade: '', classes: [], studentCount: 0 })
  showAssignDialog.value = true
}
function confirmAssign() {
  if (!assignForm.teacher) { ElMessage.warning('请选择任课教师'); return }
  if (!assignForm.classes.length) { ElMessage.warning('请选择教学班级'); return }
  const existing = assignTarget.value.assignments || []
  const newAssignment = { teacher: assignForm.teacher, grade: assignForm.grade, classes: [...assignForm.classes], studentCount: assignForm.studentCount }
  const merged = [...existing.filter(a => a.teacher !== assignForm.teacher), newAssignment]
  store.updateCourseTask(assignTarget.value.id, { assignments: merged, teacher: merged.map(a=>a.teacher).join('、'), studentCount: merged.reduce((s,a)=>s+a.studentCount,0) })
  ElMessage.success(`已为"${assignTarget.value.courseName}"分配${assignForm.teacher}`)
  showAssignDialog.value = false
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
