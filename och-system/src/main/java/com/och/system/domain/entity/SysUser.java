package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;


/**
 * 系统用户
 * <p>映射 sys_user 表，存储平台登录用户的账号、密码、昵称、手机号等基本信息。
 * 通过 SysUserRole 关联角色，角色关联菜单权限。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class SysUser {

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long userId;


    /** 登录账号 */
    @TableField("user_name")
    private String userName;

    /** 用户昵称 */
    @TableField("nick_name")
    private String nickName;

    /** 加密密码 */
    private String password;

    /** 头像地址 */
    private String avatar;

    /** 性别：0-未知 1-男 2-女 */
    private Integer sex;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态：1-启用 2-禁用 */
    private Integer status;

    /** 备注 */
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

