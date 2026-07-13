package com.och.common.base;

import com.och.common.api.ApiResponse;

/**
 * 统一响应结果类（已废弃）
 * <p>
 * 本项目已统一使用 {@link ApiResponse} 作为唯一响应类型。
 * 本类保留仅用于编译兼容，所有方法直接委托给 ApiResponse。
 * 新代码请直接使用 {@link ApiResponse} 或继承 {@link BaseController} 调用其便捷方法。
 * </p>
 *
 * @param <T> 数据类型
 * @author danmo
 * @deprecated 请使用 {@link ApiResponse} 替代
 */
@Deprecated
public class ResResult<T> extends ApiResponse<T> {

    private static final long serialVersionUID = 1L;

    @Deprecated
    public static <T> ResResult<T> success(T data) {
        ResResult<T> result = new ResResult<>();
        result.setCode(ApiResponse.success(data).getCode());
        result.setMessage("success");
        result.setData(data);
        result.setTraceId(ApiResponse.success(data).getTraceId());
        return result;
    }

    @Deprecated
    public static <T> ResResult<T> success() {
        return success(null);
    }

    @Deprecated
    public static <T> ResResult<T> error(Integer code, String message) {
        ResResult<T> result = new ResResult<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTraceId(com.och.common.trace.TraceContext.getTraceId());
        return result;
    }

    @Deprecated
    public static <T> ResResult<T> error(String msg) {
        return error(500, msg);
    }

    @Deprecated
    public static <T> ResResult<T> error(Integer code, String msg, T data) {
        ResResult<T> result = error(code, msg);
        result.setData(data);
        return result;
    }
}