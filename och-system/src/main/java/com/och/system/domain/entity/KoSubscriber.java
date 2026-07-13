package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * Kamailio SIP 订阅用户
 * <p>映射 ko_subscriber 表，存储 Kamailio 的 SIP 用户认证信息，
 * 包含用户名、域、密码哈希（HA1/HA1B）、语音信箱 PIN 等。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ko_subscriber")
public class KoSubscriber {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** SIP 用户名 */
    @TableField("username")
    private String username;

    /** SIP 域 */
    @TableField("domain")
    private String domain;

    /** SIP 密码 */
    @TableField("password")
    @JsonIgnore
    private String password;

    /** HA1 哈希值 */
    @TableField("ha1")
    private String ha1;

    /** HA1B 哈希值 */
    @TableField("ha1b")
    private String ha1b;

    /** 语音信箱 PIN */
    @TableField("vmpin")
    private String vmpin;

    /** 状态：0-开启 1-关闭 */
    @TableField("status")
    private Integer status;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
