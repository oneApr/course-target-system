package com.fmk.cource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务分配表 DO
 */
@Data
@TableName("task_assignment")
public class TaskAssignment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long teacherId;
    private String grade;
    private String classes;
    private Integer studentCount;
    private String assignStatus;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
