package com.och.api.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.och.api.service.ILoginService;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.common.constant.SecurityConstants;
import com.och.common.constant.TokenConstants;
import com.och.common.enums.ExceptionStatusEnum;
import com.och.common.exception.LoginException;
import com.och.security.authority.LoginUserInfo;
import com.och.security.utils.SecurityUtils;
import com.och.system.domain.query.login.LoginQuery;
import com.och.system.domain.vo.login.LoginUserVo;
import com.och.system.service.ISysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author danmo
 * @date 2024-02-21 15:13
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements ILoginService {

    private final AuthenticationManager authenticationManager;
    private final ISysUserService iSysUserService;
    private final RedisService redisService;


    private final static Integer EXPIRE_TIME = CacheConstants.EXPIRATION;

    private final static String ACCESS_TOKEN = CacheConstants.LOGIN_TOKEN_KEY;

    @Override
    public LoginUserVo login(LoginQuery query) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                query.getUsername(),
                query.getPassword()
        ));
        LoginUserInfo loginUserInfo = (LoginUserInfo) authenticate.getPrincipal();
        String jwtToken = JWT.create()
                .withClaim(SecurityConstants.USER_ID, loginUserInfo.getUserId())
                .withClaim(SecurityConstants.USER_NAME, loginUserInfo.getUsername())
                .withClaim(SecurityConstants.AVATAR, loginUserInfo.getAvatar())
                .withClaim(SecurityConstants.PHONE, loginUserInfo.getPhone())
                .withClaim(SecurityConstants.LOGIN_USER, JSONObject.toJSONString(loginUserInfo))
                .withClaim(SecurityConstants.TIME,DateUtil.current())
                .sign(Algorithm.HMAC512(TokenConstants.SECRET));
        redisService.setCacheObject(ACCESS_TOKEN + loginUserInfo.getUserId(), jwtToken, EXPIRE_TIME, TimeUnit.MINUTES);
        // 将token返回响应
        return LoginUserVo.builder()
                .accessToken(jwtToken)
                .expiresIn(EXPIRE_TIME)
                .build();
    }

    @Override
    public void logout() {
        Long userId = SecurityUtils.getUserId();
        redisService.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + userId);
        SecurityContextHolder.clearContext();
    }
}
