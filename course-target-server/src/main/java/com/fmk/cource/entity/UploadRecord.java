package com.fmk.cource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 达成度上传记录表 DO
 */
@Data
@TableName("upload_record")
public class UploadRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long teacherId;
    private String semester;
    private String status;
    private String auditComment;
    private String filePath;

    @TableField(exist = false)
    private java.util.List<UploadRecordDetail> details;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
