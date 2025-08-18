package com.och.security.filter;

import com.alibaba.fastjson.JSONObject;
import com.och.security.authority.LoginUserInfo;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.common.constant.TokenConstants;
import com.och.security.utils.JwtUtils;
import com.och.common.utils.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author danmo
 * @date 2024-02-21 11:04
 **/
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 1. 获取 header
        String authHeader = request.getHeader(TokenConstants.AUTHENTICATION);
        if (authHeader == null || !authHeader.startsWith(TokenConstants.PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        // 2. 解析 token
        String token = authHeader.substring(TokenConstants.PREFIX.length());
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) {
            // token 无效或过期
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }
        // 3. 校验 Redis 中的 token
        String cacheKey = CacheConstants.LOGIN_TOKEN_KEY + userId;
        String jwtToken = redisService.getCacheObject(cacheKey);
        if (StringUtils.isEmpty(jwtToken) || !jwtToken.equals(token)) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }
        // 4. 构建认证信息
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            // todo 这里是从token获取用户信息，赋值权限需要重新登入
            //  ，应该将用户的登入标识放入token中，其他全部放置到Redis中 ，注意ws同步调整
            LoginUserInfo userDetails = JSONObject.parseObject(JwtUtils.getLoginUserInfo(token), LoginUserInfo.class);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // 刷新过期时间
            redisService.expire(cacheKey, CacheConstants.EXPIRATION);
        }
        filterChain.doFilter(request, response);
    }
}
