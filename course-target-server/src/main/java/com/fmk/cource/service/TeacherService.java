package com.fmk.cource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fmk.cource.entity.Teacher;

import java.util.List;

public interface TeacherService extends IService<Teacher> {
    void fillAccountInfo(List<Teacher> teachers);
    void addTeacher(Teacher teacher);
    void updateTeacher(Teacher teacher);
    void deleteTeacher(Long id);
}
