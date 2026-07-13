package com.och.resource.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.resource.domain.ResourceAudit;
import com.och.resource.domain.ResourceType;
public interface IResourceAuditService {
    void record(String action, ResourceType resourceType, Long resourceId, String beforeData, String afterData);
    void recordCreate(ResourceType resourceType, Long resourceId, String afterData);
    void recordUpdate(ResourceType resourceType, Long resourceId, String beforeData, String afterData);
    void recordDelete(ResourceType resourceType, Long resourceId, String beforeData);
    IPage<ResourceAudit> queryPage(int pageNum, int pageSize, ResourceType resourceType, Long resourceId);
}
