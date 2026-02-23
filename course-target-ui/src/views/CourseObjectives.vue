<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">课程目标管理</div>
      </div>
      <el-button type="primary" @click="openAddDialog">+ 添加课程目标</el-button>
    </div>

    <!-- Course Selector -->
    <div class="section-card" style="margin-bottom:16px">
      <div style="display:flex;align-items:center;gap:16px;flex-wrap:wrap">
        <span style="font-size:14px;font-weight:600;color:#374151;flex-shrink:0">当前课程</span>
        <el-select v-model="store.selectedCourseId" style="width:320px">
          <el-option
            v-for="c in store.courses"
            :key="c.id"
            :label="c.name"
            :value="c.id"
          />
        </el-select>
      </div>
    </div>

    <!-- Objectives Table -->
    <div class="section-card">
      <div class="section-title" style="margin-bottom:16px">
        课程目标对照表（{{ store.selectedCourse?.name || '请选择课程' }}）
      </div>

      <el-table :data="store.selectedObjectives" stripe style="width:100%" table-layout="auto">
        <el-table-column label="目标点" min-width="100">
          <template #default="{ row }">
            <el-tag
              :style="{ background: row.tagColor, color: row.tagTextColor, border: 'none', fontWeight: 700 }"
              size="default"
            >{{ row.tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="目标描述" min-width="260" show-overflow-tooltip />
        <el-table-column prop="indicator" label="指标点" min-width="110" />
        <el-table-column label="毕业要求" min-width="130">
          <template #default="{ row }">
            {{ row.requirements?.join('、') || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="权重" min-width="70" align="center">
          <template #default="{ row }">{{ row.weight || '0%' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" :icon="Edit" @click="openEdit(row)" />
            <el-button link type="danger" size="small" :icon="Delete" @click="doDelete(row)" />
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!store.selectedObjectives.length" style="text-align:center;padding:40px 0;color:#94a3b8">
        暂无课程目标，请点击右上角"添加课程目标"
      </div>
    </div>

    <!-- Add Dialog -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑课程目标' : '添加课程目标'" width="540px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="目标类型" prop="tag">
          <el-input v-model="form.tag" placeholder="如：课程目标一" />
        </el-form-item>
        <el-form-item label="目标描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请描述本课程目标..." />
        </el-form-item>
        <el-form-item label="关联毕业要求" prop="requirements">
          <el-select v-model="form.requirements" multiple style="width:100%" placeholder="可手填或选择，若无要求可填'无'" filterable allow-create default-first-option>
            <el-option label="无" value="无" />
            <el-option v-for="r in historyRequirements" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="指标点" prop="indicator">
          <el-input v-model="form.indicator" placeholder="如：指标点1.2" />
        </el-form-item>
        <el-form-item label="权重(%)" prop="weight">
          <el-input-number v-model="form.weight" :min="1" :max="100" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="doSave">{{ isEdit ? '保存修改' : '确认添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { Edit, Delete } from '@element-plus/icons-vue'
import { useCourseStore } from '@/stores/courses'
import { ElMessage, ElMessageBox } from 'element-plus'

const store = useCourseStore()
onMounted(async () => {
  await store.fetchCourses()
  if (store.selectedCourseId) store.fetchObjectives(store.selectedCourseId)
})
watch(() => store.selectedCourseId, id => { if (id) store.fetchObjectives(id) })
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)



const historyRequirements = computed(() => {
  const s = new Set();
  store.selectedObjectives.forEach(o => {
    if (o.requirements) o.requirements.forEach(r => s.add(r))
  })
  return [...s].sort()
})

const formRef = ref(null)
const form = reactive({ tag: '', description: '', requirements: [], indicator: '', weight: 10 })

const rules = {
  tag: [{ required: true, message: '请输入目标类型', trigger: 'blur' }],
  description: [{ required: true, message: '请输入目标描述', trigger: 'blur' }],
  requirements: [{ type: 'array', required: true, message: '请选择或输入关联毕业要求', trigger: 'change' }],
  indicator: [{ required: true, message: '请输入指标点，无需要请填写"无"', trigger: 'blur' }],
  weight: [{ required: true, message: '请输入大于0的权重', trigger: 'change' }]
}

function openAddDialog() {
  isEdit.value = false; editId.value = null
  Object.assign(form, { tag: '', description: '', requirements: [], indicator: '', weight: 10 })
  setTimeout(() => {
    if (formRef.value) formRef.value.clearValidate()
  }, 0)
  showDialog.value = true
}

function openEdit(row) {
  isEdit.value = true; editId.value = row.id
  Object.assign(form, { 
    tag: row.tag, 
    description: row.description, 
    requirements: Array.isArray(row.requirements) ? row.requirements : (row.requirements ? [row.requirements] : []), 
    indicator: row.indicator, 
    weight: parseInt(String(row.weight || '0').replace('%', '')) || 0
  })
  showDialog.value = true
  setTimeout(() => {
    if (formRef.value) formRef.value.clearValidate()
  }, 0)
}

async function doSave() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      const payload = { ...form, weight: form.weight + '%' }
      if (isEdit.value) {
        store.updateObjective(editId.value, { id: editId.value, courseId: store.selectedCourseId, ...payload })
        ElMessage.success('已更新')
      } else {
        store.addObjective({ courseId: store.selectedCourseId, ...payload })
        ElMessage.success('添加成功')
      }
      showDialog.value = false
    }
  })
}

function doDelete(row) {
  ElMessageBox.confirm(`确认删除「${row.tag}」？`, '确认', { type: 'warning' })
    .then(() => { store.deleteObjective(row.id); ElMessage.success('已删除') })
    .catch(() => {})
}
</script>

<style scoped>
.page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; }
</style>
