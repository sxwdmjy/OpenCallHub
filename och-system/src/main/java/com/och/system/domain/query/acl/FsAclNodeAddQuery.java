package com.och.system.domain.query.acl;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author danmo
 * @date 2023年09月11日 15:01
 */
@Schema
@Data
public class FsAclNodeAddQuery {

    @Schema(description = "ID", hidden = true)
    private Long id;


    @Schema(description = "规则表ID" ,requiredMode = Schema.RequiredMode.REQUIRED)
    private Long listId;

    /**
     * 网关名称
     */
    @NotEmpty(message = "名称不能为空")
    @Schema(description = "名称",requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;


    /**
     * 类型 allow-允许 deny-拒绝
     */
    @Schema(description = "类型 allow-允许 deny-拒绝",requiredMode = Schema.RequiredMode.REQUIRED)
    private String defaultType;

    /**
     * 规则类型 allow-允许 deny-拒绝
     */
    @NotEmpty(message = "规则类型不能为空")
    @Schema(description = "规则类型 allow-允许 deny-拒绝",requiredMode = Schema.RequiredMode.REQUIRED)
    private String nodeType;


    /**
     * IP地址
     */
    @Schema(description = "IP地址",requiredMode = Schema.RequiredMode.REQUIRED)
    private String cidr;


    /**
     * 域地址
     */
    @Schema(description = "域地址",requiredMode = Schema.RequiredMode.REQUIRED)
    private String domain;

}
