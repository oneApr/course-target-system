package com.fmk.cource.vo;

import lombok.Data;

/**
 * 忘记密码 / 修改密码请求 VO
 */
@Data
public class ResetPasswordVO {
    /** 用户名（用于定位账号）*/
    private String username;
    /** 新密码 */
    private String newPassword;
}
