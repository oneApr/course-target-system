package com.fmk.cource.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fmk.cource.common.Result;
import com.fmk.cource.entity.CourseObjective;
import com.fmk.cource.service.CourseObjectiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import java.util.List;

@Tag(name = "课程目标管理")
@RestController
@RequestMapping(value = "/api/course-objective", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CourseObjectiveController {

    private final CourseObjectiveService courseObjectiveService;

    @Operation(summary = "按课程ID查询目标列表")
    @GetMapping
    public Result<List<CourseObjective>> listByCourse(@RequestParam Long courseId) {
        return Result.success(courseObjectiveService.list(
                new LambdaQueryWrapper<CourseObjective>()
                        .eq(CourseObjective::getCourseId, courseId)
                        .orderByAsc(CourseObjective::getTag)));
    }

    @Operation(summary = "新增课程目标")
    @PostMapping
    public Result<Void> add(@RequestBody CourseObjective obj) {
        courseObjectiveService.save(obj);
        return Result.success();
    }

    @Operation(summary = "修改课程目标")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CourseObjective obj) {
        obj.setId(id);
        courseObjectiveService.updateById(obj);
        return Result.success();
    }

    @Operation(summary = "删除课程目标")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseObjectiveService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "查询课程目标详情")
    @GetMapping("/{id}")
    public Result<CourseObjective> getById(@PathVariable Long id) {
        return Result.success(courseObjectiveService.getById(id));
    }
}
