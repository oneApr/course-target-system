<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">教师信息管理</div>
        <div class="page-subtitle">管理系部教师账号与基本信息</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAdd">+ 添加教师</el-button>
    </div>

    <!-- Stats: 3 cards matching Figma -->
    <div class="stat-cards" style="grid-template-columns:repeat(3,1fr)">
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">教师总数</div><div class="stat-value">{{ store.teachers.length }}</div></div>
        <div class="stat-card-icon icon-blue"><el-icon><UserFilled /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">在职数量</div><div class="stat-value">{{ store.teachers.filter(t=>t.status === '1').length }}</div></div>
        <div class="stat-card-icon icon-green"><el-icon><UserFilled /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">离职数量</div><div class="stat-value">{{ store.teachers.filter(t=>t.status === '0').length }}</div></div>
        <div class="stat-card-icon icon-orange"><el-icon><UserFilled /></el-icon></div>
      </div>
    </div>

    <!-- Table -->
    <div class="section-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <div class="section-title">教师列表</div>
        <div style="display:flex;gap:10px">
          <el-select v-model="filterDept" placeholder="筛选系部" clearable style="width:140px">
            <el-option v-for="d in depts" :key="d" :label="d" :value="d" />
          </el-select>
          <el-input v-model="search" placeholder="搜索姓名/账号..." :prefix-icon="Search" clearable style="width:200px" />
        </div>
      </div>

      <el-table :data="filtered" stripe style="width:100%" table-layout="auto">
        <el-table-column prop="name" label="姓名" min-width="90" />
        <el-table-column prop="gender" label="性别" min-width="60" />
        <el-table-column prop="phone" label="联系电话" min-width="130" />
        <el-table-column prop="dept" label="所属系部" min-width="110" />
        <el-table-column prop="title" label="职称" min-width="120" />
        <el-table-column label="状态"  min-width="80">
          <template #default="{row}">
            <el-tag :type="row.status === '1' ? 'success' : 'info'" size="small">{{ row.status === '1' ? '在职' : '离职' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" :icon="Edit" @click="openEdit(row)" />
            <el-button link type="danger" size="small" :icon="Delete" @click="doDelete(row)" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Add/Edit dialog — matches Figma screenshot exactly -->
    <el-dialog
      v-model="showDialog"
      :title="isEdit ? '编辑教师信息' : '添加教师信息'"
      width="460px"
      destroy-on-close
    >
      <div style="font-size:13px;color:#94a3b8;margin-bottom:16px">
        请填写教师的基本信息和登录账号密码
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名 *" prop="name">
              <el-input v-model="form.name" placeholder="请输入教师姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别 *" prop="gender">
              <el-select v-model="form.gender" style="width:100%" placeholder="男">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="登录账号 *" prop="account">
              <el-input v-model="form.account" placeholder="请输入登录账号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属系部">
              <el-select v-model="form.dept" style="width:100%" placeholder="数学系" allow-create filterable>
                <el-option v-for="d in allDepts" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="登录密码">
          <el-input v-model="form.password" placeholder="请输入登录密码（编辑时留空则不修改）" type="password" show-password />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称">
              <el-select v-model="form.title" style="width:100%" placeholder="讲师" allow-create filterable>
                <el-option label="教授" value="教授" />
                <el-option label="副教授" value="副教授" />
                <el-option label="讲师" value="讲师" />
                <el-option label="助教" value="助教" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="在职" value="1" />
            <el-option label="离职" value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog=false">取消</el-button>
        <el-button type="primary" @click="doSave">{{ isEdit ? '保存' : '添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
import { useTeacherStore } from '@/stores/courses'
import { ElMessage, ElMessageBox } from 'element-plus'

const store = useTeacherStore()
onMounted(() => store.fetchTeachers())
const search = ref('')
const filterDept = ref('')
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const form = reactive({
  name: '', gender: '', account: '', dept: '', title: '',
  password: '', confirmPassword: '', phone: '', status: '1'
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  account: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
}

const allDepts = ['数学系', '计算机系', '电气工程系', '机械工程系', '物理系', '化学系']
const depts = computed(() => [...new Set(store.teachers.map(t => t.dept))])

const filtered = computed(() => store.teachers.filter(t => {
  if (filterDept.value && t.dept !== filterDept.value) return false
  if (search.value && !t.name.includes(search.value) && !t.account?.includes(search.value)) return false
  return true
}))

function openAdd() {
  isEdit.value = false; editId.value = null
  Object.assign(form, { name: '', gender: '', account: '', dept: '', title: '', password: '', confirmPassword: '', phone: '', status: '1' })
  showDialog.value = true
}

function openEdit(row) {
  isEdit.value = true; editId.value = row.id
  Object.assign(form, { name: row.name, gender: row.gender, account: row.account, dept: row.dept, title: row.title || '', password: '', confirmPassword: '', phone: row.phone || '', status: row.status })
  showDialog.value = true
}

function doSave() {
  formRef.value?.validate(valid => {
    if (!valid) return
    if (isEdit.value) {
      store.updateTeacher(editId.value, { name: form.name, gender: form.gender, account: form.account, dept: form.dept, title: form.title, phone: form.phone, status: form.status })
      ElMessage.success('教师信息已更新')
    } else {
      store.addTeacher({ name: form.name, gender: form.gender, account: form.account, dept: form.dept, title: form.title, phone: form.phone, status: form.status })
      ElMessage.success('教师添加成功')
    }
    showDialog.value = false
  })
}

function doDelete(row) {
  ElMessageBox.confirm(`确认删除教师「${row.name}」？`, '确认', { type: 'warning' })
    .then(() => { store.deleteTeacher(row.id); ElMessage.success('已删除') })
    .catch(() => {})
}
</script>

<style scoped>
.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px; }
</style>
