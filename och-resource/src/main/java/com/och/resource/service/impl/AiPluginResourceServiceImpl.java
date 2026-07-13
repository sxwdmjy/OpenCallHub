package com.och.resource.service.impl;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.common.api.PageResponse;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.resource.domain.AiPlugin;
import com.och.resource.domain.ResourceType;
import com.och.resource.mapper.AiPluginMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 插件资源应用服务
 * <p>统一处理 AI 插件配置的查询、变更审计、删除保护和密钥脱敏。
 * API Key、API Secret 只能写入或保留，接口返回和审计摘要均不得暴露明文。</p>
 *
 * @author danmo
 * @date 2025-01-01
 */
@Service
@RequiredArgsConstructor
public class AiPluginResourceServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.resource.mapper.AiPluginMapper, com.och.resource.domain.AiPlugin> implements com.och.resource.service.IAiPluginResourceService {

    private static final String MASK_VALUE = "******";

    private final ResourceDependencyServiceImpl dependencyService;
    private final ResourceAuditServiceImpl auditService;

    public PageResponse<AiPlugin> getPageList(int pageNum, int pageSize, String pluginType, String keyword) {
        LambdaQueryWrapper<AiPlugin> wrapper = new LambdaQueryWrapper<>();
        if (hasText(pluginType)) {
            wrapper.eq(AiPlugin::getPluginType, pluginType);
        }
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(AiPlugin::getPluginName, keyword)
                    .or().like(AiPlugin::getProvider, keyword));
        }
        wrapper.orderByDesc(AiPlugin::getCreatedAt);
        IPage<AiPlugin> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        page.getRecords().forEach(this::maskSecret);
        return PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    public AiPlugin getDetail(Long id) {
        return maskSecret(baseMapper.selectById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public AiPlugin add(AiPlugin plugin) {
        baseMapper.insert(plugin);
        auditService.recordCreate(ResourceType.AI_PLUGIN, plugin.getId(), safeSummary(plugin));
        return maskSecret(plugin);
    }

    @Transactional(rollbackFor = Exception.class)
    public AiPlugin edit(Long id, AiPlugin plugin) {
        AiPlugin existing = getRequired(id);
        plugin.setId(id);
        if (!hasText(plugin.getApiKey()) || MASK_VALUE.equals(plugin.getApiKey())) {
            plugin.setApiKey(existing.getApiKey());
        }
        if (!hasText(plugin.getApiSecret()) || MASK_VALUE.equals(plugin.getApiSecret())) {
            plugin.setApiSecret(existing.getApiSecret());
        }
        baseMapper.updateById(plugin);
        auditService.recordUpdate(ResourceType.AI_PLUGIN, id, safeSummary(existing), safeSummary(plugin));
        return maskSecret(plugin);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        AiPlugin existing = getRequired(id);
        dependencyService.checkDeletable(ResourceType.AI_PLUGIN, id);
        baseMapper.deleteById(id);
        auditService.recordDelete(ResourceType.AI_PLUGIN, id, safeSummary(existing));
    }

    private AiPlugin getRequired(Long id) {
        AiPlugin plugin = baseMapper.selectById(id);
        if (plugin == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "AI插件不存在: " + id);
        }
        return plugin;
    }

    private AiPlugin maskSecret(AiPlugin plugin) {
        if (plugin != null) {
            plugin.setApiKey(MASK_VALUE);
            plugin.setApiSecret(MASK_VALUE);
        }
        return plugin;
    }

    private String safeSummary(AiPlugin plugin) {
        if (plugin == null) {
            return null;
        }
        return "pluginName=" + plugin.getPluginName()
                + ",pluginType=" + plugin.getPluginType()
                + ",provider=" + plugin.getProvider()
                + ",endpointUrl=" + plugin.getEndpointUrl()
                + ",enabled=" + plugin.getEnabled();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
