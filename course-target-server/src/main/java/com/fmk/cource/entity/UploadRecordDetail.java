package com.fmk.cource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 达成度上传记录明细表 DO
 */
@Data
@TableName("upload_record_detail")
public class UploadRecordDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private Long objectiveId;
    private BigDecimal achievementRate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
