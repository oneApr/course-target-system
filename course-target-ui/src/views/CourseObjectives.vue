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
            :label="`${c.name}（${c.semester || '暂无学期'}）`"
            :value="c.id"
          />
        </el-select>
        <div v-if="store.selectedCourse" style="font-size:13px;color:#64748b">
          年份：{{ (store.selectedCourse.semester || '').split('第')[0] || '-' }}
          &nbsp;学年&nbsp;·&nbsp;学期
        </div>
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
        <el-table-column prop="requirement" label="毕业要求" min-width="130" />
        <el-table-column label="权重" min-width="70" align="center">
          <template #default="{ row }">{{ row.weight }}%</template>
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
      <el-form :model="form" label-width="110px">
        <el-form-item label="目标类型">
          <el-select v-model="form.tag" style="width:100%">
            <el-option v-for="t in ['目标一','目标二','目标三','目标四']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请描述本课程目标..." />
        </el-form-item>
        <el-form-item label="关联毕业要求">
          <el-select v-model="form.requirement" style="width:100%">
            <el-option v-for="r in requirements" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="指标点">
          <el-input v-model="form.indicator" placeholder="如：指标点1.2" />
        </el-form-item>
        <el-form-item label="权重(%)">
          <el-input-number v-model="form.weight" :min="0" :max="100" style="width:100%" />
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
import { ref, reactive, watch, onMounted } from 'vue'
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

const requirements = ['工程知识','问题分析','设计/开发解决方案','研究','使用现代工具','工程与社会','环境和可持续发展','职业规范','个人和团队','沟通','项目管理','终身学习']

const form = reactive({ tag: '目标一', description: '', requirement: '工程知识', indicator: '', weight: 20 })

function openAddDialog() {
  isEdit.value = false; editId.value = null
  Object.assign(form, { tag: '目标一', description: '', requirement: '工程知识', indicator: '', weight: 20 })
  showDialog.value = true
}

function openEdit(row) {
  isEdit.value = true; editId.value = row.id
  Object.assign(form, { tag: row.tag, description: row.description, requirement: row.requirement, indicator: row.indicator, weight: Number(row.weight) })
  showDialog.value = true
}

function doSave() {
  if (!form.description) { ElMessage.warning('请填写目标描述'); return }
  const colors = store.tagColorMap[form.tag] || { bg: '#e5e7eb', text: '#374151' }
  if (isEdit.value) {
    store.updateObjective(editId.value, { ...form, tagColor: colors.bg, tagTextColor: colors.text })
    ElMessage.success('已更新')
  } else {
    store.addObjective({ courseId: store.selectedCourseId, ...form, tagColor: colors.bg, tagTextColor: colors.text })
    ElMessage.success('添加成功')
  }
  showDialog.value = false
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
