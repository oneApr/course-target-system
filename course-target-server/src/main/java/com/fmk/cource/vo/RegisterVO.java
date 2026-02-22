package com.fmk.cource.vo;

import lombok.Data;

/**
 * 注册请求 VO
 */
@Data
public class RegisterVO {
    private String username;
    private String password;
    private String displayName;
    /** 角色：director / teacher */
    private String roleKey;
}
