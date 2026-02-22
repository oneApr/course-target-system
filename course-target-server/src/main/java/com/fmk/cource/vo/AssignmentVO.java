package com.fmk.cource.vo;

import lombok.Data;

/**
 * 任务分配 VO
 */
@Data
public class AssignmentVO {
    private Long id;
    private Long taskId;
    private Long teacherId;
    private String teacherName;
    private String grade;
    private String classes;
    private Integer studentCount;
    private String assignStatus;
}
