<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">达成数据审核</div>
        <div class="page-subtitle">审核教师提交的课程达成数据，填写审核意见后通过或驳回</div>
      </div>
    </div>

    <!-- Stats -->
    <div class="stat-cards" style="grid-template-columns:repeat(4,1fr)">
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">待审核</div><div class="stat-value">{{ pending.length }}</div></div>
        <div class="stat-card-icon icon-orange"><el-icon><Clock /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">已通过</div><div class="stat-value">{{ approved.length }}</div></div>
        <div class="stat-card-icon icon-green"><el-icon><CircleCheck /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">已驳回</div><div class="stat-value">{{ rejected.length }}</div></div>
        <div class="stat-card-icon icon-red"><el-icon><CircleClose /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">提交总数</div><div class="stat-value">{{ store.uploadRecords.length }}</div></div>
        <div class="stat-card-icon icon-blue"><el-icon><Document /></el-icon></div>
      </div>
    </div>

    <!-- Filters -->
    <div class="section-card" style="margin-bottom:16px;padding:16px 24px">
      <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
        <el-select v-model="filterStatus" placeholder="筛选状态" clearable style="width:140px">
          <el-option label="全部" value="" />
          <el-option label="待审核" value="待审核" />
          <el-option label="已通过" value="已通过" />
          <el-option label="已驳回" value="已驳回" />
        </el-select>
        <el-select v-model="filterCourse" placeholder="筛选课程" clearable style="width:180px">
          <el-option v-for="c in store.courses" :key="c.id" :label="c.name" :value="c.name" />
        </el-select>
        <el-input v-model="search" placeholder="搜索课程/教师..." :prefix-icon="Search" clearable style="width:200px" />
      </div>
    </div>

    <!-- Table -->
    <div class="section-card">
      <div class="section-title" style="margin-bottom:16px">数据提交记录</div>
      <el-table :data="filtered" stripe style="width:100%" table-layout="auto">
        <el-table-column prop="courseName" label="课程名称" min-width="120" />
        <el-table-column prop="semester" label="学期" min-width="150" />
        <el-table-column prop="teacher" label="提交教师" min-width="90" />
        <el-table-column prop="uploadTime" label="提交时间" min-width="110" />
        <el-table-column label="达成度数据" min-width="200">
          <template #default="{row}">
            <div v-if="row.objectives" style="display:flex;gap:6px;flex-wrap:wrap">
              <span v-for="(val, key) in row.objectives" :key="key" class="obj-chip">
                {{ key }}: <b>{{ val }}%</b>
              </span>
            </div>
            <div v-else-if="row.objective1!=null" style="display:flex;gap:6px;flex-wrap:wrap">
              <span v-if="row.objective1!=null" class="obj-chip">目标一: <b>{{ row.objective1 }}%</b></span>
              <span v-if="row.objective2!=null" class="obj-chip">目标二: <b>{{ row.objective2 }}%</b></span>
              <span v-if="row.objective3!=null" class="obj-chip">目标三: <b>{{ row.objective3 }}%</b></span>
            </div>
            <span v-else style="color:#94a3b8;font-size:12px">暂无数据</span>
          </template>
        </el-table-column>
        <el-table-column label="审核意见" min-width="160">
          <template #default="{row}">
            <div v-if="row.auditComment" style="font-size:12px">
              <el-tag v-if="row.status==='已通过'" type="success" size="small" style="margin-bottom:4px">通过意见</el-tag>
              <el-tag v-else-if="row.status==='已驳回'" type="danger" size="small" style="margin-bottom:4px">驳回原因</el-tag>
              <div style="color:#475569;line-height:1.5">{{ row.auditComment }}</div>
            </div>
            <span v-else style="color:#94a3b8;font-size:12px">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{row}">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{row}">
            <template v-if="row.status === '待审核'">
              <el-button link type="success" size="small" @click="openAudit(row, 'approve')">通过</el-button>
              <el-button link type="danger"  size="small" @click="openAudit(row, 'reject')">驳回</el-button>
            </template>
            <template v-else>
              <el-button link type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
              <el-button v-if="row.status==='已驳回'" link size="small" @click="doReview(row)">重新审核</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Unified Audit Dialog -->
    <el-dialog
      v-model="showAuditDialog"
      :title="auditMode==='approve' ? '填写审核意见（通过）' : '填写驳回原因'"
      width="460px"
      destroy-on-close
    >
      <!-- Course banner -->
      <div v-if="auditTarget" class="audit-banner" :class="auditMode==='approve'?'banner-green':'banner-red'">
        <div style="font-weight:600;font-size:13px">{{ auditTarget.courseName }} · {{ auditTarget.semester }}</div>
        <div style="font-size:12px;margin-top:2px;opacity:.8">提交教师：{{ auditTarget.teacher }}</div>
      </div>

      <el-form style="margin-top:16px">
        <el-form-item>
          <template #label>
            <span style="font-weight:600">
              {{ auditMode==='approve' ? '审核意见' : '驳回原因 *' }}
            </span>
            <span v-if="auditMode==='approve'" style="font-size:12px;color:#94a3b8;margin-left:6px">（可选，无意见可留空）</span>
          </template>
          <el-input
            v-model="auditComment"
            type="textarea"
            :rows="4"
            :placeholder="auditMode==='approve'
              ? '如：数据填报规范，达成度符合预期，审核通过...'
              : '请详细说明驳回原因，教师将看到此内容...'"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAuditDialog=false">取消</el-button>
        <el-button
          :type="auditMode==='approve'?'success':'danger'"
          @click="confirmAudit"
        >{{ auditMode==='approve' ? '确认通过' : '确认驳回' }}</el-button>
      </template>
    </el-dialog>

    <!-- Detail dialog -->
    <el-dialog v-model="showDetailDialog" title="数据详情" width="500px" destroy-on-close>
      <div v-if="detailRow">
        <div style="margin-bottom:16px">
          <div style="font-weight:600;font-size:15px;margin-bottom:4px">{{ detailRow.courseName }}</div>
          <div style="font-size:13px;color:#64748b">{{ detailRow.semester }} · 教师：{{ detailRow.teacher }} · {{ detailRow.uploadTime }}</div>
        </div>

        <!-- Objective progress bars -->
        <div style="display:flex;flex-direction:column;gap:12px;margin-bottom:16px">
          <template v-if="detailRow.objectives">
            <div v-for="(val, key) in detailRow.objectives" :key="key">
              <div style="display:flex;justify-content:space-between;margin-bottom:4px">
                <span style="font-size:13px;font-weight:600">{{ key }}</span>
                <span :style="{fontWeight:600,color:getColor(val)}">{{ val }}%</span>
              </div>
              <el-progress :percentage="val" :stroke-width="8" :color="getColor(val)" :show-text="false" />
            </div>
          </template>
          <template v-else>
            <div v-if="detailRow.objective1!=null">
              <div style="display:flex;justify-content:space-between;margin-bottom:4px"><span style="font-size:13px;font-weight:600">目标一</span><span :style="{fontWeight:600,color:getColor(detailRow.objective1)}">{{ detailRow.objective1 }}%</span></div>
              <el-progress :percentage="detailRow.objective1" :stroke-width="8" :color="getColor(detailRow.objective1)" :show-text="false" />
            </div>
            <div v-if="detailRow.objective2!=null">
              <div style="display:flex;justify-content:space-between;margin-bottom:4px"><span style="font-size:13px;font-weight:600">目标二</span><span :style="{fontWeight:600,color:getColor(detailRow.objective2)}">{{ detailRow.objective2 }}%</span></div>
              <el-progress :percentage="detailRow.objective2" :stroke-width="8" :color="getColor(detailRow.objective2)" :show-text="false" />
            </div>
            <div v-if="detailRow.objective3!=null">
              <div style="display:flex;justify-content:space-between;margin-bottom:4px"><span style="font-size:13px;font-weight:600">目标三</span><span :style="{fontWeight:600,color:getColor(detailRow.objective3)}">{{ detailRow.objective3 }}%</span></div>
              <el-progress :percentage="detailRow.objective3" :stroke-width="8" :color="getColor(detailRow.objective3)" :show-text="false" />
            </div>
          </template>
        </div>

        <!-- Audit comment section -->
        <div v-if="detailRow.auditComment" class="detail-comment" :class="detailRow.status==='已驳回'?'comment-red':'comment-green'">
          <div style="font-size:12px;font-weight:700;margin-bottom:6px">
            {{ detailRow.status==='已驳回' ? '🚫 驳回原因' : '✅ 审核意见' }}
          </div>
          <div style="font-size:13px;line-height:1.6">{{ detailRow.auditComment }}</div>
        </div>
        <div v-else-if="detailRow.status!=='待审核'" style="font-size:12px;color:#94a3b8;font-style:italic">审核人未填写意见</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useCourseStore } from '@/stores/courses'
