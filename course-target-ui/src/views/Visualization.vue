<template>
  <div class="page-container">
    <div class="page-header d-flex-between">
      <div>
        <div class="page-title">数据可视化</div>
        <div class="page-subtitle">全系课程目标达成情况图表分析</div>
      </div>
      <div class="header-actions">
        <el-button :icon="Download">导出数据</el-button>
        <el-button type="primary" :icon="Document">生成报告</el-button>
      </div>
    </div>

    <div class="section-card filter-bar">
      <div class="filter-group">
        <span class="filter-label">选择课程</span>
        <el-select v-model="selectedCourse" style="width:220px">
          <el-option v-for="c in store.courses" :key="c.id" :label="c.name" :value="c.name" />
        </el-select>
      </div>
      <div class="filter-group">
        <span class="filter-label">选择学期</span>
        <el-select v-model="selectedSemester" style="width:220px">
          <el-option v-for="s in allSemesters" :key="s" :label="s" :value="s" />
        </el-select>
      </div>
      <div class="filter-group">
        <span class="filter-label">查看粒度</span>
        <el-select v-model="granularity" style="width:140px">
          <el-option label="按课程" value="course" />
          <el-option label="按学期" value="semester" />
        </el-select>
      </div>
    </div>


    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card-info">
          <div class="stat-label">综合达成度</div>
          <div class="stat-value">{{ stats.overall }}%</div>
        </div>
        <div class="stat-card-icon icon-blue"><el-icon><TrendCharts /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">知识目标</div><div class="stat-value">{{ stats.obj1 }}%</div></div>
        <div class="stat-card-icon icon-blue" style="background:#eff6ff;color:#3b82f6"><el-icon><Aim /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">能力目标</div><div class="stat-value">{{ stats.obj2 }}%</div></div>
        <div class="stat-card-icon icon-green"><el-icon><Aim /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info"><div class="stat-label">素质目标</div><div class="stat-value">{{ stats.obj3 }}%</div></div>
        <div class="stat-card-icon icon-purple"><el-icon><Aim /></el-icon></div>
      </div>
    </div>

    <!-- Charts row 1 -->
    <div class="charts-row">
      <div class="section-card chart-card">
        <div class="section-title"><el-icon><DataLine /></el-icon> 课程目标达成度</div>
        <div ref="barChart" class="chart-area"></div>
      </div>
      <div class="section-card chart-card">
        <div class="section-title"><el-icon><DataLine /></el-icon> 全系课程对比</div>
        <div ref="groupBarChart" class="chart-area"></div>
      </div>
    </div>

    <!-- Trend chart -->
    <div class="section-card">
      <div class="section-title"><el-icon><TrendCharts /></el-icon> 历史趋势分析</div>
      <div ref="trendChart" style="height:300px;width:100%"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { Download, Document, TrendCharts, Aim, DataLine } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { useCourseStore } from '@/stores/courses'

const store = useCourseStore()

// 选中的过滤条件
const selectedCourse = ref('')
const selectedSemester = ref('')
const granularity = ref('course')

// 全量学期下拉选项（从现有任务中去重提取）
const allSemesters = computed(() => {
  const s = new Set(store.courseTasks.map(t => t.semester).filter(Boolean))
  return [...s].sort((a, b) => b.localeCompare(a))
})

// 监听数据加载第一项为默认选中
watch(() => store.courses, (val) => {
  if (val.length && !selectedCourse.value) selectedCourse.value = val[0].name
}, { immediate: true })

watch(() => allSemesters.value, (val) => {
  if (val.length && !selectedSemester.value) selectedSemester.value = val[0]
}, { immediate: true })

// 过滤后的当前课程信息和目标
const currentCourseTarget = computed(() => store.courses.find(c => c.name === selectedCourse.value))
const currentObjectives = computed(() => store.objectives.filter(o => o.courseId === currentCourseTarget.value?.id))

// --- 动态统计卡片数据 ---
const stats = computed(() => {
  let tasks = []
  if (granularity.value === 'course') {
    tasks = store.courseTasks.filter(t => t.courseName === selectedCourse.value && t.status === '已审核')
  } else {
    tasks = store.courseTasks.filter(t => t.semester === selectedSemester.value && t.status === '已审核')
  }
  
  if (!tasks.length) return { overall: 0, obj1: 0, obj2: 0, obj3: 0 }
  
  // 简单平均计算
  const avg = list => list.length ? (list.reduce((a,b)=>a+b,0) / list.length).toFixed(1) : 0
  return {
    overall: avg(tasks.map(t => t.achievementRate).filter(Boolean)),
    obj1: avg(tasks.map(t => t.objective1).filter(Boolean)), // 如果后端有该字段
    obj2: avg(tasks.map(t => t.objective2).filter(Boolean)),
    obj3: avg(tasks.map(t => t.objective3).filter(Boolean))
  }
})

// --- 动态图表数据 ---
const vizBarData = computed(() => {
  const records = store.uploadRecords.filter(r => r.courseName === selectedCourse.value && r.status === '已通过')
  if (!records.length) return { categories: ['暂无数据'], values: [0] }
  
  // 取最新的一条通过记录
  const latest = records[0] 
  return {
    categories: ['目标一', '目标二', '目标三', '目标四'].slice(0, currentObjectives.value.length || 3),
    values: [latest.objective1||0, latest.objective2||0, latest.objective3||0, latest.objective4||0].slice(0, currentObjectives.value.length || 3)
  }
})

