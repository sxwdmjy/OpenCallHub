package com.och.common.web;

import com.och.common.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * API 响应包装测试
 * <p>
 * 统一响应包装是 Controller 层的公共出口，需要保证普通对象会被包装，
 * 但文件下载、ResponseEntity、String 和已包装响应不能被二次处理。
 * </p>
 *
 * @author danmo
 */
class ApiResponseAdviceTest {

    private final ApiResponseAdvice advice = new ApiResponseAdvice();

    @Test
    void supportsWrapsPlainObject() throws Exception {
        assertTrue(advice.supports(returnType("plainObject"), converterType()));
    }

    @Test
    void supportsSkipsAlreadyWrappedAndSpecialTypes() throws Exception {
        assertFalse(advice.supports(returnType("apiResponse"), converterType()));
        assertFalse(advice.supports(returnType("responseEntity"), converterType()));
        assertFalse(advice.supports(returnType("stringBody"), converterType()));
        assertFalse(advice.supports(returnType("resourceBody"), converterType()));
        assertFalse(advice.supports(returnType("bytesBody"), converterType()));
    }

    @Test
    void beforeBodyWriteWrapsPlainBody() {
        Object body = advice.beforeBodyWrite(Map.of("status", "UP"), null, null, null, null, null);

        ApiResponse<?> response = assertInstanceOf(ApiResponse.class, body);
        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
    }

    private MethodParameter returnType(String methodName) throws NoSuchMethodException {
        Method method = ControllerReturnTypes.class.getDeclaredMethod(methodName);
        return new MethodParameter(method, -1);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Class<? extends HttpMessageConverter<?>> converterType() {
        return (Class) MappingJackson2HttpMessageConverter.class;
    }

    private static class ControllerReturnTypes {

        Map<String, String> plainObject() {
            return Map.of();
        }

        ApiResponse<Void> apiResponse() {
            return ApiResponse.success();
        }

        ResponseEntity<String> responseEntity() {
            return ResponseEntity.ok("ok");
        }

        String stringBody() {
            return "ok";
        }

        ByteArrayResource resourceBody() {
            return new ByteArrayResource(new byte[0]);
        }

        byte[] bytesBody() {
            return new byte[0];
        }
    }
}
