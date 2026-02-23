<template>
  <div class="page-container">
    <div class="page-header d-flex-between">
      <div>
        <div class="page-title">数据可视化与达成度分析</div>
        <div class="page-subtitle">全面分析各个课程的教学目标达成详情及全系质量排名</div>
      </div>
    </div>

    <div class="section-card filter-bar">
      <div class="filter-group">
        <span class="filter-label">选择分析课程</span>
        <el-select v-model="selectedCourseId" style="width:260px" placeholder="请选择课程进行分析">
          <el-option v-for="c in store.courses" :key="c.id" :label="`${c.name} (${c.code})`" :value="c.id" />
        </el-select>
      </div>
    </div>

    <!-- KPI 卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card-info">
          <div class="stat-label">当前课程最新综合达成度</div>
          <div class="stat-value" :style="{color: getColor(kpi.currentOverall)}">{{ kpi.currentOverall }}%</div>
        </div>
        <div class="stat-card-icon icon-blue"><el-icon><TrendCharts /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info">
          <div class="stat-label">当前课程考核目标数</div>
          <div class="stat-value">{{ kpi.objCount }} 项</div>
        </div>
        <div class="stat-card-icon icon-purple"><el-icon><Aim /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info">
          <div class="stat-label">当前课程表现最佳目标</div>
          <div class="stat-value" style="font-size:20px">{{ kpi.bestObjTag || '-' }}</div>
        </div>
        <div class="stat-card-icon icon-green"><el-icon><Star /></el-icon></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-info">
          <div class="stat-label">最新全系达成度均值</div>
          <div class="stat-value" style="color:#64748b">{{ kpi.departmentAvg }}%</div>
        </div>
        <div class="stat-card-icon icon-orange"><el-icon><DataLine /></el-icon></div>
      </div>
    </div>

    <div class="charts-row">
      <!-- 雷达图：当前课程目标分布 -->
      <div class="section-card chart-card">
        <div class="section-title"><el-icon><Aim /></el-icon> 课程各目标达成度雷达图</div>
        <div v-if="hasRadarData" ref="radarChartRef" class="chart-area"></div>
        <div v-else class="empty-chart">暂无该课程的通过数据</div>
      </div>
      
      <!-- 柱状图：全系课程达成度排行榜 -->
      <div class="section-card chart-card">
        <div class="section-title"><el-icon><TrendCharts /></el-icon> 全系课程最新达成度排行榜</div>
        <div v-if="hasRankData" ref="rankChartRef" class="chart-area"></div>
        <div v-else class="empty-chart">暂无全系通过数据</div>
      </div>
    </div>

    <!-- 折线图：当前课程历史趋势 -->
    <div class="section-card" style="margin-top:16px">
      <div class="section-title"><el-icon><DataLine /></el-icon> {{ currentCourseName }} - 历史多学期达成度趋势</div>
      <div v-if="hasTrendData" ref="trendChartRef" style="height:340px;width:100%"></div>
      <div v-else class="empty-chart" style="height:340px">暂无该课程的历史记录</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { TrendCharts, Aim, DataLine, Star } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { useCourseStore } from '@/stores/courses'

const store = useCourseStore()

const selectedCourseId = ref(null)

const radarChartRef = ref(null)
const rankChartRef = ref(null)
const trendChartRef = ref(null)

let radarInstance = null
let rankInstance = null
let trendInstance = null

onMounted(async () => {
  await store.fetchCourses()
  await store.fetchUploadRecords() // 获取全系所有教师的上传记录
  
  if (store.courses.length > 0) {
    selectedCourseId.value = store.courses[0].id
    await store.fetchObjectives(selectedCourseId.value)
  }
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  radarInstance?.dispose()
  rankInstance?.dispose()
  trendInstance?.dispose()
})

function handleResize() {
  radarInstance?.resize()
  rankInstance?.resize()
  trendInstance?.resize()
}

// 统一过滤状态为已通过的记录
const passedRecords = computed(() => {
  return store.uploadRecords.filter(r => r.status === '已通过' || r.status === '已审核')
})

