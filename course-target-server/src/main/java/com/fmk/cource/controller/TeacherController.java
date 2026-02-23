package com.fmk.cource.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fmk.cource.common.Result;
import com.fmk.cource.entity.Teacher;
import com.fmk.cource.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

@Tag(name = "教师管理")
@RestController
@RequestMapping(value = "/api/teacher", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @Operation(summary = "分页查询教师列表")
    @GetMapping
    public Result<Page<Teacher>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String dept,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) wrapper.like(Teacher::getName, name);
        if (StringUtils.hasText(dept)) wrapper.eq(Teacher::getDept, dept);
        if (StringUtils.hasText(status)) wrapper.eq(Teacher::getStatus, status);
        wrapper.orderByDesc(Teacher::getCreateTime);
        Page<Teacher> pageObj = teacherService.page(new Page<>(page, size), wrapper);
        teacherService.fillAccountInfo(pageObj.getRecords());
        return Result.success(pageObj);
    }

    @Operation(summary = "查询全部教师（不分页，用于下拉框）")
    @GetMapping("/all")
    public Result<List<Teacher>> listAll() {
        List<Teacher> list = teacherService.list(new LambdaQueryWrapper<Teacher>().orderByAsc(Teacher::getName));
        teacherService.fillAccountInfo(list);
        list = list.stream().filter(t -> "1".equals(t.getStatus())).collect(java.util.stream.Collectors.toList());
        return Result.success(list);
    }

    @Operation(summary = "新增教师")
    @PostMapping
    public Result<Void> add(@RequestBody Teacher teacher) {
        teacherService.addTeacher(teacher);
        return Result.success();
    }

    @Operation(summary = "修改教师")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Teacher teacher) {
        teacher.setId(id);
        teacherService.updateTeacher(teacher);
        return Result.success();
    }

    @Operation(summary = "删除教师")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return Result.success();
    }

    @Operation(summary = "查询教师详情")
    @GetMapping("/{id}")
    public Result<Teacher> getById(@PathVariable Long id) {
        Teacher teacher = teacherService.getById(id);
        if (teacher != null) {
            teacherService.fillAccountInfo(java.util.Collections.singletonList(teacher));
        }
        return Result.success(teacher);
    }
}
