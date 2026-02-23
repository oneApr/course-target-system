package com.fmk.cource.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 上传记录 VO（含课程名、教师名等冗余字段）
 */
@Data
public class UploadRecordVO {
    private Long id;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private String semester;
    private java.util.List<com.fmk.cource.entity.UploadRecordDetail> details;
    private String status;
    private String auditComment;
    private String filePath;
    private String uploadTime;
}
