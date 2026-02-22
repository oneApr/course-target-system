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
          {{ store.selectedCourse.dept }} · 授课教师：{{ store.selectedCourse.teacher }}
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
        <el-tag :style="{ background: obj.tagColor, color: obj.tagTextColor, border:'none' }" size="small">{{ obj.weight }}%</el-tag>
      </div>
    </div>

    <!-- ====== 新增课程目标 ====== -->
    <div class="section-card" style="margin-bottom:16px">
      <div class="section-title" style="margin-bottom:16px">新增课程目标</div>

      <el-row :gutter="20">
        <el-col :span="4">
          <div class="field-block">
            <label class="field-lbl">目标类型</label>
            <el-select v-model="addForm.tag" style="width:100%">
              <el-option v-for="t in ['目标一','目标二','目标三','目标四']" :key="t" :label="t" :value="t" />
            </el-select>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="field-block">
            <label class="field-lbl">关联毕业要求</label>
            <el-select v-model="addForm.requirement" style="width:100%">
              <el-option v-for="r in requirements" :key="r" :label="r" :value="r" />
            </el-select>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="field-block">
            <label class="field-lbl">指标点（可选）</label>
            <el-input v-model="addForm.indicator" placeholder="如：指标点1.2" />
          </div>
        </el-col>
        <el-col :span="3">
          <div class="field-block">
            <label class="field-lbl">权重（%）</label>
            <el-input-number v-model="addForm.weightNum" :min="0" :max="100" style="width:100%" />
          </div>
        </el-col>
        <el-col :span="7">
          <div class="field-block">
            <label class="field-lbl">课程目标描述</label>
            <el-input v-model="addForm.description" placeholder="请描述本课程目标，使学生能够..." />
          </div>
        </el-col>
      </el-row>

      <div style="margin-top:16px">
        <el-button type="primary" @click="doAdd">新增目标</el-button>
      </div>
    </div>

    <!-- 目标列表 -->
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
        <div>暂无课程目标，请在上方"新增课程目标"添加</div>
      </div>

      <div v-else class="obj-list">
        <div v-for="obj in store.selectedObjectives" :key="obj.id" :id="`obj-${obj.id}`" class="obj-item">
          <div class="obj-tag-col">
            <el-tag :style="{ background: obj.tagColor, color: obj.tagTextColor, border:'none', fontWeight:700 }" size="default">{{ obj.tag }}</el-tag>
          </div>
          <div class="obj-body">
            <div class="obj-desc">{{ obj.description }}</div>
            <div class="obj-meta-row">
              <span class="meta-chip">毕业要求：{{ obj.requirement }}</span>
              <span class="meta-chip">指标点：{{ obj.indicator }}</span>
              <span class="meta-chip">权重：{{ obj.weight }}%</span>
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
          <el-select v-model="editForm.tag" style="width:100%">
            <el-option v-for="t in ['目标一','目标二','目标三','目标四']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标描述"><el-input v-model="editForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="关联毕业要求">
          <el-select v-model="editForm.requirement" style="width:100%">
            <el-option v-for="r in requirements" :key="r" :label="r" :value="r" />
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
import { ref, reactive, computed } from 'vue'
import { Edit, Delete, DocumentAdd } from '@element-plus/icons-vue'
import { useCourseStore } from '@/stores/courses'
import { ElMessage, ElMessageBox } from 'element-plus'

const store = useCourseStore()

const requirements = ['工程知识', '问题分析', '设计/开发解决方案', '研究', '使用现代工具', '工程与社会', '环境和可持续发展', '职业规范', '个人和团队', '沟通', '项目管理', '终身学习']

const addForm = reactive({
  tag: '目标一',
  description: '',
  weight: '',
  weightNum: 20,
  requirement: '工程知识',
  indicator: ''
})

const totalWeight = computed(() =>
  store.selectedObjectives.reduce((s, o) => s + (Number(o.weight) || 0), 0)
)

function doAdd() {
  if (!addForm.description) { ElMessage.warning('请输入课程目标描述'); return }
  const w = Number(addForm.weight) || addForm.weightNum || 20
  const colors = store.tagColorMap[addForm.tag] || { bg: '#e5e7eb', text: '#374151' }
  store.addObjective({
    courseId: store.selectedCourseId,
    tag: addForm.tag,
    description: addForm.description,
    requirement: addForm.requirement,
    indicator: addForm.indicator || '-',
    weight: w,
    tagColor: colors.bg,
    tagTextColor: colors.text
  })
  ElMessage.success('目标添加成功')
  addForm.description = ''; addForm.indicator = ''; addForm.weight = ''; addForm.weightNum = 20
}

function scrollToObj(id) {
  document.getElementById(`obj-${id}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

const showEditDialog = ref(false)
const editId = ref(null)
const editForm = reactive({ tag: '', requirement: '', indicator: '', weightNum: 0, description: '' })

function openEditObj(obj) {
  editId.value = obj.id
  Object.assign(editForm, { tag: obj.tag, requirement: obj.requirement, indicator: obj.indicator, weightNum: Number(obj.weight), description: obj.description })
  showEditDialog.value = true
}

function saveEditObj() {
  const colors = store.tagColorMap[editForm.tag] || { bg: '#e5e7eb', text: '#374151' }
  store.updateObjective(editId.value, { tag: editForm.tag, description: editForm.description, requirement: editForm.requirement, indicator: editForm.indicator, weight: editForm.weightNum, tagColor: colors.bg, tagTextColor: colors.text })
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
