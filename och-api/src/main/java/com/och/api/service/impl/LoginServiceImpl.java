package com.och.api.service.impl;

import com.och.api.service.ILoginService;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.security.authority.LoginUserInfo;
import com.och.security.utils.JwtUtils;
import com.och.system.domain.query.login.LoginQuery;
import com.och.system.domain.vo.login.LoginUserVo;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录服务实现类
 * <p>
 * 处理用户认证、JWT Token 颁发和会话管理。
 * 完整实现 Spring Security 认证流程：调用 AuthenticationManager 进行密码校验，
 * 成功后生成 JWT Token 并缓存至 Redis，返回访问令牌及过期时间。
 * </p>
 *
 * @author danmo
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements ILoginService {

    private final AuthenticationManager authenticationManager;
    private final RedisService redisService;

    /**
     * 用户登录
     * <p>
     * 流程：使用 Spring Security 认证管理器校验用户名密码 →
     * 从认证结果中提取用户完整信息（角色、权限、菜单已由 OchUserDetailsService 加载） →
     * 生成 JWT Token 并缓存至 Redis → 返回访问令牌。
     * </p>
     *
     * @param query 登录请求参数，包含用户名和密码
     * @return 登录成功后的用户信息及令牌
     */
    @Override
    public LoginUserVo login(LoginQuery query) {
        log.info("[LoginService] 用户登录: username={}", query.getUsername());

        // 1. 调用 Spring Security 认证管理器进行身份校验
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(query.getUsername(), query.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 2. 从认证结果中获取用户完整信息（角色、权限、菜单已加载）
        LoginUserInfo loginUserInfo = (LoginUserInfo) authentication.getPrincipal();

        // 3. 生成 JWT Token
        String token = JwtUtils.createToken(loginUserInfo);

        // 4. 将 Token 缓存至 Redis，用于后续请求的身份校验和单点登录控制
        String cacheKey = CacheConstants.LOGIN_TOKEN_KEY + loginUserInfo.getUserId();
        redisService.setCacheObject(cacheKey, token, CacheConstants.EXPIRATION, TimeUnit.MINUTES);

        log.info("[LoginService] 登录成功: userId={}, username={}", loginUserInfo.getUserId(), loginUserInfo.getUsername());

        // 5. 返回 Token 及过期时间
        return LoginUserVo.builder()
                .accessToken(token)
                .expiresIn((int) CacheConstants.EXPIRATION)
                .build();
    }

    /**
     * 用户登出
     * <p>
     * 从 Spring Security 上下文中获取当前用户，清除 Redis 中的 Token 缓存，
     * 使该 Token 立即失效。SecurityContextLogoutHandler 会在 Controller 层清除安全上下文。
     * </p>
     */
    @Override
    public void logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUserInfo loginUserInfo) {
            String cacheKey = CacheConstants.LOGIN_TOKEN_KEY + loginUserInfo.getUserId();
            redisService.deleteObject(cacheKey);
            log.info("[LoginService] 用户登出: userId={}, username={}", loginUserInfo.getUserId(), loginUserInfo.getUsername());
        } else {
            log.warn("[LoginService] 登出时未找到已认证的用户信息");
        }
    }
}
