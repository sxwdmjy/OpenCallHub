package com.och.system.domain.query.fssip;

import com.baomidou.mybatisplus.annotation.TableField;
import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author danmo
 * @date 2023年09月11日 15:01
 */
@Schema
@Data
public class FsSipGatewayQuery extends BaseQuery {

    @Schema(description = "ID")
    private Long id;

    private List<Long> ids;
    /**
     * 网关名称
     */
    @Schema(description = "网关名称")
    private String name;


    /**
     * 账号
     */
    @Schema(description = "账号")
    private String uerName;

    /**
     * 认证地址
     */
    @Schema(description = "认证地址")
    private String realm;


    /**
     * 代理地址
     */
    @Schema(description = "代理地址")
    private String proxy;


    /**
     * 注册类型 0-不注册 1-注册
     */
    @Schema(description = "注册类型 0-不注册 1-注册")
    private Integer register;


    /**
     * 注册协议 1-udp, 2-tcp
     */
    @Schema(description = "注册协议 1-udp, 2-tcp")
    private Integer transport;

    @Schema(description = "网关类型 1-internal 2-external")
    private Integer type;

    @Schema(description = "网关类型 0-非外线 1-外线")
    private Integer gatewayType;
}