// 计算单条记录的综合达成度
function calcOverall(record) {
  if (!record.details || record.details.length === 0) return 0
  const sum = record.details.reduce((acc, d) => acc + (d.achievementRate || 0), 0)
  return Number((sum / record.details.length).toFixed(1))
}

const currentCourseName = computed(() => {
  const c = store.courses.find(c => c.id === selectedCourseId.value)
  return c ? c.name : '-'
})

// KPI 统计
const currentCourseRecords = computed(() => {
  if (!selectedCourseId.value) return []
  // 按学期由于是字符串格式可能不同，按 id 倒序视为最新
  return passedRecords.value.filter(r => r.courseId === selectedCourseId.value).sort((a,b) => b.id - a.id)
})

const kpi = computed(() => {
  let currentOverall = 0
  let objCount = 0
  let bestObjTag = '-'
  let departmentAvg = 0

  const latest = currentCourseRecords.value[0]
  if (latest) {
    currentOverall = calcOverall(latest)
    objCount = latest.details?.length || 0
    if (objCount > 0) {
      const bestDetail = [...latest.details].sort((a,b) => b.achievementRate - a.achievementRate)[0]
      const obj = store.objectives.find(o => o.id === bestDetail.objectiveId)
      bestObjTag = obj ? obj.tag : `目标ID:${bestDetail.objectiveId}`
    }
  }

  // 计算全系的均值（取各课程最新一次通过的平均值）
  const courseMap = new Map()
  passedRecords.value.forEach(r => {
    if (!courseMap.has(r.courseId) || r.id > courseMap.get(r.courseId).id) {
      courseMap.set(r.courseId, r)
    }
  })
  if (courseMap.size > 0) {
    const sum = Array.from(courseMap.values()).reduce((acc, r) => acc + calcOverall(r), 0)
    departmentAvg = Number((sum / courseMap.size).toFixed(1))
  }

  return { currentOverall, objCount, bestObjTag, departmentAvg }
})

function getColor(v) {
  if (!v) return '#94a3b8'
  return v >= 85 ? '#22c55e' : v >= 75 ? '#3b82f6' : v >= 60 ? '#f59e0b' : '#ef4444'
}

// 图表控制 

watch(selectedCourseId, async (newVal) => {
  if (newVal) {
    await store.fetchObjectives(newVal)
  }
  nextTick(() => {
    renderRadar()
    renderRank()
    renderTrend()
  })
})
watch(() => store.uploadRecords, () => {
  if(selectedCourseId.value) {
    nextTick(() => {
      renderRadar()
      renderRank()
      renderTrend()
    })
  }
}, { deep: true })

// 辅助判定渲染容器
const hasRadarData = computed(() => currentCourseRecords.value.length > 0)
const hasTrendData = computed(() => currentCourseRecords.value.length > 0)
const hasRankData = computed(() => passedRecords.value.length > 0)

// 1. 雷达图：当前课程各类目标最新达成度
function renderRadar() {
  if (!hasRadarData.value || !radarChartRef.value) return
  if (!radarInstance) radarInstance = echarts.init(radarChartRef.value)

  const latest = currentCourseRecords.value[0]
  const indicator = []
  const dataValues = []
  
  latest.details.forEach(d => {
    const obj = store.objectives.find(o => o.id === d.objectiveId)
    indicator.push({ name: obj ? obj.tag : '未知目标', max: 100 })
    dataValues.push(d.achievementRate || 0)
  })

  // ECharts requires at least 3 indicators for a proper radar naturally, 
  // but it works with less, just looks weird. We adapt.
  radarInstance.setOption({
    tooltip: { trigger: 'item' },
    radar: {
      indicator: indicator.length > 0 ? indicator : [{name:'暂无参数', max:100}],
      radius: '65%',
      axisName: { color: '#475569', fontSize: 13, fontWeight: 'bold' },
      splitArea: { areaStyle: { color: ['#f8fafc', '#f1f5f9', '#e2e8f0', '#cbd5e1'] } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: dataValues,
        name: '达成度',
        areaStyle: { color: 'rgba(59, 130, 246, 0.3)' },
        lineStyle: { color: '#3b82f6', width: 2 },
        itemStyle: { color: '#3b82f6' }
      }]
    }]
  })
}

