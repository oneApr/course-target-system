package com.fmk.cource.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.cource.common.BusinessException;
import com.fmk.cource.entity.*;
import com.fmk.cource.mapper.*;
import com.fmk.cource.service.UploadRecordService;
import com.fmk.cource.util.FileUploadUtil;
import com.fmk.cource.vo.UploadRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadRecordServiceImpl extends ServiceImpl<UploadRecordMapper, UploadRecord>
        implements UploadRecordService {

    private final CourseMapper courseMapper;
    private final TeacherMapper teacherMapper;
    private final FileUploadUtil fileUploadUtil;

    @Override
    public List<UploadRecordVO> listWithDetail(Long teacherId) {
        List<UploadRecord> records;
        if (teacherId != null) {
            records = list(new LambdaQueryWrapper<UploadRecord>()
                    .eq(UploadRecord::getTeacherId, teacherId));
        } else {
            records = list();
        }

        Set<Long> courseIds = records.stream().map(UploadRecord::getCourseId).collect(Collectors.toSet());
        Set<Long> teacherIds = records.stream().map(UploadRecord::getTeacherId).collect(Collectors.toSet());

        Map<Long, String> courseNameMap = courseIds.isEmpty() ? Collections.emptyMap() :
                courseMapper.selectBatchIds(courseIds).stream()
                        .collect(Collectors.toMap(Course::getId, Course::getName));

        Map<Long, String> teacherNameMap = teacherIds.isEmpty() ? Collections.emptyMap() :
                teacherMapper.selectBatchIds(teacherIds).stream()
                        .collect(Collectors.toMap(Teacher::getId, Teacher::getName));

        return records.stream().map(r -> {
            UploadRecordVO vo = new UploadRecordVO();
            BeanUtils.copyProperties(r, vo);
            vo.setCourseName(courseNameMap.getOrDefault(r.getCourseId(), ""));
            vo.setTeacherName(teacherNameMap.getOrDefault(r.getTeacherId(), ""));
            if (r.getUploadTime() != null) {
                vo.setUploadTime(r.getUploadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public UploadRecord importExcel(MultipartFile file, Long courseId, Long teacherId, String semester) throws IOException {
        // 保存文件
        String filePath = fileUploadUtil.upload(file);

        // 解析 Excel 数据（第一行表头，第二行数据）
        AtomicReference<BigDecimal> obj1 = new AtomicReference<>();
        AtomicReference<BigDecimal> obj2 = new AtomicReference<>();
        AtomicReference<BigDecimal> obj3 = new AtomicReference<>();

        EasyExcel.read(filePath, new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                try {
                    if (data.get(0) != null) obj1.set(new BigDecimal(data.get(0).trim()));
                    if (data.get(1) != null) obj2.set(new BigDecimal(data.get(1).trim()));
                    if (data.get(2) != null) obj3.set(new BigDecimal(data.get(2).trim()));
                } catch (NumberFormatException e) {
                    log.warn("Excel 数据格式错误: {}", data);
                }
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).headRowNumber(1).sheet().doRead();

        UploadRecord record = new UploadRecord();
        record.setCourseId(courseId);
        record.setTeacherId(teacherId);
        record.setSemester(semester);
        record.setObjective1(obj1.get());
        record.setObjective2(obj2.get());
        record.setObjective3(obj3.get());
        record.setStatus("待审核");
        record.setFilePath(filePath);
        save(record);
        return record;
    }

    @Override
    public void audit(Long id, String status, String auditComment) {
        UploadRecord record = getById(id);
        if (record == null) throw new BusinessException(404, "记录不存在");
        record.setStatus(status);
        record.setAuditComment(auditComment);
        updateById(record);
    }
}
