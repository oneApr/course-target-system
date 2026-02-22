import request from './request'

/** 教师列表（分页）*/
export function getTeacherPage(params) {
    return request.get('/api/teacher', { params })
}

/** 所有教师（不分页，用于下拉）*/
export function getTeacherAll() {
    return request.get('/api/teacher/all')
}

/** 新增教师 */
export function addTeacher(data) {
    return request.post('/api/teacher', data)
}

/** 修改教师 */
export function updateTeacher(id, data) {
    return request.put(`/api/teacher/${id}`, data)
}

/** 删除教师 */
export function deleteTeacher(id) {
    return request.delete(`/api/teacher/${id}`)
}
