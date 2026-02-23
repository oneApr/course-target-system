<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">课程目标维护</div>
        <div class="page-subtitle">维护课程目标详情与毕业要求映射</div>
      </div>
    </div>

    <!-- 课程选择 -->
    <div class="section-card" style="margin-bottom:16px">
      <div style="display:flex;align-items:center;gap:16px;flex-wrap:wrap">
        <span style="font-size:14px;color:#374151;font-weight:600;flex-shrink:0">选择课程：</span>
        <el-select v-model="store.selectedCourseId" style="width:300px" placeholder="请选择课程">
          <el-option v-for="c in store.courses" :key="c.id" :label="`${c.name}（${c.code}）`" :value="c.id" />
        </el-select>
        <el-tag v-if="store.selectedCourse" type="info">
          {{ store.selectedCourse.dept }} · 授课教师：{{ currentCourseTeachers || '未分配' }}
        </el-tag>
      </div>
    </div>

    <!-- 目标统计 -->
    <div class="stat-cards" style="margin-bottom:16px" v-if="store.selectedObjectives.length">
      <div v-for="obj in store.selectedObjectives" :key="obj.id" class="stat-card" style="padding:14px 20px;cursor:pointer" @click="scrollToObj(obj.id)">
        <div class="stat-card-info">
          <div class="stat-label">{{ obj.tag }}</div>
          <div style="font-size:13px;color:#475569;margin-top:2px;max-width:160px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ obj.description }}</div>
        </div>
        <el-tag :style="{ background: obj.tagColor, color: obj.tagTextColor, border:'none' }" size="small">{{ obj.weight || '0%' }}</el-tag>
      </div>
    </div>


    <div class="section-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <div class="section-title">
          课程目标列表
          <span style="font-size:13px;color:#94a3b8;font-weight:400;margin-left:4px">— {{ store.selectedCourse?.name || '请选择课程' }}</span>
        </div>
        <div v-if="store.selectedObjectives.length">
          <span v-if="totalWeight !== 100" style="font-size:12px;color:#ef4444">⚠ 权重合计 {{ totalWeight }}%（建议100%）</span>
          <span v-else style="font-size:12px;color:#22c55e">✓ 权重合计 100%</span>
        </div>
      </div>

      <div v-if="!store.selectedObjectives.length" style="text-align:center;color:#94a3b8;padding:48px 0">
        <el-icon style="font-size:40px;margin-bottom:12px"><DocumentAdd /></el-icon>
        <div>暂无课程目标，请在"课程目标管理页面"添加</div>
      </div>

      <div v-else class="obj-list">
        <div v-for="obj in store.selectedObjectives" :key="obj.id" :id="`obj-${obj.id}`" class="obj-item">
          <div class="obj-tag-col">
            <el-tag :style="{ background: obj.tagColor, color: obj.tagTextColor, border:'none', fontWeight:700 }" size="default">{{ obj.tag }}</el-tag>
          </div>
          <div class="obj-body">
            <div class="obj-desc">{{ obj.description }}</div>
            <div class="obj-meta-row">
              <span class="meta-chip">毕业要求：{{ obj.requirements?.join('、') || '-' }}</span>
              <span class="meta-chip">指标点：{{ obj.indicator }}</span>
              <span class="meta-chip">权重：{{ obj.weight || '0%' }}</span>
            </div>
          </div>
          <div class="obj-actions">
            <el-button size="small" :icon="Edit" plain @click="openEditObj(obj)" />
            <el-button size="small" type="danger" :icon="Delete" plain @click="deleteObj(obj)" />
          </div>
        </div>
      </div>
    </div>

    <!-- Edit dialog -->
    <el-dialog v-model="showEditDialog" title="编辑课程目标" width="500px" destroy-on-close>
      <el-form :model="editForm" label-width="110px">
        <el-form-item label="目标类型">
          <el-select v-model="editForm.tag" style="width:100%" filterable allow-create default-first-option>
            <el-option v-for="t in historyTags" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标描述"><el-input v-model="editForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="关联毕业要求">
          <el-select v-model="editForm.requirements" multiple style="width:100%" filterable allow-create default-first-option>
            <el-option v-for="r in historyRequirements" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="指标点"><el-input v-model="editForm.indicator" /></el-form-item>
        <el-form-item label="权重(%)"><el-input-number v-model="editForm.weightNum" :min="0" :max="100" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEditObj">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Edit, Delete, DocumentAdd } from '@element-plus/icons-vue'
