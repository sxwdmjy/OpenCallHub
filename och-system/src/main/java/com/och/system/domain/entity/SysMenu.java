package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;


/**
 * 菜单权限实体
 * <p>映射 sys_menu 表，存储系统菜单及权限信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_menu")
public class SysMenu {

    /** 菜单ID */
    @TableId(type = IdType.AUTO)
    private Long menuId;


    /** 菜单名称 */
    @TableField("menu_name")
    private String menuName;

    /** 父菜单ID */
    @TableField("parent_id")
    private Long parentId;

    /** 显示顺序 */
    @TableField("order_num")
    private Integer orderNum;

    /** 路由地址 */
    @TableField("path")
    private String path;

    /** 组件路径 */
    @TableField("component")
    private String component;

    /** 是否为外链（0是 1否） */
    @TableField("is_frame")
    private Integer isFrame;

    /** 菜单类型（M目录 C菜单 F按钮） */
    @TableField("menu_type")
    private String menuType;

    /** 菜单状态（0显示 1隐藏） */
    @TableField("visible")
    private Integer visible;

    /** 菜单状态（0正常 1停用） */
    @TableField("status")
    private Integer status;

    /** 权限标识 */
    @TableField("perms")
    private String perms;

    /** 菜单图标 */
    @TableField("icon")
    private String icon;

    /** 备注 */
    @TableField("remark")
    private String remark;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /** 逻辑删除：0-正常 1-已删除 */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

}

