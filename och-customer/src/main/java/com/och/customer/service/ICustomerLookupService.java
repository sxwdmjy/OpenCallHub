package com.och.customer.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.customer.domain.CustomerPhone;
import com.och.customer.domain.CustomerPool;
import com.och.customer.domain.CustomerProfile;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
public interface ICustomerLookupService {
    CustomerProfile findByPhone(String phoneNumber);
    List<CustomerPhone> getPhones(Long customerId);
    void addProfile(CustomerProfile profile);
    void updateProfile(CustomerProfile profile);
    CustomerProfile getProfile(Long id);
    void deleteProfile(Long id);
    List<CustomerProfile> listProfiles(CustomerProfile query);
    IPage<CustomerProfile> pageProfiles(int pageNum, int pageSize, String keyword);
    IPage<CustomerPool> pagePools(int pageNum, int pageSize);
    void addPool(CustomerPool pool);
    void updatePool(CustomerPool pool);
    void deletePool(Long id);
    void importCustomers(MultipartFile file);
    java.util.List<com.och.customer.domain.CustomerFollowUp> getFollowUps(Long customerId);
    com.och.customer.domain.CustomerFollowUp addFollowUp(com.och.customer.domain.CustomerFollowUp followUp);
}
