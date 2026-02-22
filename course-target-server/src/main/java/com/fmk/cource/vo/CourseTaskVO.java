package com.fmk.cource.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 课程任务 VO（含课程名、分配列表等）
 */
@Data
public class CourseTaskVO {
    private Long id;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private String semester;
    private String status;
    private BigDecimal achievementRate;
    private Integer studentCount;
    private List<AssignmentVO> assignments;
}