const vizGroupBarData = computed(() => {
  const courses = [...new Set(store.uploadRecords.map(r => r.courseName))]
  const s1 = [], s2 = [], s3 = []
  
  courses.forEach(c => {
    // 找该课程最新通过记录
    const latest = store.uploadRecords.find(r => r.courseName === c && r.status === '已通过')
    s1.push(latest?.objective1 || 0)
    s2.push(latest?.objective2 || 0)
    s3.push(latest?.objective3 || 0)
  })
  
  return {
    courses: courses.length ? courses.slice(0, 5) : ['暂无课程'], // 最多5门
  }
})

const vizTrendData = computed(() => {
  const semesters = allSemesters.value.slice(0, 4).reverse() // 取最近4个学期并正序
  if (!semesters.length) return { semesters: ['暂无'], series: [] }
  
  const targetCourses = store.courses.slice(0, 3).map(c => c.name) // 取前3门课做对比
  
  const series = targetCourses.map((cName, i) => {
    const colors = ['#3b82f6', '#22c55e', '#8b5cf6']
    const values = semesters.map(sem => {
      // 找该课该学期审核通过的
      const r = store.uploadRecords.find(ur => ur.courseName === cName && ur.semester === sem && ur.status === '已通过')
      if (r) {
         // 这里取均值，如果有整体达成度最好，没有则算三目标均值
         const v1 = r.objective1||0, v2 = r.objective2||0, v3 = r.objective3||0
         const count = (r.objective1?1:0) + (r.objective2?1:0) + (r.objective3?1:0)
         return count ? ((v1+v2+v3)/count).toFixed(1) : 0
      }
      return null
    })
    return { name: cName, color: colors[i], values }
  })
  
  return { semesters, series }
})

const barChart = ref(null)
const groupBarChart = ref(null)
const trendChart = ref(null)

let barInstance, groupInstance, trendInstance

onMounted(async () => {
  await store.fetchCourses()
  await store.fetchCourseTasks()
  await store.fetchUploadRecords()
  // 并发请求所有课程的目标
  await Promise.all(store.courses.map(c => store.fetchObjectives(c.id)))
  
  initBarChart()
  initGroupBarChart()
  initTrendChart()
  window.addEventListener('resize', handleResize)
})

watch(() => vizBarData.value, () => initBarChart(), { deep: true })
watch(() => vizGroupBarData.value, () => initGroupBarChart(), { deep: true })
watch(() => vizTrendData.value, () => initTrendChart(), { deep: true })

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  barInstance?.dispose()
  groupInstance?.dispose()
  trendInstance?.dispose()
})

function handleResize() {
  barInstance?.resize()
  groupInstance?.resize()
  trendInstance?.resize()
}

function initBarChart() {
  barInstance = echarts.init(barChart.value)
  barInstance.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}: {c}%' },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '6%', containLabel: true },
    xAxis: {
      type: 'category',
      data: vizBarData.value.categories,
      axisLabel: { rotate: 30, fontSize: 12 }
    },
    yAxis: {
      type: 'value', min: 60, max: 100, name: '达成度(%)',
      axisLabel: { formatter: '{value}%' }
    },
    series: [{
      data: vizBarData.value.values.map(v => ({
        value: v,
        itemStyle: { color: v >= 85 ? '#22c55e' : v >= 75 ? '#3b82f6' : '#f97316' }
      })),
      type: 'bar', barWidth: '50%',
      label: { show: true, position: 'top', formatter: '{c}%', fontSize: 11 }
    }]
  })
}

function initGroupBarChart() {
  groupInstance = echarts.init(groupBarChart.value)
  groupInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, data: vizGroupBarData.value.series.map(s => s.name) },
    grid: { left: '3%', right: '4%', bottom: '14%', top: '6%', containLabel: true },
    xAxis: { type: 'category', data: vizGroupBarData.value.courses, axisLabel: { rotate: 15, fontSize: 11 } },
    yAxis: { type: 'value', min: 60, max: 100, axisLabel: { formatter: '{value}%' } },
    series: vizGroupBarData.value.series.map(s => ({
      name: s.name, type: 'bar', barMaxWidth: 20,
      itemStyle: { color: s.color },
      data: s.values
    }))
  })
}

function initTrendChart() {
  trendInstance = echarts.init(trendChart.value)
  trendInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, data: vizTrendData.value.series.map(s => s.name) },
    grid: { left: '3%', right: '4%', bottom: '14%', top: '6%', containLabel: true },
    xAxis: { type: 'category', data: vizTrendData.value.semesters, axisLabel: { rotate: 15, fontSize: 11 } },
    yAxis: { type: 'value', min: 70, max: 100, axisLabel: { formatter: '{value}%' } },
    series: vizTrendData.value.series.map(s => ({
      name: s.name, type: 'line', smooth: true,
      lineStyle: { color: s.color, width: 2 },
      itemStyle: { color: s.color },
      symbol: 'circle', symbolSize: 6,
      data: s.values
    }))
  })
}
</script>

<style scoped>
.d-flex-between { display: flex; justify-content: space-between; align-items: flex-start; }
.header-actions { display: flex; gap: 8px; }
.filter-bar { display: flex; align-items: center; gap: 24px; flex-wrap: wrap; }
.filter-group { display: flex; align-items: center; gap: 10px; }
.filter-label { font-size: 13px; color: #6b7280; white-space: nowrap; }
.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
.chart-card { }
.chart-area { height: 280px; width: 100%; }
</style>
