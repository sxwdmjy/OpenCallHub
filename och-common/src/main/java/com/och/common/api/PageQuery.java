package com.och.common.api;

import lombok.Data;

/**
 * 分页查询基类
 * <p>
 * 所有需要分页查询的请求参数类继承此类，统一使用 pageNum（页码，从1开始）和 pageSize（每页大小）。
 * 子类只需 extends 后添加自己的筛选字段即可。
 * </p>
 *
 * <p>使用示例：
 * <pre>{@code
 * public class UserQuery extends PageQuery {
 *     private String name;
 *     private Integer status;
 * }
 * }</pre>
 * </p>
 *
 * @author danmo
 */
@Data
public class PageQuery {

    /** 页码，从 1 开始，默认第 1 页 */
    private int pageNum = 1;

    /** 每页大小，默认 20 */
    private int pageSize = 20;
}