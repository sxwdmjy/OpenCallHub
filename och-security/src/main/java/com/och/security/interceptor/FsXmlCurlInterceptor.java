package com.och.security.interceptor;

import com.och.common.utils.MD5Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * freeswitch xml_curl接口校验
 *
 * @author danmo
 */
@Slf4j
@Component
public class FsXmlCurlInterceptor implements HandlerInterceptor {

    @Value("${fs.xml-curl.auth-scheme:basic}")
    private String authScheme;

    @Value("${fs.xml-curl.secretKey:}")
    private String secretKey;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // auth-scheme: digest
        // @see com.atomscat.freeswitch.xml.config.SecurityConfiguration
        if ("digest".equals(authScheme)) {
            return true;
        }

        // auth-scheme: basic, you need delete pom.xml security dependency
        if ("basic".equals(authScheme)) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null) {
                return false;
            }
            String base64DecodeStr = authHeader.substring(6);
            String resp = base64Decode(base64DecodeStr);


            log.info("Authorization : {} {}", authHeader, resp);

            if (StringUtils.isEmpty(resp)) {
                return false;
            }

            return authIdentity(resp);
        }
        return false;
    }

    public static String base64Decode(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        byte[] base64decodedBytes = Base64.getDecoder().decode(str);
        return new String(base64decodedBytes, StandardCharsets.UTF_8);
    }


    private Boolean authIdentity(String resp) {
        return resp.contains(secretKey);
        //return MD5Utils.verify(secretKey, resp);
    }
}