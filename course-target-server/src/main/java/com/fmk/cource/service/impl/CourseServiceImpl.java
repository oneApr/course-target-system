package com.fmk.cource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.cource.entity.Course;
import com.fmk.cource.mapper.CourseMapper;
import com.fmk.cource.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
}
