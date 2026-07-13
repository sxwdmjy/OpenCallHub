package com.och.resource.service.impl;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.common.api.PageResponse;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.resource.domain.NumberPool;
import com.och.resource.domain.ResourceType;
import com.och.resource.mapper.NumberPoolMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 号码池应用服务
 * <p>统一处理号码池资源的查询、变更审计和删除保护，避免 Controller 直接编排 Mapper、审计和依赖检查。</p>
 *
 * @author danmo
 * @date 2025-01-01
 */
@Service
@RequiredArgsConstructor
public class NumberPoolServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.resource.mapper.NumberPoolMapper, com.och.resource.domain.NumberPool> implements com.och.resource.service.INumberPoolService {

    private final ResourceDependencyServiceImpl dependencyService;
    private final ResourceAuditServiceImpl auditService;

    public PageResponse<NumberPool> getPageList(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<NumberPool> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(NumberPool::getPoolName, keyword)
                    .or().like(NumberPool::getPoolCode, keyword));
        }
        wrapper.orderByDesc(NumberPool::getCreatedAt);
        IPage<NumberPool> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    public NumberPool getDetail(Long id) {
        return baseMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public NumberPool add(NumberPool pool) {
        baseMapper.insert(pool);
        auditService.recordCreate(ResourceType.NUMBER_POOL, pool.getId(), safeSummary(pool));
        return pool;
    }

    @Transactional(rollbackFor = Exception.class)
    public NumberPool edit(Long id, NumberPool pool) {
        NumberPool existing = getRequired(id);
        pool.setId(id);
        baseMapper.updateById(pool);
        auditService.recordUpdate(ResourceType.NUMBER_POOL, id, safeSummary(existing), safeSummary(pool));
        return pool;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        NumberPool existing = getRequired(id);
        dependencyService.checkDeletable(ResourceType.NUMBER_POOL, id);
        baseMapper.deleteById(id);
        auditService.recordDelete(ResourceType.NUMBER_POOL, id, safeSummary(existing));
    }

    private NumberPool getRequired(Long id) {
        NumberPool pool = baseMapper.selectById(id);
        if (pool == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "号码池不存在: " + id);
        }
        return pool;
    }

    private String safeSummary(NumberPool pool) {
        if (pool == null) {
            return null;
        }
        return "poolName=" + pool.getPoolName()
                + ",poolCode=" + pool.getPoolCode()
                + ",enabled=" + pool.getEnabled()
                + ",status=" + pool.getStatus();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
