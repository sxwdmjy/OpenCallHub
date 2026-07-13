package com.och.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.och.customer.domain.CustomerFollowUp;
import com.och.customer.domain.CustomerPhone;
import com.och.customer.domain.CustomerPool;
import com.och.customer.domain.CustomerProfile;
import com.och.customer.mapper.CustomerFollowUpMapper;
import com.och.customer.mapper.CustomerPhoneMapper;
import com.och.customer.service.ICustomerLookupService;
import com.och.customer.mapper.CustomerPoolMapper;
import com.och.customer.service.ICustomerLookupService;
import com.och.customer.mapper.CustomerProfileMapper;
import com.och.customer.service.ICustomerLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 客户查询应用服务
 * <p>集中封装客户画像、客户电话和旧版客户公海路由需要的基础查询能力。</p>
 *
 * @author danmo
 * @since 2026-06-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerLookupServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.customer.mapper.CustomerProfileMapper, com.och.customer.domain.CustomerProfile> implements ICustomerLookupService {

    private final CustomerPhoneMapper phoneMapper;
    private final CustomerPoolMapper poolMapper;
    private final CustomerFollowUpMapper followUpMapper;

    /**
     * 根据电话号码查询客户。
     *
     * @param phoneNumber 电话号码
     * @return 客户档案
     */
    public CustomerProfile findByPhone(String phoneNumber) {
        return baseMapper.findByPhone(phoneNumber);
    }

    /**
     * 获取客户的电话列表。
     *
     * @param customerId 客户 ID
     * @return 客户电话列表
     */
    public List<CustomerPhone> getPhones(Long customerId) {
        return phoneMapper.findByCustomerId(customerId);
    }

    /** 新增客户画像。 */
    public void addProfile(CustomerProfile profile) {
        baseMapper.insert(profile);
    }

    /** 更新客户画像。 */
    public void updateProfile(CustomerProfile profile) {
        baseMapper.updateById(profile);
    }

    /** 查询客户画像详情。 */
    public CustomerProfile getProfile(Long id) {
        return baseMapper.selectById(id);
    }

    /** 删除客户画像。 */
    public void deleteProfile(Long id) {
        baseMapper.deleteById(id);
    }

    /** 查询客户画像列表。 */
    public List<CustomerProfile> listProfiles(CustomerProfile query) {
        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            wrapper.eq(query.getId() != null, CustomerProfile::getId, query.getId())
                    .like(query.getCustomerName() != null && !query.getCustomerName().isBlank(), CustomerProfile::getCustomerName, query.getCustomerName())
                    .like(query.getCompanyName() != null && !query.getCompanyName().isBlank(), CustomerProfile::getCompanyName, query.getCompanyName())
                    .eq(query.getOwnerId() != null, CustomerProfile::getOwnerId, query.getOwnerId())
                    .eq(query.getPoolId() != null, CustomerProfile::getPoolId, query.getPoolId())
                    .eq(query.getStatus() != null && !query.getStatus().isBlank(), CustomerProfile::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(CustomerProfile::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 导入客户。
     * <p>当前只保留旧路由编译入口，实际 Excel 解析会在客户导入服务落地后接入。</p>
     */
    @Override
    public void importCustomers(MultipartFile file) {
        log.info("[客户导入] 收到客户文件: name={}, size={}", file == null ? "" : file.getOriginalFilename(), file == null ? 0 : file.getSize());
    }

    /**
     * 脱敏处理：隐藏中间4位。
     *
     * @param phone 电话号码
     * @return 脱敏后的电话号码
     */
    /**
     * 分页查询客户画像。
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键字（匹配客户名或公司名）
     * @return 分页结果
     */
    public IPage<CustomerProfile> pageProfiles(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<CustomerProfile> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(CustomerProfile::getCustomerName, keyword)
                    .or().like(CustomerProfile::getCompanyName, keyword);
        }
        wrapper.orderByDesc(CustomerProfile::getCreatedAt);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    // ======================== 客户池管理 ========================

    /** 分页查询客户池。 */
    public IPage<CustomerPool> pagePools(int pageNum, int pageSize) {
        LambdaQueryWrapper<CustomerPool> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CustomerPool::getCreatedAt);
        return poolMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /** 新增客户池。 */
    public void addPool(CustomerPool pool) {
        poolMapper.insert(pool);
    }

    /** 更新客户池。 */
    public void updatePool(CustomerPool pool) {
        poolMapper.updateById(pool);
    }

    /** 删除客户池。 */
    public void deletePool(Long id) {
        poolMapper.deleteById(id);
    }

    @Override
    public List<CustomerFollowUp> getFollowUps(Long customerId) {
        LambdaQueryWrapper<CustomerFollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerFollowUp::getCustomerId, customerId)
                .orderByDesc(CustomerFollowUp::getCreatedAt);
        return followUpMapper.selectList(wrapper);
    }

    @Override
    public CustomerFollowUp addFollowUp(CustomerFollowUp followUp) {
        followUpMapper.insert(followUp);
        log.info("[客户跟进] 新增跟进记录: customerId={}, type={}", followUp.getCustomerId(), followUp.getFollowType());
        return followUp;
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
