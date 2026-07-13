package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * FS访问控制实体
 * <p>映射 fs_acl 表，存储 FreeSWITCH 访问控制规则。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fs_acl")
public class FsAcl {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 网关名称 */
    @TableField("name")
    private String name;

    /** 类型：allow-允许 deny-拒绝 */
    @TableField("default_type")
    private String defaultType;

    /** 列表ID */
    @TableField("list_id")
    private Long listId;

    /** 规则类型：allow-允许 deny-拒绝 */
    @TableField("node_type")
    private String nodeType;

    /** IP地址 */
    @TableField("cidr")
    private String cidr;

    /** 域地址 */
    @TableField("domain")
    private String domain;

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
