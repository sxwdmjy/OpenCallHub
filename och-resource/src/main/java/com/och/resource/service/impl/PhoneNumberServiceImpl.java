package com.och.resource.service.impl;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.common.api.PageResponse;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.resource.domain.PhoneNumber;
import com.och.resource.domain.ResourceType;
import com.och.resource.mapper.PhoneNumberMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 电话号码应用服务
 * <p>统一处理外线号码资源的查询、变更审计和删除保护，避免被号码池、线路或路由引用时误删。</p>
 *
 * @author danmo
 * @date 2025-01-01
 */
@Service
@RequiredArgsConstructor
public class PhoneNumberServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.resource.mapper.PhoneNumberMapper, com.och.resource.domain.PhoneNumber> implements com.och.resource.service.IPhoneNumberService {

    private final ResourceDependencyServiceImpl dependencyService;
    private final ResourceAuditServiceImpl auditService;

    public PageResponse<PhoneNumber> getPageList(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<PhoneNumber> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(PhoneNumber::getPhoneNumber, keyword)
                    .or().like(PhoneNumber::getProvider, keyword));
        }
        wrapper.orderByDesc(PhoneNumber::getCreatedAt);
        IPage<PhoneNumber> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    public PhoneNumber getDetail(Long id) {
        return baseMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public PhoneNumber add(PhoneNumber phoneNumber) {
        baseMapper.insert(phoneNumber);
        auditService.recordCreate(ResourceType.PHONE_NUMBER, phoneNumber.getId(), safeSummary(phoneNumber));
        return phoneNumber;
    }

    @Transactional(rollbackFor = Exception.class)
    public PhoneNumber edit(Long id, PhoneNumber phoneNumber) {
        PhoneNumber existing = getRequired(id);
        phoneNumber.setId(id);
        baseMapper.updateById(phoneNumber);
        auditService.recordUpdate(ResourceType.PHONE_NUMBER, id, safeSummary(existing), safeSummary(phoneNumber));
        return phoneNumber;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PhoneNumber existing = getRequired(id);
        dependencyService.checkDeletable(ResourceType.PHONE_NUMBER, id);
        baseMapper.deleteById(id);
        auditService.recordDelete(ResourceType.PHONE_NUMBER, id, safeSummary(existing));
    }

    private PhoneNumber getRequired(Long id) {
        PhoneNumber phoneNumber = baseMapper.selectById(id);
        if (phoneNumber == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "电话号码不存在: " + id);
        }
        return phoneNumber;
    }

    private String safeSummary(PhoneNumber phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        return "phoneNumber=" + phoneNumber.getPhoneNumber()
                + ",numberPoolId=" + phoneNumber.getNumberPoolId()
                + ",provider=" + phoneNumber.getProvider()
                + ",lineType=" + phoneNumber.getLineType()
                + ",enabled=" + phoneNumber.getEnabled();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
