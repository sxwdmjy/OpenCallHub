package com.och.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.common.trace.TraceContext;
import com.och.resource.domain.ResourceAudit;
import com.och.resource.domain.ResourceType;
import com.och.resource.mapper.ResourceAuditMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 资源审计服务，记录和查询资源的操作审计日志。
 * 提供统一的创建、更新、删除审计记录接口。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceAuditServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.resource.mapper.ResourceAuditMapper, com.och.resource.domain.ResourceAudit> implements com.och.resource.service.IResourceAuditService {

    private static final String MASK_VALUE = "******";


    @Transactional
    public void record(String action, ResourceType resourceType, Long resourceId, String beforeData, String afterData) {
        ResourceAudit audit = ResourceAudit.builder()
                .traceId(TraceContext.getTraceId())
                .resourceType(resourceType.name())
                .resourceId(resourceId)
                .action(action)
                .beforeData(maskSensitiveText(beforeData))
                .afterData(maskSensitiveText(afterData))
                .createdAt(LocalDateTime.now())
                .build();
        baseMapper.insert(audit);
    }

    @Transactional
    public void recordCreate(ResourceType resourceType, Long resourceId, String afterData) {
        record("CREATE", resourceType, resourceId, null, afterData);
    }

    @Transactional
    public void recordUpdate(ResourceType resourceType, Long resourceId, String beforeData, String afterData) {
        record("UPDATE", resourceType, resourceId, beforeData, afterData);
    }

    @Transactional
    public void recordDelete(ResourceType resourceType, Long resourceId, String beforeData) {
        record("DELETE", resourceType, resourceId, beforeData, null);
    }

    public IPage<ResourceAudit> queryPage(int pageNum, int pageSize, ResourceType resourceType, Long resourceId) {
        LambdaQueryWrapper<ResourceAudit> wrapper = new LambdaQueryWrapper<>();
        if (resourceType != null) {
            wrapper.eq(ResourceAudit::getResourceType, resourceType.name());
        }
        if (resourceId != null) {
            wrapper.eq(ResourceAudit::getResourceId, resourceId);
        }
        wrapper.orderByDesc(ResourceAudit::getCreatedAt);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    private String maskSensitiveText(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return text.replaceAll("(?i)(password|apiKey|apiSecret|token|secret)=([^,}\\s]+)", "$1=" + MASK_VALUE)
                .replaceAll("(?i)(\\\"(?:password|apiKey|apiSecret|token|secret)\\\"\\s*:\\s*\\\")([^\\\"]*)(\\\")", "$1" + MASK_VALUE + "$3");
    }
}
