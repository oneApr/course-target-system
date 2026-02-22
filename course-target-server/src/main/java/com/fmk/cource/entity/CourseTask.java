package com.fmk.cource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教学任务表 DO
 */
@Data
@TableName("course_task")
public class CourseTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String semester;
    private String status;
    private BigDecimal achievementRate;
    private Integer studentCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
