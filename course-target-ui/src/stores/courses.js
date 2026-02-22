import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCourseAll, addCourse as apiAddCourse, updateCourse as apiUpdateCourse, deleteCourse as apiDeleteCourse } from '@/api/course'
import { getTeacherAll, addTeacher as apiAddTeacher, updateTeacher as apiUpdateTeacher, deleteTeacher as apiDeleteTeacher } from '@/api/teacher'
import { getObjectivesByCourse, addObjective as apiAddObjective, updateObjective as apiUpdateObjective, deleteObjective as apiDeleteObjective } from '@/api/objective'
import { getCourseTaskList, addCourseTask as apiAddTask, updateCourseTask as apiUpdateTask, deleteCourseTask as apiDeleteTask } from '@/api/courseTask'
import { getUploadList } from '@/api/upload'

export const useCourseStore = defineStore('courses', () => {
    const courses = ref([])
    const objectives = ref([])
    const courseTasks = ref([])
    const uploadRecords = ref([])
    const queryResults = ref([])
    const selectedCourseId = ref(null)
    const loading = ref(false)
    const tagColorMap = {
        '目标一': { bg: '#dbeafe', text: '#2563eb' },
        '目标二': { bg: '#dcfce7', text: '#16a34a' },
        '目标三': { bg: '#f3e8ff', text: '#9333ea' },
        '目标四': { bg: '#fef3c7', text: '#d97706' },
    }

    const selectedCourse = computed(() => courses.value.find(c => c.id === selectedCourseId.value))
    const selectedObjectives = computed(() => objectives.value.filter(o => o.courseId === selectedCourseId.value))

    // 课程
    async function fetchCourses() {
        try {
            const res = await getCourseAll()
            courses.value = res.data || []
            if (!selectedCourseId.value && courses.value.length) {
                selectedCourseId.value = courses.value[0].id
            }
        } catch (e) { console.error('获取课程失败', e) }
    }

    async function addCourse(course) {
        const res = await apiAddCourse(course)
        await fetchCourses()
        return res.data?.id
    }
    async function updateCourse(id, data) {
        await apiUpdateCourse(id, data)
        await fetchCourses()
    }
    async function deleteCourse(id) {
        await apiDeleteCourse(id)
        await fetchCourses()
    }

    async function fetchObjectives(courseId) {
        if (!courseId) return
        try {
            const res = await getObjectivesByCourse(courseId)
            objectives.value = (res.data || []).map(o => {
                const colors = tagColorMap[o.tag] || { bg: '#e5e7eb', text: '#374151' }
                return { ...o, tagColor: o.tagColor || colors.bg, tagTextColor: o.tagTextColor || colors.text }
            })
        } catch (e) { console.error('获取目标失败', e) }
    }

    async function addObjective(obj) {
        const colors = tagColorMap[obj.tag] || { bg: '#e5e7eb', text: '#374151' }
        await apiAddObjective({ ...obj, tagColor: colors.bg, tagTextColor: colors.text })
        await fetchObjectives(obj.courseId)
    }
    async function updateObjective(id, data) {
        await apiUpdateObjective(id, data)
        await fetchObjectives(data.courseId || selectedCourseId.value)
    }
    async function deleteObjective(id) {
        await apiDeleteObjective(id)
        await fetchObjectives(selectedCourseId.value)
    }

    // 课程任务 
    async function fetchCourseTasks(teacherId) {
        try {
            const res = await getCourseTaskList(teacherId ? { teacherId } : {})
            courseTasks.value = res.data || []
        } catch (e) { console.error('获取任务失败', e) }
    }

    async function addCourseTask(task) {
        await apiAddTask(task)
        await fetchCourseTasks()
    }
    async function updateCourseTask(id, data) {
        await apiUpdateTask(id, data)
        await fetchCourseTasks()
    }
    async function deleteCourseTask(id) {
        await apiDeleteTask(id)
        await fetchCourseTasks()
    }

    // 上传记录 
    async function fetchUploadRecords(teacherId) {
        try {
            const res = await getUploadList(teacherId ? { teacherId } : {})
            uploadRecords.value = res.data || []
        } catch (e) { console.error('获取上传记录失败', e) }
    }
    function updateUploadStatus(id, status) {
        const r = uploadRecords.value.find(r => r.id === id)
        if (r) r.status = status
    }

    return {
        courses, objectives, courseTasks, uploadRecords, queryResults,
        selectedCourseId, selectedCourse, selectedObjectives, loading, tagColorMap,
        fetchCourses, addCourse, updateCourse, deleteCourse,
        fetchObjectives, addObjective, updateObjective, deleteObjective,
        fetchCourseTasks, addCourseTask, updateCourseTask, deleteCourseTask,
        fetchUploadRecords, updateUploadStatus,
    }
})
export const useTeacherStore = defineStore('teachers', () => {
    const teachers = ref([])

    async function fetchTeachers() {
        try {
            const res = await getTeacherAll()
            teachers.value = res.data || []
        } catch (e) { console.error('获取教师失败', e) }
    }

    async function addTeacher(t) {
        await apiAddTeacher(t)
        await fetchTeachers()
    }
    async function updateTeacher(id, data) {
        await apiUpdateTeacher(id, data)
        await fetchTeachers()
    }
    async function deleteTeacher(id) {
        await apiDeleteTeacher(id)
        await fetchTeachers()
    }

    return { teachers, fetchTeachers, addTeacher, updateTeacher, deleteTeacher }
})
