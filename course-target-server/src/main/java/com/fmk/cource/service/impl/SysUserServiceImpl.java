package com.fmk.cource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fmk.cource.common.BusinessException;
import com.fmk.cource.entity.SysMenu;
import com.fmk.cource.entity.SysRole;
import com.fmk.cource.entity.SysUser;
import com.fmk.cource.entity.SysUserRole;
import com.fmk.cource.mapper.SysMenuMapper;
import com.fmk.cource.mapper.SysRoleMapper;
import com.fmk.cource.mapper.SysUserMapper;
import com.fmk.cource.mapper.SysUserRoleMapper;
import com.fmk.cource.security.JwtUtil;
import com.fmk.cource.service.SysUserService;
import com.fmk.cource.vo.LoginVO;
import com.fmk.cource.vo.MenuVO;
import com.fmk.cource.vo.RegisterVO;
import com.fmk.cource.vo.ResetPasswordVO;
import com.fmk.cource.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Override
    public UserInfoVO login(LoginVO loginVO) {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginVO.getUsername())
                .eq(SysUser::getStatus, "1"));
        if (user == null) {
            throw new BusinessException(401, "用户名不存在或已停用");
        }
        if (!passwordEncoder.matches(loginVO.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "密码错误");
        }
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
        // 将 token 存入 Redis，24小时有效
        redisTemplate.opsForValue().set("login:token:" + user.getUsername(), token, 24, TimeUnit.HOURS);
        return buildUserInfoVO(user, token);
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        SysUser user = getById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        return buildUserInfoVO(user, null);
    }

    @Override
    public void logout(String username) {
        redisTemplate.delete("login:token:" + username);
    }

    @Override
    public void register(RegisterVO registerVO) {
        // 检查用户名是否已存在
        long count = count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, registerVO.getUsername()));
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在，请换一个用户名");
        }
        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(registerVO.getUsername());
        user.setPassword(passwordEncoder.encode(registerVO.getPassword()));
        user.setDisplayName(registerVO.getDisplayName());
        user.setStatus("1");
        save(user);
        // 分配角色
        String roleKey = registerVO.getRoleKey() != null ? registerVO.getRoleKey() : "teacher";
        SysRole role = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, roleKey));
        if (role != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getUserId());
            userRole.setRoleId(role.getRoleId());
            sysUserRoleMapper.insert(userRole);
        }
    }

    @Override
    public void resetPassword(ResetPasswordVO vo) {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, vo.getUsername()));
        if (user == null) {
            throw new BusinessException(404, "用户名不存在，请检查后重试");
        }
        user.setPassword(passwordEncoder.encode(vo.getNewPassword()));
        updateById(user);
        // 该用户如果已登录，清除 Redis 中的 Token，强制重新登录
        redisTemplate.delete("login:token:" + vo.getUsername());
    }

    private UserInfoVO buildUserInfoVO(SysUser user, String token) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setDisplayName(user.getDisplayName());
        vo.setToken(token);

        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(user.getUserId());
        if (!roles.isEmpty()) {
            vo.setRoleKey(roles.get(0).getRoleKey());
            vo.setRoleName(roles.get(0).getRoleName());
        }

        List<SysMenu> menus = sysMenuMapper.selectMenusByUserId(user.getUserId());
        List<MenuVO> menuVOs = menus.stream().map(m -> {
            MenuVO mv = new MenuVO();
            BeanUtils.copyProperties(m, mv);
            return mv;
        }).collect(Collectors.toList());
        vo.setMenus(menuVOs);

        return vo;
    }
}
