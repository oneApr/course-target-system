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
        return Result.success(uploadRecordService.getById(id));
    }

    @Operation(summary = "手动录入达成度数据")
    @PostMapping
    public Result<Void> add(@RequestBody UploadRecord record) {
        record.setStatus("待审核");
        uploadRecordService.save(record);
        return Result.success();
    }

    @Operation(summary = "修改达成度数据")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UploadRecord record) {
        record.setId(id);
        uploadRecordService.updateById(record);
        return Result.success();
    }

    @Operation(summary = "删除上传记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        uploadRecordService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "Excel 文件导入达成度数据")
    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<UploadRecord> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long courseId,
            @RequestParam Long teacherId,
            @RequestParam String semester) throws IOException {
        return Result.success(uploadRecordService.importExcel(file, courseId, teacherId, semester));
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
