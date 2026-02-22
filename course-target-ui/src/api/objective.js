import request from './request'

/** 课程目标列表（按课程ID）*/
export function getObjectivesByCourse(courseId) {
    return request.get('/api/course-objective', { params: { courseId } })
}

/** 新增课程目标 */
export function addObjective(data) {
    return request.post('/api/course-objective', data)
}

/** 修改课程目标 */
export function updateObjective(id, data) {
    return request.put(`/api/course-objective/${id}`, data)
}

/** 删除课程目标 */
export function deleteObjective(id) {
    return request.delete(`/api/course-objective/${id}`)
}
