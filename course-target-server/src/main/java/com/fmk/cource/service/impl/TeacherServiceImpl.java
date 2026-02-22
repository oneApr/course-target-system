package com.fmk.cource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.cource.entity.Teacher;
import com.fmk.cource.mapper.TeacherMapper;
import com.fmk.cource.service.TeacherService;
import org.springframework.stereotype.Service;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {
}
