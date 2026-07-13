package com.och.resource.service;
import com.och.common.api.PageResponse;
import com.och.resource.domain.NumberPool;
public interface INumberPoolService {
    PageResponse<NumberPool> getPageList(int pageNum, int pageSize, String keyword);
    NumberPool getDetail(Long id);
    NumberPool add(NumberPool pool);
    NumberPool edit(Long id, NumberPool pool);
    void delete(Long id);
}
