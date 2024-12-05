package com.och.system.domain.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (Subscriber) 订阅用户（SIP用户）
 *
 * @author danmo
 * @date 2024-07-29 10:49:24
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("ko_subscriber")
public class KoSubscriber implements Serializable {

    private static final long serialVersionUID = 1L; //1

    @Schema(description = "id")
    @TableId(type = IdType.AUTO)
    private Integer id;


    @Schema(description = "username")
    @TableField("username")
    private String username;


    @Schema(description = "domain")
    @TableField("domain")
    private String domain;


    @Schema(description = "password")
    @TableField("password")
    @JsonIgnore
    private String password;


    @Schema(description = "ha1")
    @TableField("ha1")
    private String ha1;


    @Schema(description = "ha1b")
    @TableField("ha1b")
    private String ha1b;


    @Schema(description = "vmpin")
    @TableField("vmpin")
    private String vmpin;


    @Schema(description = "状态 0-开启 1-关闭")
    @TableField("status")
    private Integer status;


    @Schema(description = "创建者")
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;


    @Schema(description = "更新者")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
