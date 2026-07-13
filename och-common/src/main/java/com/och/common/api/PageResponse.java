package com.och.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应体
 * <p>用于分页查询接口的返回值封装，包含当前页记录列表、总记录数、页码和页大小。</p>
 * <p>使用示例：
 * <pre>{@code
 * PageResponse<UserVo> page = PageResponse.of(records, total, pageNum, pageSize);
 * }</pre>
 * </p>
 *
 * @param <T> 记录类型
 * @author danmo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> records;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;

    public static <T> PageResponse<T> of(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        PageResponse<T> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(total);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        return response;
    }
}
