package com.fmk.cource.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fmk.cource.common.Result;
import com.fmk.cource.entity.TaskAssignment;
import com.fmk.cource.service.TaskAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import java.util.List;

@Tag(name = "任务分配管理")
@RestController
@RequestMapping(value = "/api/task-assignment", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TaskAssignmentController {

    private final TaskAssignmentService taskAssignmentService;

    @Operation(summary = "按任务ID查询分配列表")
    @GetMapping
    public Result<List<TaskAssignment>> listByTask(@RequestParam Long taskId) {
        return Result.success(taskAssignmentService.list(
                new LambdaQueryWrapper<TaskAssignment>()
                        .eq(TaskAssignment::getTaskId, taskId)));
    }

    @Operation(summary = "新增任务分配")
    @PostMapping
    public Result<Void> add(@RequestBody TaskAssignment assignment) {
        taskAssignmentService.save(assignment);
        return Result.success();
    }

    @Operation(summary = "修改任务分配")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody TaskAssignment assignment) {
        assignment.setId(id);
        taskAssignmentService.updateById(assignment);
        return Result.success();
    }

    @Operation(summary = "删除任务分配")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskAssignmentService.removeById(id);
        return Result.success();
    }
}
