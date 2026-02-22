package com.fmk.cource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.cource.entity.CourseObjective;
import com.fmk.cource.mapper.CourseObjectiveMapper;
import com.fmk.cource.service.CourseObjectiveService;
import org.springframework.stereotype.Service;

@Service
public class CourseObjectiveServiceImpl extends ServiceImpl<CourseObjectiveMapper, CourseObjective>
        implements CourseObjectiveService {
}
