package com.fmk.cource.controller;

import com.fmk.cource.common.Result;
import com.fmk.cource.entity.CourseTask;
import com.fmk.cource.service.CourseTaskService;
import com.fmk.cource.vo.CourseTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import java.util.List;

@Tag(name = "教学任务管理")
@RestController
@RequestMapping(value = "/api/course-task", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CourseTaskController {

    private final CourseTaskService courseTaskService;

    @Operation(summary = "查询任务列表（含分配详情，教师按teacherId过滤）")
    @GetMapping
    public Result<List<CourseTaskVO>> list(
            @RequestParam(required = false) Long teacherId) {
        return Result.success(courseTaskService.listWithDetail(teacherId));
    }

    @Operation(summary = "新增教学任务")
    @PostMapping
    public Result<Void> add(@RequestBody CourseTask task) {
        courseTaskService.save(task);
        return Result.success();
    }

    @Operation(summary = "修改教学任务")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CourseTask task) {
        task.setId(id);
        courseTaskService.updateById(task);
        return Result.success();
    }

    @Operation(summary = "删除教学任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseTaskService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "查询任务详情")
    @GetMapping("/{id}")
    public Result<CourseTask> getById(@PathVariable Long id) {
        return Result.success(courseTaskService.getById(id));
    }
}
