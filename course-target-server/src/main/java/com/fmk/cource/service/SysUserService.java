package com.fmk.cource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fmk.cource.entity.SysUser;
import com.fmk.cource.vo.LoginVO;
import com.fmk.cource.vo.RegisterVO;
import com.fmk.cource.vo.ResetPasswordVO;
import com.fmk.cource.vo.UserInfoVO;

public interface SysUserService extends IService<SysUser> {
    UserInfoVO login(LoginVO loginVO);
    UserInfoVO getUserInfo(Long userId);
    void logout(String username);
    /** 注册新用户（同时分配角色）*/
    void register(RegisterVO registerVO);
    /** 根据用户名重置密码（忘记密码）*/
    void resetPassword(ResetPasswordVO vo);
}
