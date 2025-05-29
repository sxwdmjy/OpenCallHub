package com.och.websocket.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.common.utils.StringUtils;
import com.och.security.authority.LoginUserInfo;
import com.och.security.utils.JwtUtils;
import com.och.security.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * @author danmo
 * @date 2023年09月22日 11:26
 */
@Slf4j
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    private final RedisService redisService;

    public WsHandshakeInterceptor(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            String token = ((ServletServerHttpRequest) request).getServletRequest().getParameter("token");
            final String username;
            if (StringUtils.isBlank(token)) {
                return false;
            }
            // 从token中解析出userId
            Long userId = JwtUtils.getUserId(token);
            String jwtToken = (String) redisService.getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + userId);
            if (StringUtils.isEmpty(jwtToken) && !StringUtils.equals(token, jwtToken)) {
                return false;
            }
            LoginUserInfo userDetails = JSONObject.parseObject(JwtUtils.getLoginUserInfo(token), LoginUserInfo.class);
            attributes.put("user", userDetails);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }


}