import { auditRecord } from '@/api/upload'
import { ElMessage } from 'element-plus'

const store = useCourseStore()
onMounted(async () => {
  await store.fetchCourses()
  await store.fetchUploadRecords()
})
const filterStatus = ref('')
const filterCourse = ref('')
const search = ref('')

const pending  = computed(() => store.uploadRecords.filter(r => r.status === '待审核'))
const approved = computed(() => store.uploadRecords.filter(r => r.status === '已通过'))
const rejected = computed(() => store.uploadRecords.filter(r => r.status === '已驳回'))

const filtered = computed(() => store.uploadRecords.filter(r => {
  if (filterStatus.value && r.status !== filterStatus.value) return false
  if (filterCourse.value && r.courseName !== filterCourse.value) return false
  if (search.value) {
    const q = search.value.toLowerCase()
    if (!r.courseName?.toLowerCase().includes(q) && !r.teacher?.includes(q)) return false
  }
  return true
}))

function statusTagType(s) {
  return s === '已通过' ? 'success' : s === '已驳回' ? 'danger' : 'warning'
}
function getColor(v) { return v >= 85 ? '#22c55e' : v >= 75 ? '#3b82f6' : v >= 60 ? '#f59e0b' : '#ef4444' }

// ---- Unified Audit Dialog ----
const showAuditDialog = ref(false)
const auditMode = ref('approve')   // 'approve' | 'reject'
const auditComment = ref('')
const auditTarget = ref(null)

