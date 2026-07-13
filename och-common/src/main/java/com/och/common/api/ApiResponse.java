package com.och.common.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一 API 响应体
 * <p>
 * 作为所有 HTTP 接口返回值的标准封装，包含状态码、消息、数据和链路追踪ID。
 * 提供 success()、error() 等静态工厂方法，方便快速构建响应。
 * 子类 {@link com.och.common.base.ResResult} 继承此类以扩展业务层响应能力。
 * </p>
 *
 * @param <T> 响应数据类型
 * @author danmo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;

    @JsonProperty("msg")
    private String message;

    private T data;
    private String traceId;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        response.setTraceId(com.och.common.trace.TraceContext.getTraceId());
        return response;
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setTraceId(com.och.common.trace.TraceContext.getTraceId());
        return response;
    }

    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        ApiResponse<T> response = error(code, message);
        response.setData(data);
        return response;
    }
}
