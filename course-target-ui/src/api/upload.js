import request from './request'

/** 上传记录列表（教师传 teacherId 只看自己的）*/
export function getUploadList(params) {
    return request.get('/api/upload-record', { params })
}

/** 上传 Excel 文件 */
export function uploadExcel(formData) {
    return request.post('/api/upload-record/excel', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}
/** 手动填写达成度 */
export function addUploadRecord(data) {
    return request.post('/api/upload-record', data)
}

/** 重新修改填写达成度 */
export function updateUploadRecord(id, data) {
    return request.put(`/api/upload-record/${id}`, data)
}

/** 审核（系主任）*/
export function auditRecord(id, data) {
    return request.put(`/api/upload-record/${id}/audit`, data)
}

/** 删除记录 */
export function deleteRecord(id) {
    return request.delete(`/api/upload-record/${id}`)
}
