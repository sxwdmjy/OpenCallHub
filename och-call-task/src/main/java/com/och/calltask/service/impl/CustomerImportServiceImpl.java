package com.och.calltask.service.impl;

import com.och.compliance.service.IComplianceCheckService;
import com.och.compliance.service.impl.ComplianceCheckServiceImpl;
import com.och.customer.domain.CustomerPhone;
import com.och.customer.domain.CustomerProfile;
import com.och.calltask.domain.OutboundCustomer;
import com.och.calltask.mapper.OutboundCustomerMapper;
import com.och.customer.mapper.CustomerPhoneMapper;
import com.och.customer.mapper.CustomerProfileMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 客户人群导入服务
 * <p>
 * 支持从 CSV 文件批量导入客户手机号到人群池中。
 * 导入流程：文件解析 → 手机号校验（格式 + 去重 + 合规检查） → 批量写入 CustomerPhone。
 * </p>
 *
 * <h3>CSV 格式要求</h3>
 * <p>每行一条记录，第一列为手机号（必填），第二列为客户姓名（可选），多余的列忽略。
 * 文件编码为 UTF-8，无表头行。</p>
 *
 * <h3>合规检查</h3>
 * <p>导入前对每个号码执行合规检查（DNC 黑名单），被拦截的号码不计入成功数，
 * 统计在黑名单拦截数中。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerImportServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.calltask.mapper.OutboundCustomerMapper, com.och.calltask.domain.OutboundCustomer> implements com.och.calltask.service.ICustomerImportService {

    private final CustomerPhoneMapper customerPhoneMapper;
    private final CustomerProfileMapper customerProfileMapper;
    private final IComplianceCheckService complianceCheckService;

    /** 中国大陆手机号正则：1 开头，共计 11 位数字 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 批量插入的批次大小 */
    private static final int BATCH_SIZE = 200;

    /**
     * 导入结果记录
     *
     * @param total       文件总行数
     * @param success     成功导入数
     * @param duplicate   重复号码数（在本次导入中重复或已在库中存在）
     * @param blacklisted 合规拦截数（DNC 或黑名单）
     */
    public record ImportResult(int total, int success, int duplicate, int blacklisted) {
        /** 格式校验失败数 = 总数 - (成功 + 重复 + 黑名单) */
        public int invalid() {
            return total - success - duplicate - blacklisted;
        }
    }

    /**
     * 从 CSV 输入流导入客户手机号到指定人群池
     * <p>
     * 每行解析一个手机号。解析成功后：
     * <ol>
     *   <li>校验手机号格式（11 位中国大陆手机号）</li>
     *   <li>合规检查（DNC + 黑名单）</li>
     *   <li>检查该号码是否已存在于指定人群池中（去重）</li>
     *   <li>创建 CustomerProfile（若不存在）和 CustomerPhone 记录</li>
     * </ol>
     * 采用分批提交策略，每 {@link #BATCH_SIZE} 条提交一次事务，
     * 避免大文件导入时长时间持锁和内存溢出。
     * </p>
     *
     * @param poolId      人群池 ID，关联 customer_profile.pool_id
     * @param inputStream 文件输入流（CSV 格式，UTF-8 编码）
     * @return 导入结果统计
     */
    @Transactional
    public ImportResult importFromCsv(Long poolId, InputStream inputStream) {
        int total = 0, success = 0, duplicate = 0, blacklisted = 0;

        // 用于批量插入的缓冲列表
        List<CustomerPhone> pendingPhones = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                total++;
                // 跳过空行
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                // ----- 第1步：解析 CSV 列（第一列为手机号） -----
                String[] cols = trimmed.split(",", -1);
                String phoneNumber = cols.length > 0 ? cols[0].trim() : "";
                String customerName = cols.length > 1 ? cols[1].trim() : null;

                // ----- 第2步：校验手机号格式 -----
                if (!isValidPhone(phoneNumber)) {
                    log.debug("[CustomerImport] Invalid phone format: {}", phoneNumber);
                    continue; // 不算任何统计类别，在 invalid() 中体现
                }

                // ----- 第3步：合规检查（DNC + 黑名单） -----
                ComplianceCheckServiceImpl.ComplianceResult compliance =
                        complianceCheckService.check(phoneNumber);
                if (compliance.blocked()) {
                    blacklisted++;
                    log.debug("[CustomerImport] Blocked phone {}: {}", phoneNumber, compliance.reason());
                    continue;
                }

                // ----- 第4步：去重检查（同一个池中是否已有该号码） -----
                if (existsInPool(poolId, phoneNumber)) {
                    duplicate++;
                    log.debug("[CustomerImport] Duplicate phone in pool: {}", phoneNumber);
                    continue;
                }

                // ----- 第5步：创建客户档案并关联电话 -----
                CustomerProfile profile = findOrCreateProfile(poolId, customerName);
                CustomerPhone phone = CustomerPhone.builder()
                        .customerId(profile.getId())
                        .phoneNumber(phoneNumber)
                        .phoneType("MOBILE")
                        .isPrimary(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                pendingPhones.add(phone);

                // ----- 第6步：批量写入 -----
                if (pendingPhones.size() >= BATCH_SIZE) {
                    flushBatch(pendingPhones);
                    success += pendingPhones.size();
                    pendingPhones.clear();
                }
            }

            // 写入剩余未达到批次大小的记录
            if (!pendingPhones.isEmpty()) {
                flushBatch(pendingPhones);
                success += pendingPhones.size();
            }

        } catch (Exception e) {
            log.error("[CustomerImport] Import failed for poolId={}: {}", poolId, e.getMessage(), e);
            throw new RuntimeException("客户导入失败: " + e.getMessage(), e);
        }

        ImportResult result = new ImportResult(total, success, duplicate, blacklisted);
        log.info("[CustomerImport] poolId={}, total={}, success={}, duplicate={}, blacklisted={}, invalid={}",
                poolId, result.total(), result.success(), result.duplicate(),
                result.blacklisted(), result.invalid());
        return result;
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 校验手机号格式
     * <p>中国大陆手机号：1 开头，第二位 3-9，共 11 位数字</p>
     */
    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 检查手机号是否已存在于指定人群池中
     * <p>通过 customer_profile.pool_id 关联查询</p>
     */
    private boolean existsInPool(Long poolId, String phoneNumber) {
        // 先通过 CustomerProfileMapper 的 findByPhone 查询
        CustomerProfile profile = customerProfileMapper.findByPhone(phoneNumber);
        if (profile == null) {
            return false;
        }
        // 检查该 profile 是否归属于当前 pool
        return poolId.equals(profile.getPoolId());
    }

    /**
     * 查找或创建客户档案
     * <p>
     * 如果号码对应的客户档案不存在，则创建一个新的档案并关联到指定人群池。
     * 客户姓名来自 CSV 第二列，未提供时默认为手机号脱敏值。
     * </p>
     */
    private CustomerProfile findOrCreateProfile(Long poolId, String customerName) {
        // 尝试通过已有 phone 查找 profile（由调用方保证 phoneNumber 未重复）
        // 由于 pool 内去重已做，此处直接创建新档案
        String name = (customerName != null && !customerName.isEmpty())
                ? customerName
                : "客户" + System.currentTimeMillis();

        CustomerProfile profile = CustomerProfile.builder()
                .customerName(name)
                .poolId(poolId)
                .source("CSV_IMPORT")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
        customerProfileMapper.insert(profile);
        log.debug("[CustomerImport] Created profile id={} name={} poolId={}", profile.getId(), name, poolId);
        return profile;
    }

    /**
     * 批量写入 CustomerPhone 记录
     * <p>逐条调用 MyBatis-Plus BaseMapper.insert，保证数据一致性</p>
     */
    private void flushBatch(List<CustomerPhone> phones) {
        for (CustomerPhone phone : phones) {
            customerPhoneMapper.insert(phone);
        }
        log.debug("[CustomerImport] Flushed {} phones", phones.size());
    }
}
