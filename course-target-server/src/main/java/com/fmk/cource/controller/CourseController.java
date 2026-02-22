package com.fmk.cource.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fmk.cource.common.Result;
import com.fmk.cource.entity.Course;
import com.fmk.cource.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

@Tag(name = "课程管理")
@RestController
@RequestMapping(value = "/api/course", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "分页查询课程列表")
    @GetMapping
    public Result<Page<Course>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String dept,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) wrapper.like(Course::getName, name);
        if (StringUtils.hasText(dept)) wrapper.eq(Course::getDept, dept);
        if (StringUtils.hasText(status)) wrapper.eq(Course::getStatus, status);
        wrapper.orderByDesc(Course::getCreateTime);
        return Result.success(courseService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "查询全部课程（用于下拉框）")
    @GetMapping("/all")
    public Result<List<Course>> listAll() {
        return Result.success(courseService.list(
                new LambdaQueryWrapper<Course>().orderByAsc(Course::getName)));
    }

    @Operation(summary = "新增课程")
    @PostMapping
    public Result<Void> add(@RequestBody Course course) {
        courseService.save(course);
        return Result.success();
    }

    @Operation(summary = "修改课程")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        courseService.updateById(course);
        return Result.success();
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "查询课程详情")
    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }
}
