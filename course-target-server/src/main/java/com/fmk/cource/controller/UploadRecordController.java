package com.fmk.cource.controller;

import com.fmk.cource.common.Result;
import com.fmk.cource.entity.UploadRecord;
import com.fmk.cource.service.UploadRecordService;
import com.fmk.cource.vo.UploadRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.util.List;

@Tag(name = "达成度上传审核")
@RestController
@RequestMapping(value = "/api/upload-record", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UploadRecordController {

    private final UploadRecordService uploadRecordService;

    @Operation(summary = "查询上传记录列表（teacherId=null则查全部）")
    @GetMapping
    public Result<List<UploadRecordVO>> list(
            @RequestParam(required = false) Long teacherId) {
        return Result.success(uploadRecordService.listWithDetail(teacherId));
    }

    @Operation(summary = "查询上传记录详情")
    @GetMapping("/{id}")
    public Result<UploadRecord> getById(@PathVariable Long id) {
        UploadRecord record = uploadRecordService.getById(id);
        if (record != null) {
            java.util.List<com.fmk.cource.entity.UploadRecordDetail> details = com.fmk.cource.util.SpringContextUtil.getBean(com.fmk.cource.service.UploadRecordDetailService.class)
                .list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.fmk.cource.entity.UploadRecordDetail>().eq(com.fmk.cource.entity.UploadRecordDetail::getRecordId, id));
            record.setDetails(details);
        }
        return Result.success(record);
    }

    @Operation(summary = "手动录入达成度数据")
    @PostMapping
    public Result<Void> add(@RequestBody UploadRecord record) {
        uploadRecordService.addRecord(record);
        return Result.success();
    }

    @Operation(summary = "修改达成度数据")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UploadRecord record) {
        record.setId(id);
        uploadRecordService.updateRecord(record);
        return Result.success();
    }

    @Operation(summary = "导入带附件的成绩（自动挂载达成度明细）")
    @PostMapping("/excel")
    public Result<UploadRecord> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Long courseId,
            @RequestParam("teacherId") Long teacherId,
            @RequestParam("semester") String semester,
            @RequestParam(value = "details", required = false) String detailsJson) throws IOException {
        return Result.success(uploadRecordService.importExcel(file, courseId, teacherId, semester, detailsJson));
    }

    @Operation(summary = "删除上传记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        uploadRecordService.deleteRecord(id);
        return Result.success();
    }

    @Operation(summary = "审核达成度数据（通过/驳回）")
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody AuditRequest req) {
        uploadRecordService.audit(id, req.getStatus(), req.getAuditComment());
        return Result.success();
    }

    @Data
    static class AuditRequest {
        private String status;
        private String auditComment;
    }
}
