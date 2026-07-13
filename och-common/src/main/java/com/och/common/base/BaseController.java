package com.och.common.base;

import com.och.common.api.ApiResponse;

/**
 * 基础控制器
 * <p>
 * 提供 success()、error() 等快捷方法，子类 Controller 通过继承即可直接调用。
 * 方法内部委托给 {@link ApiResponse} 的静态工厂方法创建统一响应结果。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * @RestController
 * @RequestMapping("/example")
 * public class ExampleController extends BaseController {
 *
 *     @GetMapping("/get")
 *     public ApiResponse<SomeVo> get() {
 *         SomeVo data = service.getData();
 *         return success(data);
 *     }
 *
 *     @PostMapping("/add")
 *     public ApiResponse add() {
 *         service.add();
 *         return success();
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @author danmo
 */
public class BaseController {

    /**
     * 成功响应
     * <p>
     * 返回 code=200, msg="success" 的响应结果，并携带指定的数据。
     * </p>
     *
     * @param data 返回数据
     * @param <T>  数据类型
     * @return ApiResponse 成功响应结果
     */
    protected <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    /**
     * 成功响应（无返回数据）
     * <p>
     * 返回 code=200, msg="success" 的响应结果，data 字段为 null。
     * 适用于新增、修改、删除等不需要返回数据的操作。
     * </p>
     *
     * @param <T> 数据类型
     * @return ApiResponse 成功响应结果
     */
    protected <T> ApiResponse<T> success() {
        return ApiResponse.success();
    }

    /**
     * 失败响应
     * <p>
     * 返回指定错误码和错误信息的响应结果，data 字段为 null。
     * </p>
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return ApiResponse 失败响应结果
     */
    protected <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.error(code, message);
    }

    /**
     * 失败响应（带错误数据）
     * <p>
     * 返回指定错误码、错误信息和错误数据的响应结果。
     * </p>
     *
     * @param code    错误码
     * @param message 错误信息
     * @param data    错误数据
     * @param <T>     数据类型
     * @return ApiResponse 失败响应结果
     */
    protected <T> ApiResponse<T> error(Integer code, String message, T data) {
        return ApiResponse.error(code, message, data);
    }
}
