package com.fmk.cource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fmk.cource.entity.CourseTask;
import com.fmk.cource.vo.CourseTaskVO;

import java.util.List;

public interface CourseTaskService extends IService<CourseTask> {
    List<CourseTaskVO> listWithDetail(Long teacherId);
}
