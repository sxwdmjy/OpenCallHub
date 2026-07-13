package com.och.resource.service;
import com.och.common.api.PageResponse;
import com.och.resource.domain.PhoneNumber;
public interface IPhoneNumberService {
    PageResponse<PhoneNumber> getPageList(int pageNum, int pageSize, String keyword);
    PhoneNumber getDetail(Long id);
    PhoneNumber add(PhoneNumber phoneNumber);
    PhoneNumber edit(Long id, PhoneNumber phoneNumber);
    void delete(Long id);
}
