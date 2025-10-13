package com.och.security.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.och.common.constant.SecurityConstants;
import com.och.common.constant.TokenConstants;
import com.och.security.authority.LoginUserInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author danmo
 * @date 2024-02-21 11:07
 **/
@Slf4j
public class JwtUtils {

    /**
     * 解析token
     *
     * @param token
     * @return
     */
    public static Map<String, Claim> decodeToken(String token) {
        Map<String, Claim> claims = new HashMap<>(16);
        try {
            claims = JWT.decode(token).getClaims();
        } catch (JWTDecodeException e) {
            log.error("token解码异常:{}", e.getMessage(), e);
        }
        return claims;
    }

    /**
     * 校验token并解析token
     *
     * @param token
     * @return
     */
    public static Map<String, Claim> verifyToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC512(TokenConstants.SECRET)).build();
        DecodedJWT verify = null;
        try {
            verify = verifier.verify(token);
        } catch (Exception e) {
            log.error("token解码异常:{}", e.getMessage(), e);
            return Collections.emptyMap();
        }
        return verify.getClaims();
    }

    public static String getUserName(String token) {
       return verifyToken(token).get(SecurityConstants.USER_NAME).asString();
    }

    public static Long getUserId(String token) {
        return verifyToken(token).get(SecurityConstants.USER_ID).asLong();
    }

    public static String getLoginUserInfo(String token) {
        return verifyToken(token).get(SecurityConstants.LOGIN_USER).asString();
    }

    /**
     * 判断token是否过期
     * @param
     * @return
     */
    public static boolean isTokenValid(String token, LoginUserInfo userDetails) {
        final String username = getUserName(token);
        return (username.equals(userDetails.getUsername())) &&!isTokenExpired(token);
    }

    /**
     * 验证token是否过期
     * @param token
     * @return
     */
    public static boolean isTokenExpired(String token) {
        return JWT.decode(token).getExpiresAt().before(new Date());
    }
}
