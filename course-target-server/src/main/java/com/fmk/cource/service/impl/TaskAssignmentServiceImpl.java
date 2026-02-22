package com.fmk.cource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.cource.entity.TaskAssignment;
import com.fmk.cource.mapper.TaskAssignmentMapper;
import com.fmk.cource.service.TaskAssignmentService;
import org.springframework.stereotype.Service;

@Service
public class TaskAssignmentServiceImpl extends ServiceImpl<TaskAssignmentMapper, TaskAssignment>
        implements TaskAssignmentService {
}
