package com.och.common.web;

import com.och.common.api.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;

/**
 * API 响应自动包装切面
 * <p>
 * 实现 Spring MVC 的 ResponseBodyAdvice 接口，在 Controller 返回结果写入响应体之前，
 * 自动将非 ApiResponse 类型的返回值包装为 {@link ApiResponse#success(Object)}。
 * 已在 supports() 中排除 ApiResponse、ResResult、ResponseEntity、String 和文件流等特殊类型，
 * 避免重复包装或破坏文件下载、流式响应。
 * </p>
 *
 * @author danmo
 */
@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    /**
     * 判断是否需要包装
     * <p>
     * 文件下载、流式输出和 ResponseEntity 往往已经包含状态码、响应头或二进制内容，
     * 这些返回值不能再套统一 JSON 响应体，否则会破坏下载协议或导致消息转换失败。
     * </p>
     *
     * @param returnType    Controller 方法返回类型
     * @param converterType 消息转换器类型
     * @return true 表示需要包装
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> type = returnType.getParameterType();
        if (ApiResponse.class.isAssignableFrom(type)) {
            return false;
        }
        if (ResponseEntity.class.isAssignableFrom(type)) {
            return false;
        }
        if (String.class.isAssignableFrom(type)) {
            return false;
        }
        if (Resource.class.isAssignableFrom(type) || InputStream.class.isAssignableFrom(type)
                || StreamingResponseBody.class.isAssignableFrom(type) || byte[].class.equals(type)) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, org.springframework.http.MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response) {
        if (body instanceof ApiResponse<?> || body instanceof String) {
            return body;
        }
        return ApiResponse.success(body);
    }
}
