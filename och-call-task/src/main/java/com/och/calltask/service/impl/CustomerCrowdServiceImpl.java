package com.och.calltask.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.calltask.domain.CustomerCrowd;
import com.och.calltask.domain.CustomerCrowdRel;
import com.och.calltask.domain.OutboundCustomer;
import com.och.calltask.domain.OutboundCustomerState;
import com.och.calltask.mapper.CustomerCrowdMapper;
import com.och.calltask.mapper.CustomerCrowdRelMapper;
import com.och.calltask.mapper.OutboundCustomerMapper;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.customer.domain.CustomerPhone;
import com.och.customer.domain.CustomerProfile;
import com.och.customer.mapper.CustomerPhoneMapper;
import com.och.customer.mapper.CustomerProfileMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户人群管理服务
 * <p>
 * 提供客户人群的创建、计算、查询、统计以及关联外呼任务的能力。
 * 人群通过 poolId 关联客户池（CustomerProfile.poolId），
 * 通过 JSON 格式的 filterCondition 定义筛选规则，支持动态计算人群成员。
 * </p>
 *
 * <h3>filterCondition 格式</h3>
 * <pre>{@code
 * {"level":"VIP","status":"ACTIVE","tags":"新客户"}
 * }</pre>
 * 支持的字段：
 * <ul>
 *   <li>level - 客户等级，精确匹配</li>
 *   <li>status - 客户状态，精确匹配</li>
 *   <li>tags - 客户标签，模糊匹配</li>
 *   <li>source - 客户来源，精确匹配</li>
 *   <li>companyName - 公司名称，模糊匹配</li>
 * </ul>
 *
 * <h3>计算进度</h3>
 * <ul>
 *   <li>1 - 待计算（新建人群后初始状态）</li>
 *   <li>2 - 计算中（执行 calculateCrowd 时）</li>
 *   <li>3 - 计算完成</li>
 *   <li>4 - 计算失败</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerCrowdServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.calltask.mapper.CustomerCrowdMapper, com.och.calltask.domain.CustomerCrowd> implements com.och.calltask.service.ICustomerCrowdService {

    private final CustomerCrowdRelMapper crowdRelMapper;
    private final CustomerProfileMapper profileMapper;
    private final CustomerPhoneMapper phoneMapper;
    private final OutboundCustomerMapper outboundCustomerMapper;

    /**
     * 创建客户人群
     * <p>
     * 创建一条人群定义记录，关联到指定客户池（poolId），
     * 初始计算进度为"待计算"。创建后需调用 {@link #calculateCrowd(Long)} 完成人群计算。
     * </p>
     *
     * @param name            人群名称
     * @param poolId          客户池ID，关联 CustomerProfile.poolId
     * @param filterCondition 筛选条件（JSON字符串，如 {"level":"VIP","status":"ACTIVE"}）
     * @return 创建的人群实体
     */
    @Transactional
    public CustomerCrowd createCrowd(String name, Long poolId, String filterCondition) {
        if (name == null || name.isBlank()) {
            throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR, "人群名称不能为空");
        }
        if (poolId == null) {
            throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR, "客户池ID不能为空");
        }

        // 校验名称唯一性（同一 poolId 下不允许重名）
        LambdaQueryWrapper<CustomerCrowd> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerCrowd::getName, name)
                .eq(CustomerCrowd::getPoolId, poolId)
                .eq(CustomerCrowd::getDeleted, 0);
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BizException(ErrorCode.BIZ_DUPLICATE_KEY, "该客户池下已存在同名人群");
        }

        CustomerCrowd crowd = CustomerCrowd.builder()
                .name(name)
                .poolId(poolId)
                .filterCondition(filterCondition)
                .crowdNum(0)
                .progress(1)          // 待计算
                .status(1)            // 启用
                .type(1)              // 手动更新
                .createdAt(LocalDateTime.now())
                .build();
        baseMapper.insert(crowd);
        log.info("[CustomerCrowd] Created crowd: id={}, name={}, poolId={}", crowd.getId(), name, poolId);
        return crowd;
    }

    /**
     * 重新计算人群成员
     * <p>
     * 基于人群的 filterCondition 筛选 CustomerProfile，
     * 清空旧关联记录后批量写入新匹配的成员。
     * 计算流程：
     * <ol>
     *   <li>将人群进度置为"计算中"</li>
     *   <li>解析 filterCondition JSON，构建查询条件</li>
     *   <li>查询匹配的 CustomerProfile 列表</li>
     *   <li>逻辑删除旧的关联记录</li>
     *   <li>批量插入新的关联记录</li>
     *   <li>更新人群成员数量，置为"计算完成"</li>
     * </ol>
     * </p>
     *
     * @param crowdId 人群ID
     * @return 匹配的客户数量
     */
    @Transactional
    public int calculateCrowd(Long crowdId) {
        CustomerCrowd crowd = baseMapper.selectById(crowdId);
        if (crowd == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "人群不存在");
        }

        try {
            // 步骤1：标记为计算中
            crowd.setProgress(2);
            crowd.setReason(null);
            baseMapper.updateById(crowd);

            // 步骤2：解析 filterCondition 并查询匹配客户
            List<Long> matchedCustomerIds = findCustomerIdsByFilter(crowd.getPoolId(), crowd.getFilterCondition());

            // 步骤3：逻辑删除旧关联记录
            crowdRelMapper.deleteByCrowdId(crowdId);

            // 步骤4：批量插入新关联记录
            if (!matchedCustomerIds.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                List<CustomerCrowdRel> rels = matchedCustomerIds.stream()
                        .map(customerId -> CustomerCrowdRel.builder()
                                .crowdId(crowdId)
                                .customerId(customerId)
                                .createdAt(now)
                                .deleted(0)
                                .build())
                        .collect(Collectors.toList());
                crowdRelMapper.batchInsert(rels);
            }

            // 步骤5：更新人群成员数量，标记为计算完成
            crowd.setCrowdNum(matchedCustomerIds.size());
            crowd.setProgress(3);
            baseMapper.updateById(crowd);

            log.info("[CustomerCrowd] Calculated crowd: id={}, members={}", crowdId, matchedCustomerIds.size());
            return matchedCustomerIds.size();

        } catch (Exception e) {
            log.error("[CustomerCrowd] Calculate failed for crowdId={}: {}", crowdId, e.getMessage(), e);
            crowd.setProgress(4);
            crowd.setReason(e.getMessage());
            baseMapper.updateById(crowd);
            throw new BizException(ErrorCode.INTERNAL_ERROR, "人群计算失败: " + e.getMessage());
        }
    }

    /**
     * 查询人群下的所有客户
     * <p>
     * 通过关联表查出客户ID列表，再批量查询 CustomerProfile 和 CustomerPhone。
     * 返回的 Map 中包含 profile 和 phones 两组数据。
     * </p>
     *
     * @param crowdId 人群ID
     * @return Map 包含以下 key：
     *         <ul>
     *           <li>crowd - 人群定义</li>
     *           <li>profiles - 客户档案列表（CustomerProfile）</li>
     *           <li>phones - 客户电话列表（customerId -> List&lt;CustomerPhone&gt;）</li>
     *         </ul>
     */
    public Map<String, Object> getCrowdCustomers(Long crowdId) {
        CustomerCrowd crowd = baseMapper.selectById(crowdId);
        if (crowd == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "人群不存在");
        }

        // 查询关联的客户ID
        List<Long> customerIds = crowdRelMapper.findCustomerIdsByCrowdId(crowdId);
        if (customerIds.isEmpty()) {
            return Map.of("crowd", crowd, "profiles", Collections.emptyList(), "phones", Collections.emptyMap());
        }

        // 批量查询 CustomerProfile
        List<CustomerProfile> profiles = profileMapper.selectBatchIds(customerIds);

        // 批量查询 CustomerPhone（按 customerId 分组）
        LambdaQueryWrapper<CustomerPhone> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.in(CustomerPhone::getCustomerId, customerIds);
        List<CustomerPhone> allPhones = phoneMapper.selectList(phoneWrapper);
        Map<Long, List<CustomerPhone>> phoneMap = allPhones.stream()
                .collect(Collectors.groupingBy(CustomerPhone::getCustomerId));

        log.info("[CustomerCrowd] Queried crowd customers: crowdId={}, count={}", crowdId, profiles.size());
        return Map.of("crowd", crowd, "profiles", profiles, "phones", phoneMap);
    }

    /**
     * 人群统计
     * <p>
     * 返回人群的核心统计指标，包括成员总数、新增数、活跃数、非活跃数。
     * </p>
     *
     * @param crowdId 人群ID
     * @return Map 包含以下 key：
     *         <ul>
     *           <li>crowdId - 人群ID</li>
     *           <li>crowdName - 人群名称</li>
     *           <li>totalCount - 成员总数</li>
     *           <li>newCount - 近7天新增成员数</li>
     *           <li>activeCount - 活跃成员数（status=ACTIVE）</li>
     *           <li>inactiveCount - 非活跃成员数（status 非 ACTIVE）</li>
     *           <li>progress - 计算进度（1-待计算 2-计算中 3-计算完成 4-计算失败）</li>
     *           <li>lastUpdated - 最近更新时间</li>
     *         </ul>
     */
    public Map<String, Object> getCrowdStats(Long crowdId) {
        CustomerCrowd crowd = baseMapper.selectById(crowdId);
        if (crowd == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "人群不存在");
        }

        // 查询关联的客户ID
        List<Long> customerIds = crowdRelMapper.findCustomerIdsByCrowdId(crowdId);
        int totalCount = customerIds.size();

        int activeCount = 0;
        int newCount = 0;

        if (!customerIds.isEmpty()) {
            // 批量查询 CustomerProfile
            List<CustomerProfile> profiles = profileMapper.selectBatchIds(customerIds);

            // 统计活跃数
            activeCount = (int) profiles.stream()
                    .filter(p -> "ACTIVE".equals(p.getStatus()))
                    .count();

            // 统计近7天新增数
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            newCount = (int) profiles.stream()
                    .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(sevenDaysAgo))
                    .count();
        }

        int inactiveCount = totalCount - activeCount;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("crowdId", crowdId);
        stats.put("crowdName", crowd.getName());
        stats.put("totalCount", totalCount);
        stats.put("newCount", newCount);
        stats.put("activeCount", activeCount);
        stats.put("inactiveCount", inactiveCount);
        stats.put("progress", crowd.getProgress());
        stats.put("lastUpdated", crowd.getUpdatedAt());

        log.info("[CustomerCrowd] Stats for crowdId={}: total={}, active={}, new={}", crowdId, totalCount, activeCount, newCount);
        return stats;
    }

    /**
     * 将人群关联到外呼任务
     * <p>
     * 将人群下的所有客户（及其电话号码）批量创建为外呼任务的目标客户（OutboundCustomer）。
     * 每个客户取主号码优先（isPrimary=true），否则取第一个号码。
     * 创建的 OutboundCustomer 初始状态为 IMPORTED。
     * </p>
     *
     * @param crowdId 人群ID
     * @param taskId  外呼任务ID
     * @return 导入的客户数量
     */
    @Transactional
    public int assignToTask(Long crowdId, Long taskId) {
        CustomerCrowd crowd = baseMapper.selectById(crowdId);
        if (crowd == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "人群不存在");
        }

        // 查询人群关联的客户ID
        List<Long> customerIds = crowdRelMapper.findCustomerIdsByCrowdId(crowdId);
        if (customerIds.isEmpty()) {
            log.warn("[CustomerCrowd] AssignToTask: crowdId={} has no members", crowdId);
            return 0;
        }

        // 批量查询 CustomerProfile
        List<CustomerProfile> profiles = profileMapper.selectBatchIds(customerIds);

        // 查询所有关联电话
        LambdaQueryWrapper<CustomerPhone> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.in(CustomerPhone::getCustomerId, customerIds);
        List<CustomerPhone> allPhones = phoneMapper.selectList(phoneWrapper);
        Map<Long, List<CustomerPhone>> phoneMap = allPhones.stream()
                .collect(Collectors.groupingBy(CustomerPhone::getCustomerId));

        // 为每个客户创建 OutboundCustomer 记录
        LocalDateTime now = LocalDateTime.now();
        int importedCount = 0;

        for (CustomerProfile profile : profiles) {
            // 选择电话号码：优先主号码，否则取第一个
            List<CustomerPhone> phones = phoneMap.getOrDefault(profile.getId(), Collections.emptyList());
            String phoneNumber = selectBestPhone(phones);
            if (phoneNumber == null) {
                log.debug("[CustomerCrowd] Skipping customerId={}: no phone number", profile.getId());
                continue;
            }

            OutboundCustomer customer = OutboundCustomer.builder()
                    .taskId(taskId)
                    .phoneNumber(phoneNumber)
                    .customerName(profile.getCustomerName())
                    .state(OutboundCustomerState.IMPORTED.name())
                    .attemptCount(0)
                    .createdAt(now)
                    .build();
            outboundCustomerMapper.insert(customer);
            importedCount++;
        }

        log.info("[CustomerCrowd] Assigned crowd to task: crowdId={}, taskId={}, imported={}",
                crowdId, taskId, importedCount);
        return importedCount;
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 根据 filterCondition JSON 筛选匹配的客户ID
     * <p>
     * 解析 JSON 中的条件字段，动态构建 MyBatis-Plus QueryWrapper。
     * 支持字段：
     * <ul>
     *   <li>level - 客户等级，精确匹配</li>
     *   <li>status - 客户状态，精确匹配</li>
     *   <li>tags - 客户标签，模糊匹配</li>
     *   <li>source - 客户来源，精确匹配</li>
     *   <li>companyName - 公司名称，模糊匹配</li>
     * </ul>
     * </p>
     */
    private List<Long> findCustomerIdsByFilter(Long poolId, String filterCondition) {
        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerProfile::getPoolId, poolId)
                .eq(CustomerProfile::getDeleted, 0);

        if (filterCondition == null || filterCondition.isBlank()) {
            // 无筛选条件时返回该池下所有客户
            List<CustomerProfile> profiles = profileMapper.selectList(wrapper);
            return profiles.stream().map(CustomerProfile::getId).collect(Collectors.toList());
        }

        try {
            JSONObject filter = JSONObject.parseObject(filterCondition);

            if (filter.containsKey("level")) {
                String level = filter.getString("level");
                if (level != null && !level.isBlank()) {
                    wrapper.eq(CustomerProfile::getLevel, level);
                }
            }

            if (filter.containsKey("status")) {
                String status = filter.getString("status");
                if (status != null && !status.isBlank()) {
                    wrapper.eq(CustomerProfile::getStatus, status);
                }
            }

            if (filter.containsKey("tags")) {
                String tags = filter.getString("tags");
                if (tags != null && !tags.isBlank()) {
                    wrapper.like(CustomerProfile::getTags, tags);
                }
            }

            if (filter.containsKey("source")) {
                String source = filter.getString("source");
                if (source != null && !source.isBlank()) {
                    wrapper.eq(CustomerProfile::getSource, source);
                }
            }

            if (filter.containsKey("companyName")) {
                String companyName = filter.getString("companyName");
                if (companyName != null && !companyName.isBlank()) {
                    wrapper.like(CustomerProfile::getCompanyName, companyName);
                }
            }

        } catch (Exception e) {
            log.error("[CustomerCrowd] Failed to parse filterCondition: {}", filterCondition, e);
            throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR, "筛选条件格式错误: " + e.getMessage());
        }

        List<CustomerProfile> profiles = profileMapper.selectList(wrapper);
        return profiles.stream().map(CustomerProfile::getId).collect(Collectors.toList());
    }

    /**
     * 从电话列表中选取最佳号码
     * <p>优先级：主号码（isPrimary=true） > 第一个号码 > null</p>
     */
    private String selectBestPhone(List<CustomerPhone> phones) {
        if (phones == null || phones.isEmpty()) {
            return null;
        }
        // 优先选主号码
        for (CustomerPhone phone : phones) {
            if (Boolean.TRUE.equals(phone.getIsPrimary())) {
                return phone.getPhoneNumber();
            }
        }
        // 否则取第一个
        return phones.get(0).getPhoneNumber();
    }
}
