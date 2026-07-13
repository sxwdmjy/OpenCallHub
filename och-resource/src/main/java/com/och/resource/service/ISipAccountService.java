package com.och.resource.service;
import com.och.common.api.PageResponse;
import com.och.resource.domain.SipAccount;
public interface ISipAccountService {
    PageResponse<SipAccount> getPageList(int pageNum, int pageSize, String keyword);
    SipAccount getDetail(Long id);
    SipAccount add(SipAccount account);
    SipAccount edit(Long id, SipAccount account);
    void delete(Long id);
}
