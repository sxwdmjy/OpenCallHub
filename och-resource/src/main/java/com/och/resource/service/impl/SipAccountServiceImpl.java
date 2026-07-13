package com.och.resource.service.impl;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.common.api.PageResponse;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.resource.domain.ResourceType;
import com.och.resource.domain.SipAccount;
import com.och.resource.mapper.SipAccountMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SIP 账号应用服务
 * <p>统一封装 SIP 账号的查询、保存、删除、审计和密码脱敏逻辑，
 * 让 Controller 只负责接口入参与统一响应，避免资源安全规则分散在接口层。</p>
 *
 * @author danmo
 * @date 2025-01-01
 */
@Service
@RequiredArgsConstructor
public class SipAccountServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.resource.mapper.SipAccountMapper, com.och.resource.domain.SipAccount> implements com.och.resource.service.ISipAccountService {

    private static final String MASK_VALUE = "******";

    private final ResourceDependencyServiceImpl dependencyService;
    private final ResourceAuditServiceImpl auditService;
    private final PasswordEncoder passwordEncoder;

    public PageResponse<SipAccount> getPageList(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<SipAccount> wrapper = new LambdaQueryWrapper<>();
        if (hasText(keyword)) {
            wrapper.and(w -> w.like(SipAccount::getUsername, keyword)
                    .or().like(SipAccount::getDisplayName, keyword));
        }
        wrapper.orderByDesc(SipAccount::getCreatedAt);
        IPage<SipAccount> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        page.getRecords().forEach(this::maskSecret);
        return PageResponse.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    public SipAccount getDetail(Long id) {
        return maskSecret(baseMapper.selectById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public SipAccount add(SipAccount account) {
        if (hasText(account.getPassword())) {
            account.setPassword(passwordEncoder.encode(account.getPassword()));
        }
        baseMapper.insert(account);
        auditService.recordCreate(ResourceType.SIP_ACCOUNT, account.getId(), safeSummary(account));
        return maskSecret(account);
    }

    @Transactional(rollbackFor = Exception.class)
    public SipAccount edit(Long id, SipAccount account) {
        SipAccount existing = getRequired(id);
        account.setId(id);
        if (hasText(account.getPassword()) && !MASK_VALUE.equals(account.getPassword())) {
            account.setPassword(passwordEncoder.encode(account.getPassword()));
        } else {
            account.setPassword(existing.getPassword());
        }
        baseMapper.updateById(account);
        auditService.recordUpdate(ResourceType.SIP_ACCOUNT, id, safeSummary(existing), safeSummary(account));
        return maskSecret(account);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SipAccount existing = getRequired(id);
        dependencyService.checkDeletable(ResourceType.SIP_ACCOUNT, id);
        baseMapper.deleteById(id);
        auditService.recordDelete(ResourceType.SIP_ACCOUNT, id, safeSummary(existing));
    }

    private SipAccount getRequired(Long id) {
        SipAccount account = baseMapper.selectById(id);
        if (account == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "SIP账号不存在: " + id);
        }
        return account;
    }

    private SipAccount maskSecret(SipAccount account) {
        if (account != null) {
            account.setPassword(MASK_VALUE);
        }
        return account;
    }

    private String safeSummary(SipAccount account) {
        if (account == null) {
            return null;
        }
        return "username=" + account.getUsername()
                + ",displayName=" + account.getDisplayName()
                + ",realm=" + account.getRealm()
                + ",enabled=" + account.getEnabled();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
