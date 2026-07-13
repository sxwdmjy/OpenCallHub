package com.och.resource.service.impl;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.common.api.PageResponse;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.resource.domain.Extension;
import com.och.resource.domain.ResourceType;
import com.och.resource.mapper.ExtensionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 分机应用服务
 * <p>统一处理分机资源的查询、变更审计和删除保护，避免 Controller 直接编排 Mapper、审计和依赖检查。</p>
 *
 * @author danmo
 * @date 2025-01-01
 */
@Service
@RequiredArgsConstructor
public class ExtensionServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.resource.mapper.ExtensionMapper, com.och.resource.domain.Extension> implements com.och.resource.service.IExtensionService {

    private final ResourceDependencyServiceImpl dependencyService;
    private final ResourceAuditServiceImpl auditService;

    public PageResponse<Extension> getPageList(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<Extension> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(Extension::getExtensionNumber, keyword)
                    .or().like(Extension::getDisplayName, keyword));
        }
        wrapper.orderByDesc(Extension::getCreatedAt);
        IPage<Extension> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    public Extension getDetail(Long id) {
        return baseMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Extension add(Extension extension) {
        baseMapper.insert(extension);
        auditService.recordCreate(ResourceType.EXTENSION, extension.getId(), safeSummary(extension));
        return extension;
    }

    @Transactional(rollbackFor = Exception.class)
    public Extension edit(Long id, Extension extension) {
        Extension existing = getRequired(id);
        extension.setId(id);
        baseMapper.updateById(extension);
        auditService.recordUpdate(ResourceType.EXTENSION, id, safeSummary(existing), safeSummary(extension));
        return extension;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Extension existing = getRequired(id);
        dependencyService.checkDeletable(ResourceType.EXTENSION, id);
        baseMapper.deleteById(id);
        auditService.recordDelete(ResourceType.EXTENSION, id, safeSummary(existing));
    }

    private Extension getRequired(Long id) {
        Extension extension = baseMapper.selectById(id);
        if (extension == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "分机不存在: " + id);
        }
        return extension;
    }

    private String safeSummary(Extension extension) {
        if (extension == null) {
            return null;
        }
        return "extensionNumber=" + extension.getExtensionNumber()
                + ",sipAccountId=" + extension.getSipAccountId()
                + ",userId=" + extension.getUserId()
                + ",displayName=" + extension.getDisplayName()
                + ",enabled=" + extension.getEnabled();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
