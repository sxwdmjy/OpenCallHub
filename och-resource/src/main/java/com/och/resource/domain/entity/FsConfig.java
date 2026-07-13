package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * FS管理配置实体
 * <p>映射 fs_config 表，存储 FreeSWITCH 管理配置信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fs_config")
public class FsConfig {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 名称 */
    @TableField("name")
    private String name;

    /** 客户端分组 */
    @TableField("`group`")
    private String group;

    /** 机器地址 IP */
    @TableField("ip")
    private String ip;

    /** 机器地址端口 */
    @TableField("port")
    private Integer port;

    /** 用户名 */
    @TableField("user_name")
    private String userName;

    /** 密码 */
    @TableField("password")
    private String password;

    /** 状态：0-在线 1-下线 */
    @TableField("status")
    private Integer status;

    /** 超时时间（秒） */
    @TableField("out_time")
    private Integer outTime;

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

