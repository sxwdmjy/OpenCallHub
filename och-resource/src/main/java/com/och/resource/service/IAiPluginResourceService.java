package com.och.resource.service;
import com.och.common.api.PageResponse;
import com.och.resource.domain.AiPlugin;
public interface IAiPluginResourceService {
    PageResponse<AiPlugin> getPageList(int pageNum, int pageSize, String pluginType, String keyword);
    AiPlugin getDetail(Long id);
    AiPlugin add(AiPlugin plugin);
    AiPlugin edit(Long id, AiPlugin plugin);
    void delete(Long id);
}