function openAudit(row, mode) {
  auditTarget.value = row
  auditMode.value = mode
  auditComment.value = ''
  showAuditDialog.value = true
}

async function confirmAudit() {
  if (auditMode.value === 'reject' && !auditComment.value.trim()) {
    ElMessage.warning('驳回时必须填写驳回原因')
    return
  }
  const isApprove = auditMode.value === 'approve'
  try {
    await auditRecord(auditTarget.value.id, {
      status: isApprove ? '已通过' : '已驳回',
      auditComment: auditComment.value.trim()
    })
    await store.fetchUploadRecords()
    ElMessage[isApprove ? 'success' : 'warning'](
      `已${isApprove ? '通过' : '驳回'}「${auditTarget.value.courseName}」的数据`
    )
  } catch (_) {}
  showAuditDialog.value = false
}

// Reset to pending for re-review
async function doReview(row) {
  try {
    await auditRecord(row.id, { status: '待审核', auditComment: '' })
    await store.fetchUploadRecords()
    ElMessage.info('已重置为待审核')
  } catch (_) {}
}

// Detail view
const showDetailDialog = ref(false)
const detailRow = ref(null)
function viewDetail(row) {
  detailRow.value = row
  showDetailDialog.value = true
}
</script>

<style scoped>
.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px; }
.obj-chip { font-size:12px; background:#eff6ff; color:#2563eb; padding:2px 8px; border-radius:4px; }
.audit-banner {
  padding: 10px 14px; border-radius: 8px; margin-bottom: 4px;
}
.banner-green { background:#f0fdf4; border:1px solid #86efac; color:#15803d; }
.banner-red   { background:#fef2f2; border:1px solid #fca5a5; color:#dc2626; }
.detail-comment { padding: 12px 14px; border-radius: 8px; margin-top: 4px; }
.comment-green { background:#f0fdf4; border:1px solid #86efac; color:#14532d; }
.comment-red   { background:#fef2f2; border:1px solid #fca5a5; color:#7f1d1d; }
</style>
