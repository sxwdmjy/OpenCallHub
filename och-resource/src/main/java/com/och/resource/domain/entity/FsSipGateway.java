package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * SIP网关实体
 * <p>映射 fs_sip_gateway 表，存储 SIP 网关配置信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fs_sip_gateway")
public class FsSipGateway {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 网关名称 */
    @TableField("name")
    private String name;

    /** 账号 */
    @TableField("user_name")
    private String userName;

    /** 密码 */
    @TableField("password")
    private String password;

    /** 认证地址 */
    @TableField("realm")
    private String realm;

    /** 代理地址 */
    @TableField("proxy")
    private String proxy;

    /** 注册类型：0-不注册 1-注册 */
    @TableField("register")
    private Integer register;

    /** 注册协议：1-udp 2-tcp */
    @TableField("transport")
    private Integer transport;

    /** 出站呼叫时from字段使用入站callerid：0-true 1-false */
    @TableField("caller_id_in_from")
    private Integer callerIdInFrom;

    /** from域 */
    @TableField("from_domain")
    private String fromDomain;

    /** 重试时间（秒） */
    @TableField("retry_time")
    private Integer retryTime;

    /** 心跳时间（秒） */
    @TableField("ping_time")
    private Integer pingTime;

    /** 超时时间（秒） */
    @TableField("expire_time")
    private Integer expireTime;

    /** 网关类型：1-internal 2-external */
    @TableField("type")
    private Integer type;

    /** 外线标识：0-非外线 1-外线 */
    @TableField("gateway_type")
    private Integer gatewayType;

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

