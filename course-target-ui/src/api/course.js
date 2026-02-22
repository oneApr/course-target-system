import request from './request'

/** 课程列表（分页）*/
export function getCoursePage(params) {
    return request.get('/api/course', { params })
}

/** 所有课程（不分页，用于下拉）*/
export function getCourseAll() {
    return request.get('/api/course/all')
}

/** 新增课程 */
export function addCourse(data) {
    return request.post('/api/course', data)
}

/** 修改课程 */
export function updateCourse(id, data) {
    return request.put(`/api/course/${id}`, data)
}

/** 删除课程 */
export function deleteCourse(id) {
    return request.delete(`/api/course/${id}`)
}
