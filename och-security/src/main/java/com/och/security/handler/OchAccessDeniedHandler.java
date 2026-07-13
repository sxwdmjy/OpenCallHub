package com.och.security.handler;

import com.alibaba.fastjson.JSONObject;
import com.och.common.api.ApiResponse;
import com.och.common.constant.HttpStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 访问拒绝处理器
 * <p>当已认证用户尝试访问无权资源时触发，返回 403 状态码和 JSON 格式的错误信息。</p>
 *
 * @author danmo
 * @date 2024-02-22 10:04
 */
@Component
public class OchAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json");
        httpServletResponse.getWriter().println(JSONObject.toJSONString(ApiResponse.error(HttpStatus.FORBIDDEN,"访问受限，授权过期")));
        httpServletResponse.getWriter().flush();
    }
}
