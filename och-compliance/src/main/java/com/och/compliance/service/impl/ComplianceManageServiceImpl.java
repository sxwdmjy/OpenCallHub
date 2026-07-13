package com.och.compliance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.compliance.domain.BlacklistRecord;
import com.och.compliance.domain.DncRecord;
import com.och.compliance.mapper.BlacklistRecordMapper;
import com.och.compliance.service.IComplianceManageService;
import com.och.compliance.mapper.DncRecordMapper;
import com.och.compliance.service.IComplianceManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 合规管理服务。
 * <p>
 * 提供 DNC 和黑名单的完整 CRUD 管理能力，包括分页查询、新增、删除和批量添加。
 * 所有写操作在事务中执行，并通过日志记录关键操作。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceManageServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.compliance.mapper.DncRecordMapper, com.och.compliance.domain.DncRecord> implements IComplianceManageService {

    private final BlacklistRecordMapper blacklistMapper;

    // ======================== DNC 管理 ========================

    /**
     * DNC 分页查询。
     */
    public IPage<DncRecord> pageDnc(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<DncRecord> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(DncRecord::getPhoneNumber, keyword);
        }
        wrapper.orderByDesc(DncRecord::getCreatedAt);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 新增 DNC 号码。
     */
    @Transactional
    public DncRecord addDnc(String phoneNumber, String reason, String source, LocalDateTime expireAt) {
        log.info("[Compliance] 新增DNC号码: phone={}, reason={}, source={}", phoneNumber, reason, source);
        DncRecord record = DncRecord.builder()
                .phoneNumber(phoneNumber)
                .reason(reason)
                .source(source)
                .expireAt(expireAt)
                .enabled(true)
                .build();
        baseMapper.insert(record);
        return record;
    }

    /**
     * 删除 DNC 号码。
     */
    @Transactional
    public void removeDnc(Long id) {
        log.info("[Compliance] 删除DNC号码: id={}", id);
        baseMapper.deleteById(id);
    }

    /**
     * 批量添加 DNC 号码。
     */
    @Transactional
    public int batchAddDnc(List<DncRecord> records) {
        log.info("[Compliance] 批量添加DNC号码: count={}", records.size());
        int count = 0;
        for (DncRecord record : records) {
            if (record.getEnabled() == null) {
                record.setEnabled(true);
            }
            baseMapper.insert(record);
            count++;
        }
        return count;
    }

    // ======================== 黑名单管理 ========================

    /**
     * 黑名单分页查询。
     */
    public IPage<BlacklistRecord> pageBlacklist(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<BlacklistRecord> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(BlacklistRecord::getPhoneNumber, keyword);
        }
        wrapper.orderByDesc(BlacklistRecord::getCreatedAt);
        return blacklistMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 新增黑名单号码。
     */
    @Transactional
    public BlacklistRecord addBlacklist(String phoneNumber, String reason, String level) {
        log.info("[Compliance] 新增黑名单: phone={}, reason={}, level={}", phoneNumber, reason, level);
        BlacklistRecord record = BlacklistRecord.builder()
                .phoneNumber(phoneNumber)
                .reason(reason)
                .level(level)
                .enabled(true)
                .build();
        blacklistMapper.insert(record);
        return record;
    }

    /**
     * 删除黑名单号码。
     */
    @Transactional
    public void removeBlacklist(Long id) {
        log.info("[Compliance] 删除黑名单: id={}", id);
        blacklistMapper.deleteById(id);
    }

    /**
     * 批量添加黑名单号码。
     */
    @Transactional
    public int batchAddBlacklist(List<BlacklistRecord> records) {
        log.info("[Compliance] 批量添加黑名单: count={}", records.size());
        int count = 0;
        for (BlacklistRecord record : records) {
            if (record.getEnabled() == null) {
                record.setEnabled(true);
            }
            blacklistMapper.insert(record);
            count++;
        }
        return count;
    }

    @Override
    @Transactional
    public int importDncCsv(InputStream inputStream, String source) {
        int count = 0;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String phone = line.trim();
                if (phone.isEmpty() || phone.startsWith("#")) continue;
                DncRecord record = DncRecord.builder()
                        .phoneNumber(phone).reason("CSV导入").source(source).enabled(true).build();
                baseMapper.insert(record);
                count++;
            }
        } catch (java.io.IOException e) {
            log.error("[Compliance] DNC CSV导入失败", e);
            throw new RuntimeException("CSV文件读取失败", e);
        }
        log.info("[Compliance] DNC CSV导入完成: count={}", count);
        return count;
    }

    @Override
    @Transactional
    public int importBlacklistCsv(InputStream inputStream) {
        int count = 0;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(",", 3);
                if (parts.length < 1 || parts[0].isEmpty() || parts[0].startsWith("#")) continue;
                String phone = parts[0].trim();
                String reason = parts.length > 1 ? parts[1].trim() : "CSV导入";
                String level = parts.length > 2 ? parts[2].trim() : "MEDIUM";
                BlacklistRecord record = BlacklistRecord.builder()
                        .phoneNumber(phone).reason(reason).level(level).enabled(true).build();
                blacklistMapper.insert(record);
                count++;
            }
        } catch (java.io.IOException e) {
            log.error("[Compliance] 黑名单CSV导入失败", e);
            throw new RuntimeException("CSV文件读取失败", e);
        }
        log.info("[Compliance] 黑名单CSV导入完成: count={}", count);
        return count;
    }
}
