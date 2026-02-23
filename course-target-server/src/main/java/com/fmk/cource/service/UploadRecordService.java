package com.fmk.cource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fmk.cource.entity.UploadRecord;
import com.fmk.cource.vo.UploadRecordVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UploadRecordService extends IService<UploadRecord> {
    List<UploadRecordVO> listWithDetail(Long teacherId);
    UploadRecord importExcel(MultipartFile file, Long courseId, Long teacherId, String semester, String detailsJson) throws IOException;
    void audit(Long id, String status, String auditComment);

    void addRecord(UploadRecord record);
    void updateRecord(UploadRecord record);
    void deleteRecord(Long id);
}
