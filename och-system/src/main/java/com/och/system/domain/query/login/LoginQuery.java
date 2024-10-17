package com.och.system.domain.query.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author danmo
 * @date 2024-02-21 15:10
 **/

@Schema
@Data
public class LoginQuery {

    /**
     * 用户名
     */
    @NotEmpty(message = "账号不能为空")
    @Schema(description = "用户名")
    private String username;

    /**
     * 用户密码
     */
    @NotEmpty(message = "密码不能为空")
    @Schema(description = "用户密码")
    private String password;


    @Schema(description = "登录类型 1-账号登录 2-手机号登录  默认为1")
    private String loginType = "1";
}
