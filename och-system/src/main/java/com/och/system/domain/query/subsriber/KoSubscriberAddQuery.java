package com.och.system.domain.query.subsriber;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author danmo
 * @date 2023年09月25日 13:58
 */
@Schema
@Data
public class KoSubscriberAddQuery {

    @NotEmpty(message = "账号不能为空")
    @Schema(description = "username",requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;


    @Schema(description = "域")
    private String domain;

    @NotEmpty(message = "密码不能为空")
    @Schema(description = "password",requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "vmpin")
    private String vmpin;

    @Schema(description = "ha1")
    private String ha1;

    @Schema(description = "ha1b")
    private String ha1b;

    @Schema(description = "状态 0-开启 1-关闭")
    private Integer status;
}