// 2. 柱状图：全系课程排行榜
function renderRank() {
  if (!hasRankData.value || !rankChartRef.value) return
  if (!rankInstance) rankInstance = echarts.init(rankChartRef.value)

  const courseMap = new Map()
  passedRecords.value.forEach(r => {
    if (!courseMap.has(r.courseId) || r.id > courseMap.get(r.courseId).id) {
      courseMap.set(r.courseId, r)
    }
  })

  const rankList = Array.from(courseMap.values()).map(r => ({
    name: r.courseName || store.courses.find(c=>c.id===r.courseId)?.name || '未知',
    value: calcOverall(r)
  })).sort((a,b) => a.value - b.value) // 升序，为了 ECharts 的横向条形图从上到下是降序

  rankInstance.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}: {c}%' },
    grid: { left: '3%', right: '6%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { type: 'value', max: 100, splitLine: { lineStyle: { type: 'dashed' } } },
    yAxis: { type: 'category', data: rankList.map(i => i.name), axisLabel: { fontWeight: 'bold' } },
    series: [{
      type: 'bar',
      data: rankList.map(i => i.value),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#3b82f6' },
          { offset: 1, color: '#93c5fd' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      label: { show: true, position: 'right', formatter: '{c}%', textStyle: { color: '#1e293b', fontWeight: 'bold' } }
    }]
  })
}

// 3. 折线图：当前课程历史多学期趋势
function renderTrend() {
  if (!hasTrendData.value || !trendChartRef.value) return
  if (!trendInstance) trendInstance = echarts.init(trendChartRef.value)

  // 拿出该课程的所有记录，按 ID 升序（模拟时间轴正序）
  const history = [...currentCourseRecords.value].reverse()
  const xData = history.map(r => r.semester)
  const yData = history.map(r => calcOverall(r))

  trendInstance.setOption({
    tooltip: { trigger: 'axis', formatter: '{b} <br/>综合达成度: {c}%' },
    grid: { left: '3%', right: '4%', bottom: '8%', top: '10%', containLabel: true },
    xAxis: { 
      type: 'category', boundaryGap: false, data: xData,
      axisLine: { lineStyle: { color: '#94a3b8' } },
      axisLabel: { color: '#475569', fontWeight: 'bold' }
    },
    yAxis: { 
      type: 'value', min: 0, max: 100,
      splitLine: { lineStyle: { type: 'dashed', color: '#e2e8f0' } }
    },
    series: [{
      type: 'line',
      data: yData,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: { color: '#10b981' },
      lineStyle: { width: 4, color: '#10b981' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(16, 185, 129, 0.4)' },
          { offset: 1, color: 'rgba(16, 185, 129, 0.0)' }
        ])
      },
      label: { show: true, position: 'top', formatter: '{c}%', fontSize: 12, fontWeight: 'bold' }
    }]
  })
}

</script>

<style scoped>
.page-container {
  padding: 24px;
  background-color: #f8fafc;
  min-height: 100%;
}
.d-flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}
.page-subtitle {
  font-size: 14px;
  color: #64748b;
}
.filter-bar {
  display: flex;
  gap: 24px;
  align-items: center;
  margin-bottom: 24px;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}
.filter-label {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}
.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -1px rgba(0,0,0,0.06);
}
.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
}
.stat-card-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}
.icon-blue { background: #eff6ff; color: #3b82f6; }
.icon-purple { background: #f3e8ff; color: #a855f7; }
.icon-green { background: #f0fdf4; color: #22c55e; }
.icon-orange { background: #fff7ed; color: #f97316; }

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.section-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.chart-area {
  height: 340px;
  width: 100%;
}
.empty-chart {
  height: 340px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 14px;
  background-color: #f8fafc;
  border-radius: 8px;
  border: 1px dashed #cbd5e1;
}
</style>
