package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;


/**
 * 角色信息实体
 * <p>映射 sys_role 表，存储系统角色信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role")
public class SysRole {

    /** 角色ID */
    @TableId(type = IdType.AUTO)
    private Long roleId;


    /** 角色名称 */
    @TableField("role_name")
    private String roleName;

    /** 角色权限字符串 */
    @TableField("role_key")
    private String roleKey;

    /** 显示顺序 */
    @TableField("role_sort")
    private Integer roleSort;

    /** 数据范围（1：全部数据权限 2:本部门及以下数据权限 3：本部门数据权限 4：本人数据权限） */
    @TableField("data_scope")
    private Integer dataScope;

    /** 角色状态（0正常 1停用） */
    @TableField("status")
    private Integer status;

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

    /**
     * 角色及其关联菜单权限的聚合视图
     * <p>用于安全模块加载用户角色时，携带对应的菜单权限列表。</p>
     */
    public record RoleWithMenus(Long roleId, String roleKey, String roleName,
                                Integer status, Integer dataScope,
                                java.util.List<String> permsList) {}
}

