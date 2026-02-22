package com.fmk.cource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.cource.entity.*;
import com.fmk.cource.mapper.*;
import com.fmk.cource.service.CourseTaskService;
import com.fmk.cource.vo.AssignmentVO;
import com.fmk.cource.vo.CourseTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseTaskServiceImpl extends ServiceImpl<CourseTaskMapper, CourseTask>
        implements CourseTaskService {

    private final CourseMapper courseMapper;
    private final TaskAssignmentMapper assignmentMapper;
    private final TeacherMapper teacherMapper;

    @Override
    public List<CourseTaskVO> listWithDetail(Long teacherId) {
        List<CourseTask> tasks;
        if (teacherId != null) {
            // 教师：只查自己被分配的任务
            List<TaskAssignment> myAssigns = assignmentMapper.selectList(
                    new LambdaQueryWrapper<TaskAssignment>().eq(TaskAssignment::getTeacherId, teacherId));
            Set<Long> taskIds = myAssigns.stream().map(TaskAssignment::getTaskId).collect(Collectors.toSet());
            if (taskIds.isEmpty()) return Collections.emptyList();
            tasks = listByIds(taskIds);
        } else {
            tasks = list();
        }

        // 预加载 course 和 assignment
        if (tasks.isEmpty()) return Collections.emptyList();

        Set<Long> courseIds = tasks.stream().map(CourseTask::getCourseId).collect(Collectors.toSet());
        Map<Long, Course> courseMap = courseIds.isEmpty() ? Collections.emptyMap() :
                courseMapper.selectBatchIds(courseIds).stream()
                        .collect(Collectors.toMap(Course::getId, c -> c));

        Set<Long> taskIds2 = tasks.stream().map(CourseTask::getId).collect(Collectors.toSet());
        List<TaskAssignment> assignments = taskIds2.isEmpty() ? Collections.emptyList() :
                assignmentMapper.selectList(new LambdaQueryWrapper<TaskAssignment>()
                        .in(TaskAssignment::getTaskId, taskIds2));

        // 预加载 teacher 名字
        Set<Long> teacherIds = assignments.stream().map(TaskAssignment::getTeacherId).collect(Collectors.toSet());
        Map<Long, String> teacherNameMap = teacherIds.isEmpty() ? Collections.emptyMap() :
                teacherMapper.selectBatchIds(teacherIds).stream()
                        .collect(Collectors.toMap(Teacher::getId, Teacher::getName));

        Map<Long, List<TaskAssignment>> assignByTask = assignments.stream()
                .collect(Collectors.groupingBy(TaskAssignment::getTaskId));

        return tasks.stream().map(t -> {
            CourseTaskVO vo = new CourseTaskVO();
            BeanUtils.copyProperties(t, vo);
            Course course = courseMap.get(t.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getName());
                vo.setCourseCode(course.getCode());
            }
            List<TaskAssignment> aList = assignByTask.getOrDefault(t.getId(), Collections.emptyList());
            List<AssignmentVO> aVOs = aList.stream().map(a -> {
                AssignmentVO aVO = new AssignmentVO();
                BeanUtils.copyProperties(a, aVO);
                aVO.setTeacherName(teacherNameMap.getOrDefault(a.getTeacherId(), ""));
                return aVO;
            }).collect(Collectors.toList());
            vo.setAssignments(aVOs);
            return vo;
        }).collect(Collectors.toList());
    }
}
