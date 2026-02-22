package com.fmk.cource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 菜单权限表 DO
 */
@Data
@TableName("sys_menu")
public class SysMenu {
    @TableId(type = IdType.AUTO)
    private Long menuId;
    private String menuName;
    private Long parentId;
    private String path;
    private String icon;
    private String perms;
    private String menuType;
    private Integer sortOrder;
    private String status;
}
