package com.och.api.controller;

import com.och.api.service.ILoginService;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.common.enums.OperatorTypeEnum;
import com.och.system.domain.query.login.LoginQuery;
import com.och.system.domain.vo.login.LoginUserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author danmo
 * @date 2024-02-21 15:08
 **/
@Tag(name = "授权登录")
@RestController
@RequestMapping("/auth/v1")
public class LoginController extends BaseController {

    @Autowired
    private ILoginService iLoginService;

    @Log(title = "系统登陆",businessType = BusinessTypeEnum.LOGIN,operatorType = OperatorTypeEnum.MANAGE)
    @Operation(summary = "系统登陆", method = "POST")
    @PostMapping("/login")
    public ResResult<LoginUserVo> login(@Validated @RequestBody LoginQuery query) {
        return success(iLoginService.login(query));
    }

    @Log(title = "系统登出",businessType = BusinessTypeEnum.LOGOUT,operatorType = OperatorTypeEnum.MANAGE)
    @Operation(summary = "系统登出", method = "GET")
    @GetMapping("/logout")
    public ResResult logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        iLoginService.logout();
        logoutHandler.logout(request, response, authentication);
        return success();
    }
}
