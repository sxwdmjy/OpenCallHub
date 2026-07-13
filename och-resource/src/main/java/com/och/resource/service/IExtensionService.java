package com.och.resource.service;
import com.och.common.api.PageResponse;
import com.och.resource.domain.Extension;
public interface IExtensionService {
    PageResponse<Extension> getPageList(int pageNum, int pageSize, String keyword);
    Extension getDetail(Long id);
    Extension add(Extension extension);
    Extension edit(Long id, Extension extension);
    void delete(Long id);
}
