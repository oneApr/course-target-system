package com.fmk.cource.vo;

import lombok.Data;
import java.util.List;

/**
 * 菜单 VO
 */
@Data
public class MenuVO {
    private Long menuId;
    private String menuName;
    private Long parentId;
    private String path;
    private String icon;
    private String perms;
    private String menuType;
    private Integer sortOrder;
    private List<MenuVO> children;
}
