package com.fmk.cource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.cource.entity.TaskAssignment;
import com.fmk.cource.mapper.TaskAssignmentMapper;
import com.fmk.cource.service.TaskAssignmentService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fmk.cource.entity.CourseTask;
import com.fmk.cource.mapper.CourseTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Service
public class TaskAssignmentServiceImpl extends ServiceImpl<TaskAssignmentMapper, TaskAssignment>
        implements TaskAssignmentService {

    @Autowired
    private CourseTaskMapper courseTaskMapper;

    @Override
    public boolean save(TaskAssignment entity) {
        entity.setAssignStatus("已分配"); // 默认状态改为已分配
        boolean r = super.save(entity);
        syncTaskData(entity.getTaskId());
        return r;
    }

    @Override
    public boolean saveBatch(Collection<TaskAssignment> entityList, int batchSize) {
        entityList.forEach(e -> e.setAssignStatus("已分配"));
        boolean r = super.saveBatch(entityList, batchSize);
        if (!entityList.isEmpty()) {
            syncTaskData(entityList.iterator().next().getTaskId());
        }
        return r;
    }

    @Override
    public boolean updateById(TaskAssignment entity) {
        boolean r = super.updateById(entity);
        TaskAssignment curr = getById(entity.getId());
        if (curr != null) {
            syncTaskData(curr.getTaskId());
        }
        return r;
    }

    @Override
    public boolean removeById(Serializable id) {
        TaskAssignment curr = getById(id);
        boolean r = super.removeById(id);
        if (curr != null) {
            syncTaskData(curr.getTaskId());
        }
        return r;
    }

    private void syncTaskData(Long taskId) {
        if (taskId == null) return;
        List<TaskAssignment> list = list(new LambdaQueryWrapper<TaskAssignment>()
                .eq(TaskAssignment::getTaskId, taskId));
        int total = list.stream().mapToInt(a -> a.getStudentCount() == null ? 0 : a.getStudentCount()).sum();
        
        String status = "未分配";
        if (!list.isEmpty()) {
            boolean hasAssigned = list.stream().anyMatch(a -> "已分配".equals(a.getAssignStatus()));
            status = hasAssigned ? "已分配" : "未分配";
        }

        CourseTask bt = courseTaskMapper.selectById(taskId);
        if (bt != null) {
            bt.setStudentCount(total);
            bt.setStatus(status);
            courseTaskMapper.updateById(bt);
        }
    }
}
