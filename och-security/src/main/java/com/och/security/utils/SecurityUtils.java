package com.och.security.utils;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.och.security.authority.LoginUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;

/**
 * @author danmo
 * @date 2024-07-10 15:31
 **/
public class SecurityUtils extends com.och.common.utils.SecurityUtils {

    private static final ThreadLocal<LoginUserInfo> THREAD_LOCAL = new TransmittableThreadLocal<>();

    /**
     * 获取当前用户信息
     *
     * @return LoginUserInfo
     */
    public static LoginUserInfo getCurrentUserInfo() {

        if(Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof LoginUserInfo userInfo){
            return userInfo;
        }

        if(Objects.nonNull(THREAD_LOCAL.get())){
            return THREAD_LOCAL.get();
        }
        return null;
    }

    public static void setThreadLocalCurrentUserInfo(LoginUserInfo userInfo) {
        THREAD_LOCAL.set(userInfo);
    }

    public static void removeThreadLoCurrentUserInfo() {
        THREAD_LOCAL.remove();
    }


    /**
     * 获取当前用户ID
     *
     * @return Long
     */
    public static Long getUserId() {
        LoginUserInfo userInfo = getCurrentUserInfo();
        if (Objects.nonNull(userInfo)) {
            return userInfo.getUserId();
        }
        return null;
    }

    /**
     * 获取当前用户账号
     *
     * @return String
     */
    public static String getUserName() {
        LoginUserInfo userInfo = getCurrentUserInfo();
        if (Objects.nonNull(userInfo)) {
            return userInfo.getUsername();
        }
        return null;
    }

    /**
     * 获取当前用户角色
     *
     * @return List<Long>
     */
    public static List<Long> getRole() {
        LoginUserInfo userInfo = getCurrentUserInfo();
        if (Objects.nonNull(userInfo)) {
            return userInfo.getRoleIds();
        }
        return null;
    }

    /**
     * 获取当前用户数据权限
     *
     * @return List<Integer>
     */
    public static List<Integer> getDataScope() {
        LoginUserInfo userInfo = getCurrentUserInfo();
        if (Objects.nonNull(userInfo)) {
            return userInfo.getDataScope();
        }
        return null;
    }

    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    public static String getCorpCode() {
        LoginUserInfo userInfo = getCurrentUserInfo();
        if (Objects.nonNull(userInfo)) {
            return userInfo.getCorpCode();
        }
        return null;
    }
}
