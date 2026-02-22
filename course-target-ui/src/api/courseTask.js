import request from './request'

/** 课程任务列表（教师传 teacherId 只看自己的）*/
export function getCourseTaskList(params) {
    return request.get('/api/course-task', { params })
}

/** 新增课程任务 */
export function addCourseTask(data) {
    return request.post('/api/course-task', data)
}

/** 修改课程任务 */
export function updateCourseTask(id, data) {
    return request.put(`/api/course-task/${id}`, data)
}

/** 删除课程任务 */
export function deleteCourseTask(id) {
    return request.delete(`/api/course-task/${id}`)
}

/** 分配教师 */
export function assignTeacher(data) {
    return request.post('/api/task-assignment', data)
}

/** 取消分配 */
export function removeAssignment(id) {
    return request.delete(`/api/task-assignment/${id}`)
}
