package com.och.system.domain.query.fssip;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

/**
 * @author danmo
 * @date 2023年09月11日 15:01
 */
@Schema
@Data
public class FsSipGatewayAddQuery {

    @Schema(description = "ID",hidden = true)
    private Long id;


    /**
     * 网关名称
     */
    @NotEmpty(message = "网关名称不能为空")
    @Schema(description = "网关名称",requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;


    /**
     * 账号
     */
    @Schema(description = "账号",requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;


    /**
     * 密码
     */
    @Schema(description = "密码",requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;


    /**
     * 认证地址
     */
    @NotEmpty(message = "认证地址不能为空")
    @Schema(description = "认证地址",requiredMode = Schema.RequiredMode.REQUIRED)
    private String realm;


    /**
     * 代理地址
     */
    @NotEmpty(message = "代理地址不能为空")
    @Schema(description = "代理地址",requiredMode = Schema.RequiredMode.REQUIRED)
    private String proxy;


    /**
     * 注册类型 0-不注册 1-注册
     */
    @Schema(description = "注册类型 0-不注册 1-注册",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer register;


    /**
     * 注册协议 1-udp, 2-tcp
     */
    @Schema(description = "注册协议 1-udp, 2-tcp",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer transport;


    /**
     * from域
     */
    @Schema(description = "from域",requiredMode = Schema.RequiredMode.REQUIRED)
    private String fromDomain;


    /**
     * 重试时间（秒）
     */
    @Schema(description = "重试时间（秒）",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer retryTime;


    /**
     * 心跳时间（秒）
     */
    @Schema(description = "心跳时间（秒）",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pingTime;
}
