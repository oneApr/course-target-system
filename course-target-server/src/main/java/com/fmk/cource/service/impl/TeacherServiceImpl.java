package com.fmk.cource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.cource.entity.SysRole;
import com.fmk.cource.entity.SysUser;
import com.fmk.cource.entity.SysUserRole;
import com.fmk.cource.entity.Teacher;
import com.fmk.cource.mapper.SysRoleMapper;
import com.fmk.cource.mapper.SysUserRoleMapper;
import com.fmk.cource.mapper.TeacherMapper;
import com.fmk.cource.service.SysUserService;
import com.fmk.cource.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    private final SysUserService sysUserService;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void fillAccountInfo(List<Teacher> teachers) {
        if (teachers == null || teachers.isEmpty()) return;
        List<Long> userIds = teachers.stream().map(Teacher::getUserId).filter(Objects::nonNull).collect(Collectors.toList());
        if (userIds.isEmpty()) return;
        Map<Long, SysUser> userMap = sysUserService.listByIds(userIds).stream().collect(Collectors.toMap(SysUser::getUserId, u -> u));
        for (Teacher t : teachers) {
            if (t.getUserId() != null) {
                SysUser user = userMap.get(t.getUserId());
                if (user != null) {
                    t.setAccount(user.getUsername());
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTeacher(Teacher teacher) {
        long count = sysUserService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, teacher.getAccount()));
        if (count > 0) {
            throw new com.fmk.cource.common.BusinessException(400, "账号已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(teacher.getAccount());
        user.setPassword(passwordEncoder.encode(StringUtils.hasText(teacher.getPassword()) ? teacher.getPassword() : null));
        user.setDisplayName(teacher.getAccount() + "老师");
        user.setStatus(teacher.getStatus());
        sysUserService.save(user);

        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, "teacher"));
        if (role != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getUserId());
            userRole.setRoleId(role.getRoleId());
            sysUserRoleMapper.insert(userRole);
        }

        teacher.setUserId(user.getUserId());
        save(teacher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTeacher(Teacher teacher) {
        Teacher old = getById(teacher.getId());
        if (old != null && old.getUserId() != null) {
            SysUser user = sysUserService.getById(old.getUserId());
            if (user != null) {
                boolean userChanged = false;
                if (StringUtils.hasText(teacher.getAccount()) && !teacher.getAccount().equals(user.getUsername())) {
                    long count = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getUsername, teacher.getAccount())
                            .ne(SysUser::getUserId, user.getUserId()));
                    if (count > 0) {
                        throw new com.fmk.cource.common.BusinessException(400, "账号已存在");
                    }
                    user.setUsername(teacher.getAccount());
                    userChanged = true;
                }
                if (StringUtils.hasText(teacher.getPassword())) {
                    user.setPassword(passwordEncoder.encode(teacher.getPassword()));
                    userChanged = true;
                }
                if (StringUtils.hasText(teacher.getStatus()) && !teacher.getStatus().equals(user.getStatus())) {
                    user.setStatus(teacher.getStatus());
                    userChanged = true;
                }
                if (StringUtils.hasText(teacher.getAccount()) && !teacher.getAccount().concat("老师").equals(user.getDisplayName())) {
                    user.setDisplayName(teacher.getAccount() + "老师");
                    userChanged = true;
                }
                if (userChanged) {
                    sysUserService.updateById(user);
                }
            }
        }
        updateById(teacher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTeacher(Long id) {
        Teacher teacher = getById(id);
        if (teacher != null && teacher.getUserId() != null) {
            sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, teacher.getUserId()));
            sysUserService.removeById(teacher.getUserId());
        }
        removeById(id);
    }
}