import { useCourseStore } from '@/stores/courses'
import { ElMessage, ElMessageBox } from 'element-plus'

const store = useCourseStore()

// 提取当前选中课程的所有讲师
const currentCourseTeachers = computed(() => {
  if (!store.selectedCourseId) return ''
  const tasks = store.courseTasks.filter(t => t.courseId === store.selectedCourseId)
  if (!tasks.length) return ''
  const names = tasks.flatMap(t => (t.assignments || []).map(a => a.teacherName)).filter(Boolean)
  return names.length > 0 ? [...new Set(names)].join('，') : ''
})

onMounted(async () => {
  await store.fetchCourses()
  await store.fetchCourseTasks() // Fetch course tasks
  if (store.selectedCourseId) {
    await store.fetchObjectives(store.selectedCourseId)
  }
})

watch(() => store.selectedCourseId, id => {
  if (id) store.fetchObjectives(id)
})

const historyTags = computed(() => {
  const s = new Set();
  store.selectedObjectives.forEach(o => { if (o.tag) s.add(o.tag) })
  return [...s].sort()
})

const historyRequirements = computed(() => {
  const s = new Set();
  store.selectedObjectives.forEach(o => {
    if (o.requirements) o.requirements.forEach(r => s.add(r))
  })
  return [...s].sort()
})

const totalWeight = computed(() =>
  store.selectedObjectives.reduce((s, o) => s + (parseInt(String(o.weight||'0').replace('%','')) || 0), 0)
)

function scrollToObj(id) {
  document.getElementById(`obj-${id}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

const showEditDialog = ref(false)
const editId = ref(null)
const editForm = reactive({ tag: '', requirements: [], indicator: '', weightNum: 0, description: '' })

function openEditObj(obj) {
  editId.value = obj.id
  Object.assign(editForm, { 
    tag: obj.tag, 
    requirements: Array.isArray(obj.requirements) ? obj.requirements : (obj.requirements ? [obj.requirements] : []), 
    indicator: obj.indicator, 
    weightNum: parseInt(String(obj.weight || '0').replace('%', '')) || 0, 
    description: obj.description 
  })
  showEditDialog.value = true
}

function saveEditObj() {
  store.updateObjective(editId.value, { 
    id: editId.value,
    courseId: store.selectedCourseId,
    tag: editForm.tag, 
    description: editForm.description, 
    requirements: editForm.requirements, 
    indicator: editForm.indicator, 
    weight: editForm.weightNum + '%'
  })
  showEditDialog.value = false
  ElMessage.success('已保存')
}

function deleteObj(obj) {
  ElMessageBox.confirm(`确认删除「${obj.tag}」？`, '确认', { type: 'warning' })
    .then(() => { store.deleteObjective(obj.id); ElMessage.success('已删除') })
    .catch(() => {})
}
</script>

<style scoped>
.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px; }
.field-block { display:flex; flex-direction:column; gap:6px; }
.field-lbl { font-size:12px; color:#374151; font-weight:500; }

/* Objective list */
.obj-list { display:flex; flex-direction:column; gap:10px; }
.obj-item { display:flex; align-items:flex-start; gap:16px; padding:16px 20px; background:#f8fafc; border-radius:10px; border:1px solid #e5e7eb; transition:box-shadow .15s; }
.obj-item:hover { box-shadow:0 2px 8px rgba(0,0,0,.06); }
.obj-tag-col { flex-shrink:0; padding-top:2px; min-width:56px; }
.obj-body { flex:1; }
.obj-desc { font-size:14px; font-weight:600; color:#1e293b; margin-bottom:8px; }
.obj-meta-row { display:flex; gap:8px; flex-wrap:wrap; }
.meta-chip { font-size:12px; color:#64748b; background:#e5e7eb; padding:2px 8px; border-radius:4px; }
.obj-actions { display:flex; gap:6px; flex-shrink:0; }
</style>
