package com.fmk.cource.vo;

import lombok.Data;
import java.util.List;

/**
 * 用户信息 VO（登录后返回给前端）
 */
@Data
public class UserInfoVO {
    private Long userId;
    private String username;
    private String displayName;
    private String token;
    private String roleKey;
    private String roleName;
    private List<MenuVO> menus;
}
