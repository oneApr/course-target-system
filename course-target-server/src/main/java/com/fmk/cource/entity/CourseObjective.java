package com.fmk.cource.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程目标表 DO
 */
@Data
@TableName(value = "course_objective", autoResultMap = true)
public class CourseObjective {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String tag;
    private String description;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> requirements;
    private String indicator;
    private String weight;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
