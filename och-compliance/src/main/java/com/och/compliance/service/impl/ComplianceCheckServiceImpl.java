package com.och.compliance.service.impl;



import com.och.compliance.mapper.BlacklistRecordMapper;
import com.och.compliance.service.IComplianceCheckService;
import org.springframework.stereotype.Service;

/**
 * 合规检查服务。
 * 对外提供号码合规检查：先查黑名单，再查 DNC 列表，返回检查结果（是否拦截、原因、过滤类型）。
 */
@Service
public class ComplianceCheckServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.compliance.mapper.DncRecordMapper, com.och.compliance.domain.DncRecord> implements IComplianceCheckService {

    private final BlacklistRecordMapper blacklistMapper;

    public ComplianceCheckServiceImpl(BlacklistRecordMapper blacklistMapper) {
        this.blacklistMapper = blacklistMapper;
    }

    public record ComplianceResult(boolean blocked, String reason, String filterType) {
        public static ComplianceResult pass() { return new ComplianceResult(false, null, null); }
        public static ComplianceResult dnc(String reason) { return new ComplianceResult(true, reason, "DNC"); }
        public static ComplianceResult blacklist(String reason) { return new ComplianceResult(true, reason, "BLACKLIST"); }
    }

    public ComplianceResult check(String phoneNumber) {
        if (blacklistMapper.isBlacklisted(phoneNumber) > 0) {
            return ComplianceResult.blacklist("号码 " + phoneNumber + " 在黑名单中");
        }
        if (baseMapper.isDncNumber(phoneNumber) > 0) {
            return ComplianceResult.dnc("号码 " + phoneNumber + " 在DNC列表中");
        }
        return ComplianceResult.pass();
    }
}
