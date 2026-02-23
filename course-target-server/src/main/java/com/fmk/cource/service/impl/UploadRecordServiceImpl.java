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
import com.fmk.cource.service.UploadRecordDetailService;
import com.fmk.cource.util.FileUploadUtil;
import com.fmk.cource.vo.UploadRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadRecordServiceImpl extends ServiceImpl<UploadRecordMapper, UploadRecord>
        implements UploadRecordService {

    private final CourseMapper courseMapper;
    private final TeacherMapper teacherMapper;
    private final FileUploadUtil fileUploadUtil;
    private final UploadRecordDetailService detailService;
    private final CourseObjectiveMapper courseObjectiveMapper;
    private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    public List<UploadRecordVO> listWithDetail(Long teacherId) {
        List<UploadRecord> records;
        if (teacherId != null) {
            records = list(new LambdaQueryWrapper<UploadRecord>()
                    .eq(UploadRecord::getTeacherId, teacherId).orderByDesc(UploadRecord::getUploadTime));
        } else {
            records = list(new LambdaQueryWrapper<UploadRecord>().orderByDesc(UploadRecord::getUploadTime));
        }

        if (records.isEmpty()) return Collections.emptyList();

        Set<Long> courseIds = records.stream().map(UploadRecord::getCourseId).collect(Collectors.toSet());
        Set<Long> teacherIds = records.stream().map(UploadRecord::getTeacherId).collect(Collectors.toSet());
        List<Long> recordIds = records.stream().map(UploadRecord::getId).collect(Collectors.toList());

        Map<Long, String> courseNameMap = courseIds.isEmpty() ? Collections.emptyMap() :
                courseMapper.selectBatchIds(courseIds).stream()
                        .collect(Collectors.toMap(Course::getId, Course::getName));

        Map<Long, String> teacherNameMap = teacherIds.isEmpty() ? Collections.emptyMap() :
                teacherMapper.selectBatchIds(teacherIds).stream()
                        .collect(Collectors.toMap(Teacher::getId, Teacher::getName));

        List<UploadRecordDetail> allDetails = detailService.list(
                new LambdaQueryWrapper<UploadRecordDetail>().in(UploadRecordDetail::getRecordId, recordIds)
        );
        Map<Long, List<UploadRecordDetail>> detailMap = allDetails.stream().collect(Collectors.groupingBy(UploadRecordDetail::getRecordId));

        return records.stream().map(r -> {
            UploadRecordVO vo = new UploadRecordVO();
            BeanUtils.copyProperties(r, vo);
            vo.setCourseName(courseNameMap.getOrDefault(r.getCourseId(), ""));
            vo.setTeacherName(teacherNameMap.getOrDefault(r.getTeacherId(), ""));
            if (r.getUploadTime() != null) {
                vo.setUploadTime(r.getUploadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            vo.setDetails(detailMap.getOrDefault(r.getId(), new ArrayList<>()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadRecord importExcel(MultipartFile file, Long courseId, Long teacherId, String semester, String detailsJson) throws IOException {
        String filePath = fileUploadUtil.upload(file);

        UploadRecord record = new UploadRecord();
        record.setCourseId(courseId);
        record.setTeacherId(teacherId);
        record.setSemester(semester);
        record.setStatus("待审核");
        record.setFilePath(filePath);
        save(record);

        List<UploadRecordDetail> details = new ArrayList<>();
        if (detailsJson != null && !detailsJson.trim().isEmpty()) {
            // 前端传了填写的全部数据，直接解析
            try {
                details = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        detailsJson, new com.fasterxml.jackson.core.type.TypeReference<List<UploadRecordDetail>>() {}
                );
                if (details != null && !details.isEmpty()) {
                    for (UploadRecordDetail d : details) {
                        d.setRecordId(record.getId());
                    }
                    detailService.saveBatch(details);
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.error("解析 detailsJson 失败: {}", e.getMessage());
            }
        } else {
            // 如果没填数据全靠上传的 Excel 包含数据 
            List<CourseObjective> objectives = courseObjectiveMapper.selectList(
                    new LambdaQueryWrapper<CourseObjective>().eq(CourseObjective::getCourseId, courseId).orderByAsc(CourseObjective::getId)
            );
            if (!objectives.isEmpty()) {
                List<BigDecimal> parsedRates = new ArrayList<>();
                EasyExcel.read(filePath, new AnalysisEventListener<Map<Integer, String>>() {
                    @Override
                    public void invoke(Map<Integer, String> data, AnalysisContext context) {
                        for (int i = 0; i < objectives.size(); i++) {
                            try {
                                String val = data.get(i);
                                if (val != null && !val.trim().isEmpty()) {
                                    parsedRates.add(new BigDecimal(val.trim()));
                                } else {
                                    parsedRates.add(null);
                                }
                            } catch (Exception e) {
                                parsedRates.add(null);
                            }
                        }
                    }
                    @Override
                    public void doAfterAllAnalysed(AnalysisContext context) {}
                }).headRowNumber(1).sheet().doRead();

                for (int i = 0; i < objectives.size(); i++) {
                    if (i < parsedRates.size() && parsedRates.get(i) != null) {
                        UploadRecordDetail d = new UploadRecordDetail();
                        d.setRecordId(record.getId());
                        d.setObjectiveId(objectives.get(i).getId());
                        d.setAchievementRate(parsedRates.get(i));
                        details.add(d);
                    }
                }
                if (!details.isEmpty()) {
                    detailService.saveBatch(details);
                }
            }
        }

        record.setDetails(details);
        
        Course course = courseMapper.selectById(courseId);
        String cName = course != null ? course.getName() : "未知课程";
        logActivity("上传了\"" + cName + "\"课程达成数据", "upload");
        
        return record;
    }

    @Override
    public void audit(Long id, String status, String auditComment) {
        UploadRecord record = getById(id);
        if (record == null) throw new BusinessException(404, "记录不存在");
        record.setStatus(status);
        record.setAuditComment(auditComment);
        updateById(record);
        
        Course course = courseMapper.selectById(record.getCourseId());
        String cName = course != null ? course.getName() : "未知课程";
        logActivity("审核了\"" + cName + "\"的数据(" + status + ")", "check");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRecord(UploadRecord record) {
        record.setStatus("待审核");
        save(record);
        if (record.getDetails() != null && !record.getDetails().isEmpty()) {
            for (UploadRecordDetail d : record.getDetails()) {
                d.setRecordId(record.getId());
            }
            detailService.saveBatch(record.getDetails());
        }
        
        Course course = courseMapper.selectById(record.getCourseId());
        String cName = course != null ? course.getName() : "未知课程";
        logActivity("上传了\"" + cName + "\"课程达成数据", "upload");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRecord(UploadRecord record) {
        updateById(record);
        if (record.getDetails() != null) {
            detailService.remove(new LambdaQueryWrapper<UploadRecordDetail>().eq(UploadRecordDetail::getRecordId, record.getId()));
            if (!record.getDetails().isEmpty()) {
                for (UploadRecordDetail d : record.getDetails()) {
                    d.setRecordId(record.getId());
                }
                detailService.saveBatch(record.getDetails());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long id) {
        detailService.remove(new LambdaQueryWrapper<UploadRecordDetail>().eq(UploadRecordDetail::getRecordId, id));
        removeById(id);
    }

    private void logActivity(String text, String type) {
        Long userId = com.fmk.cource.security.SecurityUtils.getUserId();
        if (userId == null) return;
        try {
            String key = "activity:logs:user:" + userId;
            java.util.Map<String, String> logEntry = new java.util.HashMap<>();
            logEntry.put("text", text);
            logEntry.put("type", type);
            logEntry.put("time", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stringRedisTemplate.opsForList().leftPush(key, objectMapper.writeValueAsString(logEntry));
            stringRedisTemplate.opsForList().trim(key, 0, 9);
            stringRedisTemplate.expire(key, 30, java.util.concurrent.TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Failed to log activity to Redis", e);
        }
    }
}
